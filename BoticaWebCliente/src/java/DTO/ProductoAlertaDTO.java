package DTO;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO para Alertas de Productos
 * Representa un producto con alerta (stock bajo o por vencer)
 */
public class ProductoAlertaDTO {

    private Long id;
    private String nombre;
    private String lote;
    private int stockActual;
    private int stockMinimo;
    private Date fechaVencimiento;
    private int diasRestantes;
    private String tipoAlerta; // STOCK_BAJO, AGOTADO, POR_VENCER, VENCIDO

    // Constructores
    public ProductoAlertaDTO() {
    }

    public ProductoAlertaDTO(String nombre, String lote, int stockActual, int stockMinimo) {
        this.nombre = nombre;
        this.lote = lote;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.tipoAlerta = stockActual == 0 ? "AGOTADO" : "STOCK_BAJO";
    }

    public ProductoAlertaDTO(String nombre, String lote, Date fechaVencimiento, int diasRestantes, int stockActual) {
        this.nombre = nombre;
        this.lote = lote;
        this.fechaVencimiento = fechaVencimiento;
        this.diasRestantes = diasRestantes;
        this.stockActual = stockActual;
        this.tipoAlerta = diasRestantes < 0 ? "VENCIDO" : "POR_VENCER";
    }

    // =====================================================
    //              MÉTODOS DE CONVERSIÓN
    // =====================================================

    /**
     * Convertir de Map (stock bajo) a DTO
     */
    public static ProductoAlertaDTO fromStockBajoMap(Map<String, Object> map) {
        ProductoAlertaDTO dto = new ProductoAlertaDTO();
        dto.setNombre((String) map.get("nombre"));
        dto.setLote((String) map.get("lote"));
        dto.setStockActual((Integer) map.get("stockActual"));
        dto.setStockMinimo((Integer) map.get("stockMinimo"));
        dto.setTipoAlerta(dto.getStockActual() == 0 ? "AGOTADO" : "STOCK_BAJO");
        return dto;
    }

    /**
     * Convertir de Map (por vencer) a DTO
     */
    public static ProductoAlertaDTO fromPorVencerMap(Map<String, Object> map) {
        ProductoAlertaDTO dto = new ProductoAlertaDTO();
        dto.setNombre((String) map.get("nombre"));
        dto.setLote((String) map.get("lote"));
        dto.setFechaVencimiento((Date) map.get("fechaVencimiento"));
        dto.setDiasRestantes((Integer) map.get("diasRestantes"));
        dto.setStockActual((Integer) map.get("stockActual"));
        dto.setTipoAlerta(dto.getDiasRestantes() < 0 ? "VENCIDO" : "POR_VENCER");
        return dto;
    }

    /**
     * Convertir lista de Maps (stock bajo) a lista de DTOs
     */
    public static List<ProductoAlertaDTO> fromStockBajoMapList(List<Map<String, Object>> mapList) {
        List<ProductoAlertaDTO> lista = new ArrayList<>();
        if (mapList != null) {
            for (Map<String, Object> map : mapList) {
                lista.add(fromStockBajoMap(map));
            }
        }
        return lista;
    }

    /**
     * Convertir lista de Maps (por vencer) a lista de DTOs
     */
    public static List<ProductoAlertaDTO> fromPorVencerMapList(List<Map<String, Object>> mapList) {
        List<ProductoAlertaDTO> lista = new ArrayList<>();
        if (mapList != null) {
            for (Map<String, Object> map : mapList) {
                lista.add(fromPorVencerMap(map));
            }
        }
        return lista;
    }

    /**
     * Convertir DTO a Map (para JSON)
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", id);
        map.put("nombre", nombre);
        map.put("lote", lote);
        map.put("stockActual", stockActual);
        map.put("stockMinimo", stockMinimo);
        map.put("fechaVencimiento", fechaVencimiento);
        map.put("diasRestantes", diasRestantes);
        map.put("tipoAlerta", tipoAlerta);
        return map;
    }

    // =====================================================
    //              MÉTODOS DE ESTADO
    // =====================================================

    /**
     * Verificar si es alerta de stock
     */
    public boolean esAlertaStock() {
        return "STOCK_BAJO".equals(tipoAlerta) || "AGOTADO".equals(tipoAlerta);
    }

    /**
     * Verificar si es alerta de vencimiento
     */
    public boolean esAlertaVencimiento() {
        return "POR_VENCER".equals(tipoAlerta) || "VENCIDO".equals(tipoAlerta);
    }

    /**
     * Obtener nivel de criticidad (1-4)
     * 4 = Más crítico
     */
    public int getNivelCriticidad() {
        switch (tipoAlerta) {
            case "VENCIDO": return 4;
            case "AGOTADO": return 4;
            case "POR_VENCER":
                if (diasRestantes <= 7) return 3;
                if (diasRestantes <= 15) return 2;
                return 1;
            case "STOCK_BAJO":
                if (stockActual <= 3) return 3;
                return 2;
            default: return 0;
        }
    }

    /**
     * Obtener clase CSS para badge según criticidad
     */
    public String getBadgeClass() {
        switch (tipoAlerta) {
            case "VENCIDO":
            case "AGOTADO":
                return "bg-danger";
            case "POR_VENCER":
                if (diasRestantes <= 7) return "bg-danger";
                if (diasRestantes <= 15) return "bg-warning text-dark";
                return "bg-info";
            case "STOCK_BAJO":
                if (stockActual <= 3) return "bg-danger";
                return "bg-warning text-dark";
            default:
                return "bg-secondary";
        }
    }

    /**
     * Obtener descripción del tipo de alerta
     */
    public String getDescripcionAlerta() {
        switch (tipoAlerta) {
            case "VENCIDO": return "Producto vencido";
            case "AGOTADO": return "Sin stock";
            case "POR_VENCER": return "Vence en " + diasRestantes + " días";
            case "STOCK_BAJO": return "Stock bajo (" + stockActual + "/" + stockMinimo + ")";
            default: return "";
        }
    }

    /**
     * Obtener fecha de vencimiento formateada
     */
    public String getFechaVencimientoFormateada() {
        if (fechaVencimiento == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(fechaVencimiento);
    }

    // =====================================================
    //              GETTERS Y SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public int getDiasRestantes() {
        return diasRestantes;
    }

    public void setDiasRestantes(int diasRestantes) {
        this.diasRestantes = diasRestantes;
    }

    public String getTipoAlerta() {
        return tipoAlerta;
    }

    public void setTipoAlerta(String tipoAlerta) {
        this.tipoAlerta = tipoAlerta;
    }

    @Override
    public String toString() {
        return "ProductoAlertaDTO{" +
                "nombre='" + nombre + '\'' +
                ", lote='" + lote + '\'' +
                ", tipoAlerta='" + tipoAlerta + '\'' +
                ", stockActual=" + stockActual +
                ", diasRestantes=" + diasRestantes +
                '}';
    }
}
