package org.apache.tomcat.user005;

import javax.websocket.*;

public class WSEndpoint extends Endpoint {
    private WSConnection connection;

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        connection = new WSConnection(session);
        System.out.println("Opened WebSocket session-" + session.getId());
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Closed WebSocket session-" + session.getId() + ", reason: " + closeReason.getCloseCode() + " (" + closeReason.getReasonPhrase() + ")");
        connection.destroy();
        connection = null;
    }

    @Override
    public void onError(Session session, Throwable throwable) {
        System.out.println("Error on WebSocket session-" + session.getId());
        throwable.printStackTrace(System.out);
        connection.destroy();
        connection = null;
    }

    static class WSConnection implements MessageHandler.Whole<String> {
        private final Session session;

        WSConnection(Session session) {
            this.session = session;
            session.addMessageHandler(this);
        }

        void destroy() {
            session.removeMessageHandler(this);
        }

        @Override
        public void onMessage(String message) {
            System.out.println("Session-" + session.getId() + " onMessage(" + message  +")");
        }
    }
}