package DTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO para Productos Vendidos (Top productos)
 * Representa un producto con su cantidad vendida y total
 */
public class ProductoVendidoDTO {

    private String nombre;
    private int cantidad;
    private BigDecimal total;

    // Constructores
    public ProductoVendidoDTO() {
    }

    public ProductoVendidoDTO(String nombre, int cantidad, BigDecimal total) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.total = total;
    }

    // =====================================================
    //              MÉTODOS DE CONVERSIÓN
    // =====================================================

    /**
     * Convertir de Map a DTO
     */
    public static ProductoVendidoDTO fromMap(Map<String, Object> map) {
        ProductoVendidoDTO dto = new ProductoVendidoDTO();
        dto.setNombre((String) map.get("nombre"));
        dto.setCantidad((Integer) map.get("cantidad"));
        dto.setTotal((BigDecimal) map.get("total"));
        return dto;
    }

    /**
     * Convertir lista de Maps a lista de DTOs
     */
    public static List<ProductoVendidoDTO> fromMapList(List<Map<String, Object>> mapList) {
        List<ProductoVendidoDTO> lista = new ArrayList<>();
        if (mapList != null) {
            for (Map<String, Object> map : mapList) {
                lista.add(fromMap(map));
            }
        }
        return lista;
    }

    /**
     * Convertir DTO a Map (para JSON)
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("nombre", nombre);
        map.put("cantidad", cantidad);
        map.put("total", total);
        return map;
    }

    // =====================================================
    //              MÉTODOS DE CÁLCULO
    // =====================================================

    /**
     * Obtener precio promedio por unidad
     */
    public BigDecimal getPrecioPromedio() {
        if (cantidad == 0) return BigDecimal.ZERO;
        return total.divide(new BigDecimal(cantidad), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Obtener nombre truncado (para gráficos)
     */
    public String getNombreTruncado(int maxLength) {
        if (nombre == null) return "";
        if (nombre.length() <= maxLength) return nombre;
        return nombre.substring(0, maxLength - 3) + "...";
    }

    // =====================================================
    //              GETTERS Y SETTERS
    // =====================================================

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "ProductoVendidoDTO{" +
                "nombre='" + nombre + '\'' +
                ", cantidad=" + cantidad +
                ", total=" + total +
                '}';
    }
}
