package api.websocket;

import com.google.gson.Gson;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class NotificacionBroadcaster {

    private static final Gson GSON = new Gson();
    private static final int HISTORIAL_MAXIMO = 20;
    private static final Set<Session> SESSIONS = ConcurrentHashMap.newKeySet();
    private static final List<NotificacionDTO> HISTORIAL = Collections.synchronizedList(new LinkedList<>());

    private NotificacionBroadcaster() {
    }

    static void agregar(Session session) {
        SESSIONS.add(session);
        synchronized (HISTORIAL) {
            for (NotificacionDTO notificacion : HISTORIAL) {
                enviar(session, notificacion);
            }
        }
    }

    static void quitar(Session session) {
        SESSIONS.remove(session);
    }

    public static void enviar(String tipo, String titulo, String mensaje) {
        enviar(new NotificacionDTO(tipo, titulo, mensaje));
    }

    public static void enviar(NotificacionDTO notificacion) {
        synchronized (HISTORIAL) {
            HISTORIAL.add(0, notificacion);
            while (HISTORIAL.size() > HISTORIAL_MAXIMO) {
                HISTORIAL.remove(HISTORIAL.size() - 1);
            }
        }

        for (Session session : SESSIONS) {
            enviar(session, notificacion);
        }
    }

    private static void enviar(Session session, NotificacionDTO notificacion) {
        if (session == null || !session.isOpen()) {
            quitar(session);
            return;
        }
        try {
            session.getBasicRemote().sendText(GSON.toJson(notificacion));
        } catch (IOException ex) {
            quitar(session);
        }
    }
}
