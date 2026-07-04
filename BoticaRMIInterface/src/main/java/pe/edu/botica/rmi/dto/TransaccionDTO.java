package pe.edu.botica.rmi.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class TransaccionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Long TIPO_COMPRA = 1L;
    public static final Long TIPO_VENTA = 2L;

    private Long id;
    private Long tipoTransaccionId;
    private String numeroTransaccion;
    private Long usuarioId;
    private Long sesionCajaId;
    private Long proveedorId;
    private String nombrePersona;
    private Timestamp fecha;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String metodoPago;
    private String estado;
    private String observaciones;
    private BigDecimal montoEfectivo;
    private BigDecimal montoVirtual;
    private String medioPagoVirtual;
    private BigDecimal vuelto;
    private String tipoComprobante;
    private String tipoTransaccionNombre;
    private String usuarioNombre;
    private ProveedorDTO proveedor;
    private UsuarioDTO usuario;
    private List<DetalleTransaccionDTO> detalles;

    public TransaccionDTO() {
        this.subtotal = BigDecimal.ZERO;
        this.igv = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.metodoPago = "EFECTIVO";
        this.estado = "COMPLETADA";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTipoTransaccionId() { return tipoTransaccionId; }
    public void setTipoTransaccionId(Long tipoTransaccionId) { this.tipoTransaccionId = tipoTransaccionId; }

    public String getNumeroTransaccion() { return numeroTransaccion; }
    public void setNumeroTransaccion(String numeroTransaccion) { this.numeroTransaccion = numeroTransaccion; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getSesionCajaId() { return sesionCajaId; }
    public void setSesionCajaId(Long sesionCajaId) { this.sesionCajaId = sesionCajaId; }

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

    public String getNombrePersona() { return nombrePersona; }
    public void setNombrePersona(String nombrePersona) { this.nombrePersona = nombrePersona; }

    public String getCliente() { return nombrePersona; }
    public void setCliente(String cliente) { this.nombrePersona = cliente; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public BigDecimal getMontoEfectivo() { return montoEfectivo; }
    public void setMontoEfectivo(BigDecimal montoEfectivo) { this.montoEfectivo = montoEfectivo; }

    public BigDecimal getMontoVirtual() { return montoVirtual; }
    public void setMontoVirtual(BigDecimal montoVirtual) { this.montoVirtual = montoVirtual; }

    public String getMedioPagoVirtual() { return medioPagoVirtual; }
    public void setMedioPagoVirtual(String medioPagoVirtual) { this.medioPagoVirtual = medioPagoVirtual; }

    public BigDecimal getVuelto() { return vuelto; }
    public void setVuelto(BigDecimal vuelto) { this.vuelto = vuelto; }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getTipoTransaccionNombre() { return tipoTransaccionNombre; }
    public void setTipoTransaccionNombre(String tipoTransaccionNombre) { this.tipoTransaccionNombre = tipoTransaccionNombre; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public ProveedorDTO getProveedor() { return proveedor; }
    public void setProveedor(ProveedorDTO proveedor) { this.proveedor = proveedor; }

    public UsuarioDTO getUsuario() { return usuario; }
    public void setUsuario(UsuarioDTO usuario) { this.usuario = usuario; }

    public List<DetalleTransaccionDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleTransaccionDTO> detalles) { this.detalles = detalles; }

    public boolean esCompra() { return TIPO_COMPRA.equals(this.tipoTransaccionId); }
    public boolean esVenta() { return TIPO_VENTA.equals(this.tipoTransaccionId); }

    public void calcularTotales() {
        if (detalles == null || detalles.isEmpty()) {
            this.subtotal = BigDecimal.ZERO;
            this.igv = BigDecimal.ZERO;
            this.total = BigDecimal.ZERO;
            return;
        }
        BigDecimal suma = detalles.stream()
            .map(DetalleTransaccionDTO::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.subtotal = suma;
        this.igv = suma.multiply(new BigDecimal("0.18")).setScale(2, BigDecimal.ROUND_HALF_UP);
        this.total = suma.add(this.igv);
    }
}
