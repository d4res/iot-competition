package com.iot.aircraftNav;
import android.util.Log;


import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Locale;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;


public class WsConn extends WebSocketClient {

    private final MainActivity m;
    public WsConn(URI serverURI, MainActivity m) {super(serverURI); this.m = m;}

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d("[WS-CLOSE]", String.format("id: %d; reason: %s", code, reason) );
        m.logger.add("[WS]", String.format(Locale.CHINA, "close. id: %d; reason: %s", code, reason));
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONArray data = new JSONArray(message);
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                double latitude = obj.getDouble("latitude");
                double longitude = obj.getDouble("longitude");
                m.logger.add("[WS]",String.format(Locale.CHINA, "receive gps: %f %f", latitude, longitude));
                location loc = new location(latitude, longitude);
//                m.latitudeET.setText(String.valueOf(latitude));
//                m.longitudeET.setText(String.valueOf(longitude));
                m.workQueue.add(loc);
            }

//            m.fc.NavQueue(m.workQueue);
        } catch (Exception e ) {
            m.logger.add("[WS]", e.getMessage());
        }
    }

    @Override
    public void onMessage(ByteBuffer message) {
        //System.out.println("received ByteBuffer");
    }


    @Override
    public void onError(Exception ex) {
        m.logger.add("[WS]", ex.getMessage());
    }

}
