package com.example.atividade.CRUD.controller;

import com.example.atividade.CRUD.dto.LoginRequest;
import com.example.atividade.CRUD.dto.RegisterRequest;
import com.example.atividade.CRUD.dto.TokenResponse;
import com.example.atividade.CRUD.service.AuthService;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        return "auth-modal";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        try {
            LoginRequest loginRequest = new LoginRequest(username, password);
            TokenResponse tokenResponse = authService.login(loginRequest);
            
            if (tokenResponse != null && tokenResponse.getAccess_token() != null) {
                // Armazenar token na sessão
                session.setAttribute("token", tokenResponse.getAccess_token());
                session.setAttribute("username", username);
                
                return "redirect:/";
            } else {
                redirectAttributes.addFlashAttribute("erro", "Erro ao obter token de autenticação");
                return "redirect:/login";
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                redirectAttributes.addFlashAttribute("erro", "Usuário ou senha inválidos");
            } else {
                redirectAttributes.addFlashAttribute("erro", "Erro ao fazer login: " + e.getMessage());
            }
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao fazer login: " + e.getMessage() + ". Verifique se o auth-server está rodando.");
            return "redirect:/login";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        try {
            RegisterRequest registerRequest = new RegisterRequest(email, password, email);
            Map<String, Object> result = authService.register(registerRequest);
            
            if (result.get("success") != null && (Boolean) result.get("success")) {
                redirectAttributes.addFlashAttribute("sucesso", "Cadastro realizado com sucesso! Faça login.");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("erro", result.get("message") != null ? 
                    result.get("message").toString() : "Erro ao realizar cadastro");
                return "redirect:/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao realizar cadastro: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

