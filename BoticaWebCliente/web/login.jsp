<%-- 
    Document   : login
    Created on : 1 dic. 2025, 23:44:57
    Author     : yerri
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Login - Sistema Botica</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <style>
            body {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                display: flex;
                align-items: center;
            }
            .login-card {
                max-width: 400px;
                margin: 0 auto;
                border-radius: 15px;
                box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            }
            .login-header {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                border-radius: 15px 15px 0 0;
                padding: 30px;
                text-align: center;
            }
            .login-body {
                padding: 40px;
                background: white;
                border-radius: 0 0 15px 15px;
            }
            .btn-login {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                border: none;
                padding: 12px;
                font-weight: 600;
            }
            .btn-login:hover {
                opacity: 0.9;
            }
            .logo-img {
                width: 80px;       /* Tamaño del logo */
                height: auto;      /* Mantiene proporción */
                display: block;    /* Quita espacios raros */
                margin: 0 auto;    /* Centra el logo */
            }

        </style>
    </head>
    <body>
        <div class="container">
            <div class="login-card">
                <div class="login-header">
                    <img src="assets/img/logo.png" alt="logo" class="logo-img" />
                    <h2 class="mb-0">Sistema Botica</h2>
                    <p class="mb-0 mt-2">Iniciar Sesión</p>
                </div>
                <div class="login-body">
                    <%
                        String error = (String) request.getAttribute("error");
                        if (error != null) {
                    %>
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <strong>Error:</strong> <%= error%>
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                    <% }%>

                    <form action="<%= request.getContextPath()%>/AuthController" method="POST">
                        <input type="hidden" name="action" value="login">

                        <div class="mb-3">
                            <label for="username" class="form-label">Usuario</label>
                            <input type="text" class="form-control" id="username" name="username" 
                                   placeholder="Ingrese su usuario" required autofocus>
                        </div>

                        <div class="mb-4">
                            <label for="password" class="form-label">Contraseña</label>
                            <input type="password" class="form-control" id="password" name="password" 
                                   placeholder="Ingrese su contraseña" required>
                        </div>

                        <div class="d-grid">
                            <button type="submit" class="btn btn-primary btn-login">
                                Iniciar Sesión
                            </button>
                        </div>
                    </form>

                    <div class="text-center mt-3">
                        <small class="text-muted">
                            Usuario por defecto: <strong>admin</strong> / Contraseña: <strong>admin</strong>
                        </small>
                    </div>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>