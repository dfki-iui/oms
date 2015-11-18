package de.dfki.oms.websocket;

/**
 * Created by crishushu on 30/10/15.
 */

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.util.concurrent.TimeUnit;

/**
 * Wrapper for the Websocket instance.
 */
public class SocketServlet extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(TimeUnit.MINUTES.toMillis(120));
        factory.register(OMSWebSocket.class);
    }
}