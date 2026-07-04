package DTO;

import DAO.MovimientoInventarioDAO;
import java.sql.Timestamp;
import java.util.List;

/**
 * DTO para Movimientos de Inventario
 */
public class MovimientoInventarioDTO {

    private Long id;
    private Long productoId;
    private String tipoMovimiento;  // ENTRADA, SALIDA, AJUSTE
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private String motivo;
    private Long referenciaId;
    private String referenciaTipo;  // TRANSACCION, AJUSTE
    private Long usuarioId;
    private Timestamp fechaMovimiento;

    // Campos adicionales para mostrar
    private String usuarioNombre;
    private String productoNombre;
    private String lote;

    // Constructor
    public MovimientoInventarioDTO() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(Integer stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public Integer getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(Integer stockNuevo) {
        this.stockNuevo = stockNuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public String getReferenciaTipo() {
        return referenciaTipo;
    }

    public void setReferenciaTipo(String referenciaTipo) {
        this.referenciaTipo = referenciaTipo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Timestamp getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(Timestamp fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    // =====================================================
    //              MÉTODOS DE NEGOCIO (DTO)
    // =====================================================

    /**
     * Registrar movimiento
     */
    public boolean registrar() {
        Long idGenerado = new MovimientoInventarioDAO().registrar(this);
        if (idGenerado != null) {
            this.id = idGenerado;
            return true;
        }
        return false;
    }

    /**
     * Realizar ajuste de inventario
     */
    public static boolean realizarAjuste(Long productoId, int nuevoStock, String motivo, Long usuarioId) {
        return new MovimientoInventarioDAO().realizarAjuste(productoId, nuevoStock, motivo, usuarioId);
    }

    /**
     * Listar movimientos por producto
     */
    public static List<MovimientoInventarioDTO> listarPorProducto(Long productoId, int pagina, int porPagina) {
        return new MovimientoInventarioDAO().listarPorProducto(productoId, pagina, porPagina);
    }

    /**
     * Listar todos los movimientos
     */
    public static List<MovimientoInventarioDTO> listarTodos(int pagina, int porPagina) {
        return new MovimientoInventarioDAO().listarTodos(pagina, porPagina);
    }

    /**
     * Buscar movimientos
     */
    public static List<MovimientoInventarioDTO> buscar(String termino, int pagina, int porPagina) {
        return new MovimientoInventarioDAO().buscar(termino, pagina, porPagina);
    }

    /**
     * Contar total de movimientos
     */
    public static int contarTodos() {
        return new MovimientoInventarioDAO().contarTodos();
    }

    /**
     * Contar movimientos por búsqueda
     */
    public static int contarBusqueda(String termino) {
        return new MovimientoInventarioDAO().contarBusqueda(termino);
    }

    /**
     * Listar movimientos recientes
     */
    public static List<MovimientoInventarioDTO> listarRecientes(int limite) {
        return new MovimientoInventarioDAO().listarRecientes(limite);
    }

    /**
     * Contar movimientos por producto
     */
    public static int contarPorProducto(Long productoId) {
        return new MovimientoInventarioDAO().contarPorProducto(productoId);
    }

    // =====================================================
    //              MÉTODOS AUXILIARES
    // =====================================================

    /**
     * Obtener icono según tipo de movimiento
     */
    public String getIcono() {
        if (tipoMovimiento == null) return "bi-circle";
        switch (tipoMovimiento) {
            case "ENTRADA": return "bi-arrow-down-circle-fill text-success";
            case "SALIDA": return "bi-arrow-up-circle-fill text-danger";
            case "AJUSTE": return "bi-gear-fill text-warning";
            default: return "bi-circle";
        }
    }

    /**
     * Obtener clase CSS según tipo de movimiento
     */
    public String getClaseCSS() {
        if (tipoMovimiento == null) return "";
        switch (tipoMovimiento) {
            case "ENTRADA": return "text-success";
            case "SALIDA": return "text-danger";
            case "AJUSTE": return "text-warning";
            default: return "";
        }
    }

    /**
     * Obtener descripción del tipo
     */
    public String getTipoDescripcion() {
        if (tipoMovimiento == null) return "-";
        switch (tipoMovimiento) {
            case "ENTRADA": return "Entrada";
            case "SALIDA": return "Salida";
            case "AJUSTE": return "Ajuste";
            default: return tipoMovimiento;
        }
    }

    /**
     * Obtener badge del tipo
     */
    public String getBadgeClass() {
        if (tipoMovimiento == null) return "bg-secondary";
        switch (tipoMovimiento) {
            case "ENTRADA": return "bg-success";
            case "SALIDA": return "bg-danger";
            case "AJUSTE": return "bg-warning text-dark";
            default: return "bg-secondary";
        }
    }
}
