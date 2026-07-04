package controller;

import DAO.MovimientoInventarioDAO;
import DTO.MovimientoInventarioDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller para operaciones de Inventario
 */
@WebServlet(name = "InventarioController", urlPatterns = {"/InventarioController"})
public class InventarioController extends HttpServlet {

    private final Gson gson = new Gson();
    private final MovimientoInventarioDAO movimientoDAO = new MovimientoInventarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            switch (action != null ? action : "") {
                case "listarMovimientos":
                    listarMovimientos(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/admin/inventario/listar.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            enviarErrorJson(response, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            switch (action != null ? action : "") {
                case "ajustarStock":
                    ajustarStock(request, response);
                    break;
                default:
                    enviarErrorJson(response, "Acción no válida");
            }
        } catch (Exception e) {
            e.printStackTrace();
            enviarErrorJson(response, "Error: " + e.getMessage());
        }
    }

    /**
     * Ajustar stock de un producto
     */
    private void ajustarStock(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Validar sesión
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            enviarErrorJson(response, "Sesión no válida");
            return;
        }
        Long usuarioId = (Long) session.getAttribute("userId");

        // Obtener parámetros
        String productoIdParam = request.getParameter("productoId");
        String nuevoStockParam = request.getParameter("nuevoStock");
        String motivo = request.getParameter("motivo");

        if (productoIdParam == null || nuevoStockParam == null || motivo == null) {
            enviarErrorJson(response, "Parámetros incompletos");
            return;
        }

        try {
            Long productoId = Long.parseLong(productoIdParam);
            int nuevoStock = Integer.parseInt(nuevoStockParam);

            if (nuevoStock < 0) {
                enviarErrorJson(response, "El stock no puede ser negativo");
                return;
            }

            if (motivo.trim().isEmpty()) {
                enviarErrorJson(response, "Debe especificar un motivo");
                return;
            }

            // Realizar ajuste (el DAO se encarga de actualizar producto y registrar movimiento)
            boolean resultado = movimientoDAO.realizarAjuste(productoId, nuevoStock, motivo.trim(), usuarioId);

            if (resultado) {
                JsonObject json = new JsonObject();
                json.addProperty("success", true);
                json.addProperty("message", "Stock ajustado correctamente");
                enviarJsonResponse(response, json.toString());
            } else {
                enviarErrorJson(response, "Error al ajustar el stock");
            }

        } catch (NumberFormatException e) {
            enviarErrorJson(response, "Valores inválidos");
        }
    }

    /**
     * Listar movimientos (para AJAX si se necesita)
     */
    private void listarMovimientos(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Por ahora redirige a la JSP
        response.sendRedirect(request.getContextPath() + "/admin/inventario/listar.jsp");
    }

    // =====================================================
    //              MÉTODOS AUXILIARES
    // =====================================================

    private void enviarJsonResponse(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
            out.flush();
        }
    }

    private void enviarErrorJson(HttpServletResponse response, String mensaje) throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", false);
        obj.addProperty("error", mensaje);
        enviarJsonResponse(response, obj.toString());
    }
}
