package pe.edu.botica.rmi.dto;

import java.io.Serializable;
import java.sql.Timestamp;

public class CatalogoProductoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String codigoProducto;
    private String nombreComercial;
    private String principioActivo;
    private String concentracion;
    private String formaFarmaceutica;
    private String presentacion;
    private String laboratorio;
    private String registroSanitario;
    private Integer cantidad;
    private String nombreTitular;
    private String nombreFabricante;
    private boolean activo;
    private Timestamp createdAt;

    public CatalogoProductoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getPrincipioActivo() { return principioActivo; }
    public void setPrincipioActivo(String principioActivo) { this.principioActivo = principioActivo; }

    public String getConcentracion() { return concentracion; }
    public void setConcentracion(String concentracion) { this.concentracion = concentracion; }

    public String getFormaFarmaceutica() { return formaFarmaceutica; }
    public void setFormaFarmaceutica(String formaFarmaceutica) { this.formaFarmaceutica = formaFarmaceutica; }

    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }

    public String getLaboratorio() { return laboratorio; }
    public void setLaboratorio(String laboratorio) { this.laboratorio = laboratorio; }

    public String getRegistroSanitario() { return registroSanitario; }
    public void setRegistroSanitario(String registroSanitario) { this.registroSanitario = registroSanitario; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getNombreTitular() { return nombreTitular; }
    public void setNombreTitular(String nombreTitular) { this.nombreTitular = nombreTitular; }

    public String getNombreFabricante() { return nombreFabricante; }
    public void setNombreFabricante(String nombreFabricante) { this.nombreFabricante = nombreFabricante; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getDescripcionCompleta() {
        StringBuilder sb = new StringBuilder(nombreComercial != null ? nombreComercial : "");
        if (concentracion != null && !concentracion.isEmpty()) sb.append(" ").append(concentracion);
        if (formaFarmaceutica != null && !formaFarmaceutica.isEmpty()) sb.append(" - ").append(formaFarmaceutica);
        return sb.toString();
    }

    public String getDescripcionCorta() {
        StringBuilder sb = new StringBuilder(nombreComercial != null ? nombreComercial : "");
        if (principioActivo != null && !principioActivo.isEmpty()) sb.append(" (").append(principioActivo).append(")");
        if (concentracion != null && !concentracion.isEmpty()) sb.append(" ").append(concentracion);
        return sb.toString();
    }
}
