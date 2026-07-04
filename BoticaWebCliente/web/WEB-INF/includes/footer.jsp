<%-- 
    Document   : footer
    Created on : 2 dic. 2025, 15:21:35
    Author     : yerri
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <!-- jQuery (opcional, útil para AJAX) -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    
    <!-- JS personalizado -->
    <script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
    
    <!-- Scripts adicionales de la página -->
    <% if (request.getAttribute("extraScripts") != null) { %>
        <%= request.getAttribute("extraScripts") %>
    <% } %>
</body>
</html>