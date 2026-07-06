package api.productos;

import api.common.ApiResponse;
import api.config.RMIClientFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import rmi.dto.CatalogoProductoDTO;
import rmi.productos.CatalogoServiceRMI;

@WebServlet(name = "CatalogoApiServlet", urlPatterns = {"/api/catalogo", "/api/catalogo/*"})
public class CatalogoApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            CatalogoServiceRMI catalogoService = RMIClientFactory.getCatalogoService();
            if ("/estadisticas".equals(request.getPathInfo())) {
                Map<String, Object> estadisticas = new LinkedHashMap<>();
                int total = catalogoService.contarTotal();
                estadisticas.put("total", total);
                estadisticas.put("activos", total);
                estadisticas.put("laboratorios", catalogoService.contarLaboratorios());
                estadisticas.put("ultimoAgregado", catalogoService.obtenerUltimoAgregado());
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Estadisticas de catalogo", estadisticas)));
                return;
            }

            Long id = obtenerId(request);
            if (id != null) {
                CatalogoProductoDTO producto = catalogoService.buscarPorId(id);
                if (producto == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(gson.toJson(ApiResponse.error("Producto no encontrado")));
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Producto encontrado", producto)));
                return;
            }

            String termino = request.getParameter("q");
            int pagina = parseInt(request.getParameter("pagina"), 1);
            int porPagina = parseInt(request.getParameter("porPagina"), 20);
            if (termino != null && !termino.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(ApiResponse.ok("Productos encontrados", catalogoService.buscarParaAutocomplete(termino, porPagina))));
                return;
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("productos", catalogoService.listarPaginado(pagina, porPagina));
            data.put("total", catalogoService.contarTotal());
            data.put("pagina", pagina);
            data.put("porPagina", porPagina);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Catalogo encontrado", data)));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try {
            CatalogoProductoDTO producto = gson.fromJson(request.getReader(), CatalogoProductoDTO.class);
            CatalogoServiceRMI catalogoService = RMIClientFactory.getCatalogoService();
            Long id = catalogoService.insertar(producto);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Producto creado", catalogoService.buscarPorId(id))));
        } catch (JsonSyntaxException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error("JSON invalido")));
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try {
            Long id = obtenerId(request);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Id requerido")));
                return;
            }
            CatalogoProductoDTO producto = gson.fromJson(request.getReader(), CatalogoProductoDTO.class);
            producto.setId(id);
            CatalogoServiceRMI catalogoService = RMIClientFactory.getCatalogoService();
            catalogoService.actualizar(producto);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(ApiResponse.ok("Producto actualizado", catalogoService.buscarPorId(id))));
        } catch (JsonSyntaxException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error("JSON invalido")));
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try {
            Long id = obtenerId(request);
            if (id == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(ApiResponse.error("Id requerido")));
                return;
            }
            boolean ok = RMIClientFactory.getCatalogoService().desactivar(id);
            response.setStatus(ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(ok ? ApiResponse.ok("Producto desactivado", null) : ApiResponse.error("Producto no encontrado")));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(ApiResponse.error(ex.getMessage())));
        }
    }

    private Long obtenerId(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            return null;
        }
        try {
            return Long.valueOf(pathInfo.substring(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
