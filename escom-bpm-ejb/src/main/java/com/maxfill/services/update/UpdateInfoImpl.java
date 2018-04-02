package com.maxfill.services.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import javax.ejb.Stateless;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Сервис получения по wss информации о текущем релизе
 */
@Stateless
public class UpdateInfoImpl implements UpdateInfo{
    private static final Logger LOG = Logger.getLogger(UpdateInfoImpl.class.getName());

    /**
     * Выполняет подключение к wss серверу и возвращает информацио о текущем релизе
     * @param licenseNumber
     * @param uri
     * @return
     */
    @Override
    public Map<String, String> start(String licenseNumber, String uri){
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setTrustAll(true);

        WebSocketClient client = new WebSocketClient(sslContextFactory);
        Map<String,String> result = new HashMap <>();
        Session session = null;
        try {
            client.start();
            UpdateInfoWS socket = new UpdateInfoWS();
            Future<Session> fut = client.connect(socket, URI.create(uri));
            fut.get(5, TimeUnit.SECONDS);
            session = fut.get();
            session.getRemote().sendStringByFuture(licenseNumber);
            fut.get(2, TimeUnit.SECONDS);
            String actualReleasesJSON = socket.getResult();
            ObjectMapper objectMapper = new ObjectMapper();
            result = objectMapper.readValue(actualReleasesJSON, HashMap.class);
        } catch (Throwable ex) {
            LOG.info(ex.getMessage());
        } finally {
            if (session != null) {
                session.close(StatusCode.NORMAL, "");
            }
        }
        return result;
    }
}
