package com.example.atividade.CRUD.service;

import com.example.atividade.CRUD.dto.LoginRequest;
import com.example.atividade.CRUD.dto.RegisterRequest;
import com.example.atividade.CRUD.dto.TokenResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

    @Value("${auth.server.url}")
    private String authServerUrl;

    @Value("${auth.server.client.id}")
    private String clientId;

    @Value("${auth.server.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    @Autowired
    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TokenResponse login(LoginRequest loginRequest) {
        String url = authServerUrl + "/oauth/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", loginRequest.getUsername());
        body.add("password", loginRequest.getPassword());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                url, 
                request, 
                TokenResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("Falha na autenticação");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer login: " + e.getMessage(), e);
        }
    }

    public boolean validateToken(String token) {
        String url = authServerUrl + "/oauth/check_token?token=" + token;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(url, request, Object.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> register(RegisterRequest registerRequest) {
        // Criar usuário via endpoint do auth-server
        // O endpoint /manager requer autenticação admin, então precisamos primeiro fazer login como admin
        // e depois criar o usuário
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Primeiro, fazer login como admin para obter token
            LoginRequest adminLogin = new LoginRequest("teste@teste.com", "123456");
            TokenResponse adminTokenResponse;
            
            try {
                adminTokenResponse = login(adminLogin);
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", "Erro ao autenticar administrador. Verifique se o auth-server está rodando.");
                return result;
            }
            
            // Agora criar o usuário usando o token de admin
            String url = authServerUrl + "/manager";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminTokenResponse.getAccess_token());
            
            // Preparar dados do usuário conforme esperado pelo auth-server
            Map<String, Object> userData = new HashMap<>();
            userData.put("login", registerRequest.getEmail().toLowerCase());
            userData.put("password", registerRequest.getPassword());
            userData.put("roles", new String[]{"ROLE_USER"});
            userData.put("extra", new HashMap<>());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(userData, headers);
            
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    result.put("success", true);
                    result.put("message", "Cadastro realizado com sucesso!");
                    return result;
                }
                
                result.put("success", false);
                result.put("message", "Erro ao realizar cadastro");
                return result;
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                String errorMessage = e.getResponseBodyAsString();
                if (e.getStatusCode().value() == 403) {
                    result.put("success", false);
                    result.put("message", "Cadastro requer permissões de administrador.");
                } else if (e.getStatusCode().value() == 400 && errorMessage != null && errorMessage.contains("already exists")) {
                    result.put("success", false);
                    result.put("message", "Este e-mail já está cadastrado. Tente fazer login.");
                } else {
                    result.put("success", false);
                    result.put("message", "Erro ao realizar cadastro: " + (errorMessage != null ? errorMessage : e.getMessage()));
                }
                return result;
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Erro ao realizar cadastro: " + e.getMessage());
            return result;
        }
    }
}

