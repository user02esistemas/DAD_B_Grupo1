/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Filtro de autenticación simple.
 * Protege las rutas excepto login, recursos estáticos, etc.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nada por ahora
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // Rutas públicas (ajusta según tu proyecto)
        boolean recursoLogin = uri.equals(contextPath + "/login.jsp");
        boolean recursoAuth = uri.startsWith(contextPath + "/AuthController");
        boolean recursosEstaticos = uri.startsWith(contextPath + "/assets/")
                || uri.startsWith(contextPath + "/css/")
                || uri.startsWith(contextPath + "/js/")
                || uri.startsWith(contextPath + "/img/");

        if (recursoLogin || recursoAuth || recursosEstaticos) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        Object usuarioLogueado = (session != null) ? session.getAttribute("usuarioLogueado") : null;

        if (usuarioLogueado == null) {
            response.sendRedirect(contextPath + "/login.jsp");
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        // nada
    }
}

