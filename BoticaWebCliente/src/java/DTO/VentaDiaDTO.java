package DTO;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO para Ventas por Día
 * Representa el total de ventas de un día específico
 */
public class VentaDiaDTO {

    private Date fecha;
    private BigDecimal total;
    private int cantidad;

    // Constructores
    public VentaDiaDTO() {
    }

    public VentaDiaDTO(Date fecha, BigDecimal total) {
        this.fecha = fecha;
        this.total = total;
    }

    public VentaDiaDTO(Date fecha, BigDecimal total, int cantidad) {
        this.fecha = fecha;
        this.total = total;
        this.cantidad = cantidad;
    }

    // =====================================================
    //              MÉTODOS DE CONVERSIÓN
    // =====================================================

    /**
     * Convertir de Map a DTO
     */
    public static VentaDiaDTO fromMap(Map<String, Object> map) {
        VentaDiaDTO dto = new VentaDiaDTO();
        dto.setFecha((Date) map.get("fecha"));
        dto.setTotal((BigDecimal) map.get("total"));
        if (map.containsKey("cantidad")) {
            dto.setCantidad((Integer) map.get("cantidad"));
        }
        return dto;
    }

    /**
     * Convertir lista de Maps a lista de DTOs
     */
    public static List<VentaDiaDTO> fromMapList(List<Map<String, Object>> mapList) {
        List<VentaDiaDTO> lista = new ArrayList<>();
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
        map.put("fecha", fecha);
        map.put("total", total);
        map.put("cantidad", cantidad);
        return map;
    }

    // =====================================================
    //              MÉTODOS DE FORMATO
    // =====================================================

    /**
     * Obtener fecha formateada (dd/MM)
     */
    public String getFechaCorta() {
        if (fecha == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM");
        return sdf.format(fecha);
    }

    /**
     * Obtener fecha formateada (dd/MM/yyyy)
     */
    public String getFechaCompleta() {
        if (fecha == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(fecha);
    }

    /**
     * Obtener nombre del día de la semana
     */
    public String getDiaSemana() {
        if (fecha == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE", new java.util.Locale("es", "PE"));
        return sdf.format(fecha);
    }

    /**
     * Obtener nombre corto del día (Lun, Mar, etc.)
     */
    public String getDiaSemanaCorto() {
        if (fecha == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE", new java.util.Locale("es", "PE"));
        return sdf.format(fecha);
    }

    // =====================================================
    //              GETTERS Y SETTERS
    // =====================================================

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    @Override
    public String toString() {
        return "VentaDiaDTO{" +
                "fecha=" + fecha +
                ", total=" + total +
                ", cantidad=" + cantidad +
                '}';
    }
}
