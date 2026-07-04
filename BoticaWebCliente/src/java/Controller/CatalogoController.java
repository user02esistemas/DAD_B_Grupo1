package controller;

import DTO.CatalogoProductoDTO;
import DAO.CatalogoProductoDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller para el CRUD del Catálogo de Productos DIGEMID
 */
@WebServlet(name = "CatalogoController", urlPatterns = {"/CatalogoController"})
public class CatalogoController extends HttpServlet {

    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private final CatalogoProductoDAO dao = new CatalogoProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            switch (action != null ? action : "") {
                case "buscar":
                    buscarProductos(request, response);
                    break;
                case "obtener":
                    obtenerProducto(request, response);
                    break;
                case "estadisticas":
                    obtenerEstadisticas(request, response);
                    break;
                case "listar":
                    listarPaginado(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/admin/compras/catalogo.jsp");
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
                case "guardar":
                    guardarProducto(request, response);
                    break;
                case "eliminar":
                    eliminarProducto(request, response);
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
     * Buscar productos (autocomplete)
     */
    private void buscarProductos(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String termino = request.getParameter("q");
        if (termino == null || termino.trim().length() < 2) {
            enviarJsonResponse(response, "[]");
            return;
        }

        List<CatalogoProductoDTO> productos = dao.buscarParaAutocomplete(termino.trim(), 20);

        JsonArray jsonArray = new JsonArray();
        for (CatalogoProductoDTO p : productos) {
            jsonArray.add(construirJsonProducto(p));
        }

        enviarJsonResponse(response, jsonArray.toString());
    }

    /**
     * Obtener producto por ID
     */
    private void obtenerProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            enviarErrorJson(response, "ID no proporcionado");
            return;
        }

        try {
            Long id = Long.parseLong(idParam);
            CatalogoProductoDTO producto = dao.buscarPorId(id);

            if (producto == null) {
                enviarErrorJson(response, "Producto no encontrado");
                return;
            }

            JsonObject json = new JsonObject();
            json.addProperty("success", true);
            json.add("producto", construirJsonProducto(producto));
            enviarJsonResponse(response, json.toString());

        } catch (NumberFormatException e) {
            enviarErrorJson(response, "ID inválido");
        }
    }

    /**
     * Estadísticas del catálogo
     */
    private void obtenerEstadisticas(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        JsonObject json = new JsonObject();

        try {
            int total = dao.contarTotal();
            int activos = dao.contarActivos();
            int laboratorios = dao.contarLaboratorios();
            String ultimoAgregado = dao.obtenerUltimoAgregado();

            json.addProperty("total", total);
            json.addProperty("activos", activos);
            json.addProperty("laboratorios", laboratorios);
            json.addProperty("ultimoAgregado", ultimoAgregado != null ? ultimoAgregado : "-");

        } catch (Exception e) {
            json.addProperty("total", 0);
            json.addProperty("activos", 0);
            json.addProperty("laboratorios", 0);
            json.addProperty("ultimoAgregado", "-");
        }

        enviarJsonResponse(response, json.toString());
    }

    /**
     * Listar paginado
     */
    private void listarPaginado(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int pagina = 1;
        int porPagina = 20;

        try {
            if (request.getParameter("pagina") != null) {
                pagina = Integer.parseInt(request.getParameter("pagina"));
            }
            if (request.getParameter("porPagina") != null) {
                porPagina = Integer.parseInt(request.getParameter("porPagina"));
            }
        } catch (NumberFormatException e) {
            // Usar valores por defecto
        }

        List<CatalogoProductoDTO> productos = dao.listarPaginado(pagina, porPagina);

        JsonArray jsonArray = new JsonArray();
        for (CatalogoProductoDTO p : productos) {
            jsonArray.add(construirJsonProducto(p));
        }

        JsonObject json = new JsonObject();
        json.addProperty("success", true);
        json.addProperty("total", dao.contarTotal());
        json.addProperty("pagina", pagina);
        json.addProperty("porPagina", porPagina);
        json.add("productos", jsonArray);

        enviarJsonResponse(response, json.toString());
    }

    /**
     * Guardar producto (crear o actualizar)
     */
    private void guardarProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Leer JSON del body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String jsonData = sb.toString();
        if (jsonData.isEmpty()) {
            enviarErrorJson(response, "No se recibieron datos");
            return;
        }

