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
        } catch (HttpClientErrorException e) {
            // Preservar HttpClientErrorException para o controller tratar adequadamente
            // Log do erro para depuração
            System.err.println("Erro HTTP ao fazer login: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Erro de conexão com o auth-server
            throw new RuntimeException("Não foi possível conectar ao servidor de autenticação. Verifique se o auth-server está rodando em " + authServerUrl, e);
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
        
        System.out.println("=== INÍCIO DO CADASTRO ===");
        System.out.println("Email recebido: " + registerRequest.getEmail());
        System.out.println("Password length: " + (registerRequest.getPassword() != null ? registerRequest.getPassword().length() : "null"));
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Primeiro, fazer login como admin para obter token
            LoginRequest adminLogin = new LoginRequest("teste@teste.com", "123456");
            TokenResponse adminTokenResponse;
            
            try {
                System.out.println("Tentando fazer login como admin...");
                adminTokenResponse = login(adminLogin);
                System.out.println("Login admin bem-sucedido! Token obtido.");
            } catch (HttpClientErrorException e) {
                result.put("success", false);
                String errorMsg = e.getResponseBodyAsString();
                System.err.println("Erro ao autenticar admin para cadastro: " + e.getStatusCode() + " - " + errorMsg);
                if (e.getStatusCode().value() == 400 && errorMsg != null && errorMsg.contains("Invalid username or password")) {
                    result.put("message", "Erro interno: Credenciais de administrador inválidas. Contate o suporte.");
                } else {
                    result.put("message", "Erro ao autenticar administrador. Verifique se o auth-server está rodando.");
                }
                return result;
            } catch (Exception e) {
                result.put("success", false);
                System.err.println("Erro inesperado ao autenticar admin: " + e.getMessage());
                e.printStackTrace();
                result.put("message", "Erro ao autenticar administrador. Verifique se o auth-server está rodando.");
                return result;
            }
            
            // Agora criar o usuário usando o token de admin
            String url = authServerUrl + "/manager";
            System.out.println("URL completa para criar usuário: " + url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String token = adminTokenResponse.getAccess_token();
            System.out.println("Token completo (length: " + token.length() + "): " + token.substring(0, Math.min(50, token.length())) + "...");
            headers.setBearerAuth(token);
            
            // Preparar dados do usuário conforme esperado pelo auth-server
            Map<String, Object> userData = new HashMap<>();
            String login = registerRequest.getEmail().toLowerCase();
            userData.put("login", login);
            userData.put("password", registerRequest.getPassword());
            userData.put("roles", java.util.Arrays.asList("ROLE_USER")); // ManagerController espera List, não array
            userData.put("extra", new HashMap<>());
            
            System.out.println("Dados do usuário preparados: " + userData);
            System.out.println("Login: " + login);
            System.out.println("Password: " + (registerRequest.getPassword() != null ? "***" : "null"));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(userData, headers);
            
            try {
                System.out.println("=== TENTANDO CRIAR USUÁRIO ===");
                System.out.println("URL: " + url);
                System.out.println("Email: " + registerRequest.getEmail());
                System.out.println("Headers: " + headers);
                
                System.out.println("Fazendo requisição POST para " + url + "...");
                ResponseEntity<Map> response;
                try {
                    response = restTemplate.postForEntity(url, request, Map.class);
                } catch (org.springframework.web.client.RestClientException e) {
                    System.err.println("ERRO na requisição RestTemplate: " + e.getClass().getName() + " - " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
                
                System.out.println("Resposta recebida - Status: " + response.getStatusCode());
                System.out.println("Resposta recebida - Body: " + response.getBody());
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    Boolean success = (Boolean) responseBody.get("success");
                    String message = (String) responseBody.get("message");
                    
                    System.out.println("SUCESSO no cadastro - success: " + success + ", message: " + message);
                    
                    result.put("success", true);
                    result.put("message", message != null ? message : "Cadastro realizado com sucesso!");
                    return result;
                }
                
                System.err.println("ERRO: Resposta não foi bem-sucedida");
                result.put("success", false);
                result.put("message", "Erro ao realizar cadastro");
                return result;
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                String errorMessage = e.getResponseBodyAsString();
                System.err.println("Erro HTTP no cadastro: " + e.getStatusCode() + " - " + errorMessage);
                
                if (e.getStatusCode().value() == 403) {
                    result.put("success", false);
                    result.put("message", "Cadastro requer permissões de administrador.");
                } else if (e.getStatusCode().value() == 400) {
                    // Verificar diferentes tipos de erro 400
                    if (errorMessage != null && (errorMessage.contains("already exists") || errorMessage.contains("já existe") || errorMessage.contains("user_exists"))) {
                        result.put("success", false);
                        result.put("message", "Este e-mail já está cadastrado. Tente fazer login.");
                    } else if (errorMessage != null && errorMessage.contains("obrigatório")) {
                        result.put("success", false);
                        result.put("message", "Dados incompletos. Verifique se preencheu todos os campos.");
                    } else {
                        result.put("success", false);
                        result.put("message", "Erro ao realizar cadastro: " + (errorMessage != null ? errorMessage : e.getMessage()));
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "Erro ao realizar cadastro: " + (errorMessage != null ? errorMessage : e.getMessage()));
                }
                return result;
            } catch (Exception e) {
                System.err.println("ERRO GERAL no cadastro: " + e.getMessage());
                e.printStackTrace();
                result.put("success", false);
                result.put("message", "Erro ao realizar cadastro: " + e.getMessage());
                return result;
            }
        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO no método register: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Erro inesperado ao realizar cadastro: " + e.getMessage());
            return result;
        }
    }
}

