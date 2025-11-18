package com.example.atividade.CRUD.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Permitir acesso às páginas públicas
        String path = request.getRequestURI();
        
        if (path.equals("/login") || path.equals("/auth/login") || 
            path.startsWith("/css/") || path.startsWith("/js/") ||
            path.startsWith("/api/auth/")) {
            return true;
        }

        // Verificar se o usuário está autenticado
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("token") == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}

