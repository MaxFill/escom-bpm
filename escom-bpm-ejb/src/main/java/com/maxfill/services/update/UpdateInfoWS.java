package com.maxfill.services.update;

import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

@WebSocket
public class UpdateInfoWS{
    private static final Logger LOG = Log.getLogger(UpdateInfoWS.class);

    private String result;

    @OnWebSocketConnect
    public void onConnect(Session sess) {
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason)  {
    }

    @OnWebSocketError
    public void onError(Throwable cause){
        LOG.warn(cause);
    }

    @OnWebSocketMessage
    public void onMessage(String msg){
        result = msg;
        LOG.info("onMessage() - {}", msg);
    }

    public String getResult() {
        return result;
    }
}
