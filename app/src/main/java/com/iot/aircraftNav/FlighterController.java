package com.iot.aircraftNav;

import org.java_websocket.client.WebSocketClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Queue;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class FlighterController {
    private dji.sdk.flightcontroller.FlightController flightController;
    private final logger logger;
    private final boolean isAvailable ;
    private  boolean isVSOn;
    NavMission navMission;
    private HttpURLConnection connection = null;
    private final String url = "http://81.68.245.247:8888/arrive";

    private void log(String msg) {
        logger.add("[FC]", msg);
    }

    public FlighterController(logger logger) {
        this.logger = logger;
        Aircraft aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();
        if (null == aircraft || !aircraft.isConnected()) {
            log("can not get the flighter");
            isAvailable = false;
        } else {
            flightController = aircraft.getFlightController();
            flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
            flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
            flightController.setYawControlMode(YawControlMode.ANGLE);
            flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
            isAvailable = true;
           log("flighter connected");
        }
        isVSOn = false;

        try {
            URL _url = new URL(url);
            this.connection = (HttpURLConnection) _url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(50000);
            connection.setReadTimeout(50000);
        } catch (Exception e) {
            log(e.getMessage());
        }
    }

    public boolean IsAvailable() {
        return isAvailable;
    }


    public void TakeOff() {
        flightController.startTakeoff(
                new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            log(djiError.getDescription());
                        } else {
                            log("take off success");
                        }
                    }
                }
        );
    }

    public void GoHome() {
        flightController.startGoHome(djiError -> {
            if (djiError != null) {
                log(djiError.getDescription());
            } else {
                log( "start go home");
            }
        });
    }

    public void EnableVS() {
        if (!isVSOn) {
            flightController.setVirtualStickModeEnabled(true, djiError -> {
                if (djiError != null) {
                    log(djiError.getDescription());
                } else {
                    isVSOn = true;
                    log("enable virtual stick success");
                }
            });
        }
    }

    public void DisableVS(){
        if (isVSOn) {
            flightController.setVirtualStickModeEnabled(false, djiError -> {
                if (djiError != null) {
                    log(djiError.getDescription());
                } else {
                    isVSOn = false;
                    log("Disable virtual stick success");
                }
            });
        } else {
            log("already ");
        }
    }

    public void AutoNav(double targetLatitude, double targetLongitude) {
        this.EnableVS();
        navMission = new NavMission(flightController, targetLatitude, targetLongitude);
        try {
            navMission.Start();
            log("start auto nav mission. target: "+ targetLatitude+","+targetLongitude);
        } catch (Error e) {
            log(e.getMessage());
        }
    }

    public void NavQueue(Queue<location> workQueue) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while(!workQueue.isEmpty()) {
                            location work = workQueue.poll();
                            navMission = new NavMission(flightController, work.latitude, work.longitude);

                            log("before mission start");
                            navMission.Start();
                            log("end mission");

                            try {
                                InputStream in = connection.getInputStream();
                                in.close();
                            } catch (IOException e) {
                                log(e.getMessage());
                            }
                            log(String.format("arrive: %f %f",work.latitude, work.longitude));
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                log(e.getMessage());
                            }
                        }
                        log("all work is done");
                    }
                }
        ).start();
    }

    public void StopNav() {
        log("navMission stop manually{under construction}");
    }

}

