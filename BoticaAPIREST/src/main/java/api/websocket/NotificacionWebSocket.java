package api.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/notificaciones")
public class NotificacionWebSocket {

    @OnOpen
    public void onOpen(Session session) {
        NotificacionBroadcaster.agregar(session);
    }

    @OnClose
    public void onClose(Session session) {
        NotificacionBroadcaster.quitar(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        NotificacionBroadcaster.quitar(session);
    }
}
