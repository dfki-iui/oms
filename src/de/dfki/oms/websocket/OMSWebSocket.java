package de.dfki.oms.websocket;

import de.dfki.adom.rest.APIObservable;
import de.dfki.adom.rest.APIObserver;
import de.dfki.oms.adom.ADOMContainer;
import de.dfki.oms.history.OMMVersionManager;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by crishushu on 30/10/15.
 */
@WebSocket
public class OMSWebSocket {

    private static Session session;

    private static Map<String, APIObserver> registeredMemories = new HashMap();

    // called when the socket connection with the browser is established
    @OnWebSocketConnect
    public void handleConnect(Session s) {

        session = s;
        System.out.println("session: " + session + " is connected");

        System.out.println("Add api listener:");
        for (String mem : OMMVersionManager.getAvailableMemories()) {
            APIObservable api = ADOMContainer.getAdom(mem).getAPI();
            if (!registeredMemories.containsKey(mem)) {
                APIObserver listener = new ADOMListener(mem);
                System.out.println(mem + ":" + listener);
                api.addObserver(listener);
                registeredMemories.put(mem, listener);
            }
        }
    }

    // called when the connection closed
    @OnWebSocketClose
    public void handleClose(int statusCode, String reason) {

        System.out.println("Connection closed with statusCode="
                + statusCode + ", reason=" + reason);

        System.out.println("Remove api listener:");
        for (String mem : registeredMemories.keySet()) {
            APIObservable api = ADOMContainer.getAdom(mem).getAPI();
            System.out.println(mem + ":" + api);
            api.removeObserver(registeredMemories.get(mem));
        }

        registeredMemories.clear();
    }

    // called when a message received from the browser
    @OnWebSocketMessage
    public void handleMessage(String message) {
        switch (message) {
            case "start":
                // prevents from timeout exception
                // executor.scheduleAtFixedRate(() -> send("ping"), 0, 300, TimeUnit.SECONDS);
                break;
            case "pong": System.out.println("pong");
                break;
            case "stop":
                this.stop();
                break;
            default:
                System.out.println("received event with message: " + message);
        }
    }

    // called in case of an error1
    @OnWebSocketError
    public void handleError(Throwable error) {
        error.printStackTrace();
    }

    // sends message to browser
    public static void send(String message) {
        try {
            if (session.isOpen()) {
                session.getRemote().sendString(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {

            System.err.println("Websocket session not initialized");
        }
    }

    // closes the socket
    private void stop() {
        try {
            session.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

