package rmi.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ResumenVentasDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int cantidadVentas;
    private BigDecimal totalGeneral;
    private BigDecimal totalEfectivo;
    private BigDecimal totalYapePlin;
    private BigDecimal totalTarjeta;
    private BigDecimal totalMixto;
    private BigDecimal promedioVenta;

    public ResumenVentasDTO() {
        this.totalGeneral = BigDecimal.ZERO;
        this.totalEfectivo = BigDecimal.ZERO;
        this.totalYapePlin = BigDecimal.ZERO;
        this.totalTarjeta = BigDecimal.ZERO;
        this.totalMixto = BigDecimal.ZERO;
        this.promedioVenta = BigDecimal.ZERO;
    }

    public int getCantidadVentas() { return cantidadVentas; }
    public void setCantidadVentas(int cantidadVentas) { this.cantidadVentas = cantidadVentas; }

    public BigDecimal getTotalGeneral() { return totalGeneral; }
    public void setTotalGeneral(BigDecimal totalGeneral) { this.totalGeneral = totalGeneral; }

    public BigDecimal getTotalEfectivo() { return totalEfectivo; }
    public void setTotalEfectivo(BigDecimal totalEfectivo) { this.totalEfectivo = totalEfectivo; }

    public BigDecimal getTotalYapePlin() { return totalYapePlin; }
    public void setTotalYapePlin(BigDecimal totalYapePlin) { this.totalYapePlin = totalYapePlin; }

    public BigDecimal getTotalTarjeta() { return totalTarjeta; }
    public void setTotalTarjeta(BigDecimal totalTarjeta) { this.totalTarjeta = totalTarjeta; }

    public BigDecimal getTotalMixto() { return totalMixto; }
    public void setTotalMixto(BigDecimal totalMixto) { this.totalMixto = totalMixto; }

    public BigDecimal getPromedioVenta() { return promedioVenta; }
    public void setPromedioVenta(BigDecimal promedioVenta) { this.promedioVenta = promedioVenta; }
}
