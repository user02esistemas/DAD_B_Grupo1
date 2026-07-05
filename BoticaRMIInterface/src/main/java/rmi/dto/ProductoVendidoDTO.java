package rmi.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ProductoVendidoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombreProducto;
    private int cantidadVendida;
    private BigDecimal totalVendido;

    public ProductoVendidoDTO() {}

    public ProductoVendidoDTO(String nombreProducto, int cantidadVendida, BigDecimal totalVendido) {
        this.nombreProducto = nombreProducto;
        this.cantidadVendida = cantidadVendida;
        this.totalVendido = totalVendido;
    }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidadVendida() { return cantidadVendida; }
    public void setCantidadVendida(int cantidadVendida) { this.cantidadVendida = cantidadVendida; }

    public BigDecimal getTotalVendido() { return totalVendido; }
    public void setTotalVendido(BigDecimal totalVendido) { this.totalVendido = totalVendido; }
}
