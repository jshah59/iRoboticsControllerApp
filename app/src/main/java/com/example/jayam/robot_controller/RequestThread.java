package com.example.jayam.robot_controller;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RequestThread extends Thread {

    private static String targetURL = "ws://192.168.4.1:80";
    private static int WAIT_TIME = 150;

    private static String authCode = "VRobotDay";


    private WebSocket ws;
    private OkHttpClient client = new OkHttpClient();

    private void openConnection(String target) {
        Request request = new Request.Builder()
                .url(target)
                .build();



        ws = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                //System.out.println("Opened!");
                super.onOpen(webSocket, response);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                //Log.i("WSResponse", text);
                if (text.equals("Not Authenticated")) {
                    ws.send(authCode);
                }

            }
        });

        ws.send(authCode);
    }

    public void run() {
        openConnection(targetURL);

        while (true) {
            try {
                Thread.sleep(WAIT_TIME);
                //Log.i("WebSocket", "----- Start message -----");
                String temp = makeRequest("M1") + makeRequest("M2");
                ws.send(temp);
                ws.send(makeRequest("S2"));
                //Log.i("WebSocket", "----- End Message -----");

            } catch (Exception e) {
                //System.out.println("Something broke");
                e.printStackTrace();
            }
        }
    }

    private String makeRequest(String key) {
        StringBuilder request = new StringBuilder(key + ":");

        int val = MainActivity.parameters.get(key);
        if (val >= 0) {
            request.append("+");
        } else {
            request.append("-");
        }

        if (Math.abs(val) < 100) {
            request.append("0");
        }

        if (Math.abs(val) < 10) {
            request.append("0");
        }

        request.append(Integer.toString(Math.abs(val)));
        request.append(";");

        //Log.i("WebSocket", request.toString());
        return request.toString();
    }
}
