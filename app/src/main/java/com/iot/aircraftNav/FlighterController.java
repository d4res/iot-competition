package com.iot.aircraftNav;

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

    public void StopNav() {
        navMission.Stop();
        log("navMission stop manually");

    }

}

