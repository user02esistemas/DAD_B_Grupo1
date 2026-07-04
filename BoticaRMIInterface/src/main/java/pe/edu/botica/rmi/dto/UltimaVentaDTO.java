package pe.edu.botica.rmi.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class UltimaVentaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String numeroTransaccion;
    private String cliente;
    private BigDecimal total;
    private String fecha;
    private String vendedor;

    public UltimaVentaDTO() {}

    public String getNumeroTransaccion() { return numeroTransaccion; }
    public void setNumeroTransaccion(String numeroTransaccion) { this.numeroTransaccion = numeroTransaccion; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }
}
