package com.iot.aircraftNav;
import android.util.Log;


import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;


public class    WsConn extends WebSocketClient {

    private MainActivity m;
    public WsConn(URI serverURI, MainActivity m) {super(serverURI); this.m = m;}

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        // System.out.println("new connection opened");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d("[WS-CLOSE]", String.format("id: %d; reason: %s", code, reason) );
        m.showToast("ws close. code: " + code + " reason: " + reason);
    }

    @Override
    public void onMessage(String message) {
        m.showToast(message);
        Log.d("[MSG]", message);
        try {
            JSONObject obj = new JSONObject(message);
            double latitude = obj.getDouble("latitude");
            double longitude = obj.getDouble("longitude");
            m.setTargetLatitude(latitude);
            m.setTargetLongitude(longitude);
            m.latitudeET.setText(String.valueOf(latitude));
            m.longitudeET.setText(String.valueOf(longitude));
            m.showToast(latitude + " " + longitude) ;

            Log.d("[MSG]", "json " + latitude + " " + longitude);
        } catch (Exception e ) {
            m.showToast("error parsing message");
            Log.d("[MSG]", e.getMessage());
        }
    }

    @Override
    public void onMessage(ByteBuffer message) {
        //System.out.println("received ByteBuffer");
    }


    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
    }

}
