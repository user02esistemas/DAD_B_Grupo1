package DTO;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO para Últimas Ventas
 * Representa una venta reciente con sus datos básicos
 */
public class UltimaVentaDTO {

    private String numero;
    private Timestamp fecha;
    private BigDecimal total;
    private String metodoPago;
    private String usuario;

    // Constructores
    public UltimaVentaDTO() {
    }

    public UltimaVentaDTO(String numero, Timestamp fecha, BigDecimal total, String metodoPago, String usuario) {
        this.numero = numero;
        this.fecha = fecha;
        this.total = total;
        this.metodoPago = metodoPago;
        this.usuario = usuario;
    }

    // =====================================================
    //              MÉTODOS DE CONVERSIÓN
    // =====================================================

    /**
     * Convertir de Map a DTO
     */
    public static UltimaVentaDTO fromMap(Map<String, Object> map) {
        UltimaVentaDTO dto = new UltimaVentaDTO();
        dto.setNumero((String) map.get("numero"));
        dto.setFecha((Timestamp) map.get("fecha"));
        dto.setTotal((BigDecimal) map.get("total"));
        dto.setMetodoPago((String) map.get("metodoPago"));
        dto.setUsuario((String) map.get("usuario"));
        return dto;
    }

    /**
     * Convertir lista de Maps a lista de DTOs
     */
    public static List<UltimaVentaDTO> fromMapList(List<Map<String, Object>> mapList) {
        List<UltimaVentaDTO> lista = new ArrayList<>();
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
        map.put("numero", numero);
        map.put("fecha", fecha);
        map.put("total", total);
        map.put("metodoPago", metodoPago);
        map.put("usuario", usuario);
        return map;
    }

    // =====================================================
    //              MÉTODOS DE FORMATO
    // =====================================================

    /**
     * Obtener fecha formateada (dd/MM/yyyy)
     */
    public String getFechaFormateada() {
        if (fecha == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(fecha);
    }

    /**
     * Obtener hora formateada (HH:mm)
     */
    public String getHoraFormateada() {
        if (fecha == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        return sdf.format(fecha);
    }

    /**
     * Obtener fecha y hora formateada (dd/MM/yyyy HH:mm)
     */
    public String getFechaHoraFormateada() {
        if (fecha == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(fecha);
    }

    /**
     * Obtener tiempo transcurrido (hace X minutos, hace X horas)
     */
    public String getTiempoTranscurrido() {
        if (fecha == null) return "";
        long diff = System.currentTimeMillis() - fecha.getTime();
        long minutos = diff / (1000 * 60);
        long horas = diff / (1000 * 60 * 60);
        long dias = diff / (1000 * 60 * 60 * 24);

        if (minutos < 1) return "Hace un momento";
        if (minutos < 60) return "Hace " + minutos + " min";
        if (horas < 24) return "Hace " + horas + " hora" + (horas > 1 ? "s" : "");
        return "Hace " + dias + " día" + (dias > 1 ? "s" : "");
    }

    /**
     * Obtener descripción del método de pago
     */
    public String getMetodoPagoDescripcion() {
        if (metodoPago == null) return "";
        switch (metodoPago) {
            case "EFECTIVO": return "Efectivo";
            case "YAPE_PLIN": return "Yape/Plin";
            case "TARJETA": return "Tarjeta";
            case "MIXTO": return "Mixto";
            default: return metodoPago;
        }
    }

    /**
     * Obtener clase CSS para el badge del método de pago
     */
    public String getMetodoPagoBadgeClass() {
        if (metodoPago == null) return "bg-secondary";
        switch (metodoPago) {
            case "EFECTIVO": return "bg-success";
            case "YAPE_PLIN": return "bg-purple";
            case "TARJETA": return "bg-info";
            case "MIXTO": return "bg-warning text-dark";
            default: return "bg-secondary";
        }
    }

    // =====================================================
    //              GETTERS Y SETTERS
    // =====================================================

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return "UltimaVentaDTO{" +
                "numero='" + numero + '\'' +
                ", fecha=" + fecha +
                ", total=" + total +
                ", metodoPago='" + metodoPago + '\'' +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}
