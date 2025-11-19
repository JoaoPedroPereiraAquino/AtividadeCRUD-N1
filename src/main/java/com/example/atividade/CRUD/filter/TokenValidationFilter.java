package com.example.atividade.CRUD.filter;

import com.example.atividade.CRUD.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenValidationFilter extends OncePerRequestFilter {

    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Permitir acesso às rotas públicas
        if (path.equals("/login") || path.equals("/auth/login") || 
            path.equals("/auth/register") ||
            path.startsWith("/css/") || path.startsWith("/js/") ||
            path.startsWith("/api/auth/") || path.equals("/auth/logout")) {
            System.out.println("TokenValidationFilter: Permitindo acesso público para: " + path);
            filterChain.doFilter(request, response);
            return;
        }
        
        System.out.println("TokenValidationFilter: Verificando autenticação para: " + path);

        // Para rotas da API, validar token no header Authorization
        if (path.startsWith("/api/")) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (authService.validateToken(token)) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                } catch (Exception e) {
                    // Se houver erro ao validar (auth-server não disponível), permitir acesso
                    // Em produção, você pode querer bloquear aqui
                }
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Para rotas web, verificar token na sessão
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("token") != null) {
            String token = (String) session.getAttribute("token");
            try {
                if (authService.validateToken(token)) {
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    // Token inválido, limpar sessão e redirecionar
                    session.invalidate();
                    if (!path.startsWith("/api/")) {
                        response.sendRedirect("/auth/login");
                        return;
                    }
                }
            } catch (Exception e) {
                // Se houver erro ao validar (auth-server não disponível), permitir acesso temporariamente
                // Isso permite que a aplicação funcione mesmo sem o auth-server rodando
                System.err.println("Erro ao validar token: " + e.getMessage());
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Se não tem sessão/token, redirecionar para login (exceto rotas públicas)
        if (!path.startsWith("/api/") && !path.equals("/auth/register") && !path.equals("/auth/login")) {
            System.out.println("TokenValidationFilter: Redirecionando para login - path: " + path);
            response.sendRedirect("/auth/login");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

