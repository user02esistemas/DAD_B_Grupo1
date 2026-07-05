package rmi.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class VentaDiaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fecha;
    private BigDecimal total;
    private int cantidad;

    public VentaDiaDTO() {}

    public VentaDiaDTO(String fecha, BigDecimal total, int cantidad) {
        this.fecha = fecha;
        this.total = total;
        this.cantidad = cantidad;
    }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
