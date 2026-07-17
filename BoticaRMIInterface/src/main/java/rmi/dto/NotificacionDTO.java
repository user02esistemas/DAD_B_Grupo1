package rmi.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificacionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String tipo;
    private final String titulo;
    private final String mensaje;
    private final String fecha;

    public NotificacionDTO(String tipo, String titulo, String mensaje) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getFecha() {
        return fecha;
    }
}