        try {
            JsonObject jsonProducto = gson.fromJson(jsonData, JsonObject.class);

            // Validar nombre comercial
            if (!jsonProducto.has("nombreComercial") || 
                jsonProducto.get("nombreComercial").getAsString().trim().isEmpty()) {
                enviarErrorJson(response, "El nombre comercial es obligatorio");
                return;
            }

            CatalogoProductoDTO producto = new CatalogoProductoDTO();

            // Si tiene ID, es actualización
            if (jsonProducto.has("id") && !jsonProducto.get("id").isJsonNull() &&
                !jsonProducto.get("id").getAsString().isEmpty()) {
                producto.setId(jsonProducto.get("id").getAsLong());
            }

            producto.setCodigoProducto(getStringFromJson(jsonProducto, "codigoProducto"));
            producto.setRegistroSanitario(getStringFromJson(jsonProducto, "registroSanitario"));
            producto.setCantidad(jsonProducto.has("cantidad") ? jsonProducto.get("cantidad").getAsInt() : 1);
            producto.setNombreComercial(getStringFromJson(jsonProducto, "nombreComercial"));
            producto.setPrincipioActivo(getStringFromJson(jsonProducto, "principioActivo"));
            producto.setConcentracion(getStringFromJson(jsonProducto, "concentracion"));
            producto.setFormaFarmaceutica(getStringFromJson(jsonProducto, "formaFarmaceutica"));
            producto.setPresentacion(getStringFromJson(jsonProducto, "presentacion"));
            producto.setLaboratorio(getStringFromJson(jsonProducto, "laboratorio"));
            producto.setNombreFabricante(getStringFromJson(jsonProducto, "nombreFabricante"));
            producto.setNombreTitular(getStringFromJson(jsonProducto, "nombreTitular"));
            producto.setActivo(true);

            boolean resultado;
            String mensaje;

            if (producto.getId() != null) {
                resultado = dao.actualizar(producto);
                mensaje = "Producto actualizado correctamente";
            } else {
                Long id = dao.insertar(producto);
                resultado = id != null;
                if (resultado) {
                    producto.setId(id);
                }
                mensaje = "Producto creado correctamente";
            }

            if (resultado) {
                JsonObject json = new JsonObject();
                json.addProperty("success", true);
                json.addProperty("id", producto.getId());
                json.addProperty("message", mensaje);
                enviarJsonResponse(response, json.toString());
            } else {
                enviarErrorJson(response, "Error al guardar el producto");
            }

        } catch (Exception e) {
            e.printStackTrace();
            enviarErrorJson(response, "Error al procesar los datos: " + e.getMessage());
        }
    }

    /**
     * Eliminar producto (desactivar)
     */
    private void eliminarProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            enviarErrorJson(response, "ID no proporcionado");
            return;
        }

        try {
            Long id = Long.parseLong(idParam);

            if (dao.desactivar(id)) {
                JsonObject json = new JsonObject();
                json.addProperty("success", true);
                json.addProperty("message", "Producto eliminado correctamente");
                enviarJsonResponse(response, json.toString());
            } else {
                enviarErrorJson(response, "Error al eliminar el producto");
            }

        } catch (NumberFormatException e) {
            enviarErrorJson(response, "ID inválido");
        }
    }

    // =====================================================
    //              MÉTODOS AUXILIARES
    // =====================================================

    private JsonObject construirJsonProducto(CatalogoProductoDTO p) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", p.getId());
        obj.addProperty("codigoProducto", p.getCodigoProducto());
        obj.addProperty("nombreComercial", p.getNombreComercial());
        obj.addProperty("principioActivo", p.getPrincipioActivo());
        obj.addProperty("concentracion", p.getConcentracion());
        obj.addProperty("formaFarmaceutica", p.getFormaFarmaceutica());
        obj.addProperty("presentacion", p.getPresentacion());
        obj.addProperty("laboratorio", p.getLaboratorio());
        obj.addProperty("registroSanitario", p.getRegistroSanitario());
        obj.addProperty("cantidad", p.getCantidad());
        obj.addProperty("nombreTitular", p.getNombreTitular());
        obj.addProperty("nombreFabricante", p.getNombreFabricante());
        obj.addProperty("activo", p.isActivo());
        return obj;
    }

    private String getStringFromJson(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString().trim();
        }
        return null;
    }

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
