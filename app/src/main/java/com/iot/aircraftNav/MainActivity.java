package com.iot.aircraftNav;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;

import org.java_websocket.client.WebSocketClient;

import dji.common.error.DJIError;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.realname.AircraftBindingState;
import dji.common.util.CommonCallbacks;
import dji.sdk.products.Aircraft;
import dji.sdk.realname.AppActivationManager;
import dji.sdk.sdkmanager.DJISDKManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

   protected Button enableBtn;
   protected Button disableBtn;
   protected Button takeoffBtn;
    protected Button flyNavBtn;
    protected Button cfmBtn;
    protected Button homeBtn;
    protected Button locBtn;
    protected Button connectBtn;
    protected Button stopBtn;

    protected EditText latitudeET;
    protected EditText longitudeET;
    protected TextView deviceTV;
    protected TextView wsTV;
    protected TextView logTV;

    private AppActivationManager appActivationManager;
    private AircraftBindingState.AircraftBindingStateListener bindingStateListener;
    private dji.sdk.flightcontroller.FlightController flightController;
    private FlighterController fc;

    protected logger logger;

    private NavMission navMission;

    private static final String url = "ws://81.68.245.247:8888/ws/aircraft";
    private WebSocketClient client;

    public void setTargetLatitude(double targetLatitude) {
        this.targetLatitude = targetLatitude;
    }
    private double targetLatitude = 0;

    public void setTargetLongitude(double targetLongitude) {
        this.targetLongitude = targetLongitude;
    }
    private double targetLongitude = 0;

    public Queue<location> workQueue = new LinkedList<location>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initData();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        setUpListener();
        super.onResume();
        initFlighterController();
        fc = new FlighterController(logger);
    }


    private void initFlighterController() {
        Aircraft aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();
        if (null == aircraft || !aircraft.isConnected()) {
            logger.add("[FLY]","can not get the flighter");
            return;
        } else {
            flightController = aircraft.getFlightController();
            flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
            flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
            flightController.setYawControlMode(YawControlMode.ANGLE);
            flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
            logger.add("[FLY]", "flighter connected");
        }
     }



    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        tearDownListener();
        super.onDestroy();
    }

    private void initUI(){

        enableBtn = (Button) findViewById(R.id.btn_enable);
        disableBtn = (Button)findViewById(R.id.btn_disable);
        takeoffBtn = (Button) findViewById(R.id.btn_takeoff);
        flyNavBtn = (Button) findViewById(R.id.btn_nav);
        cfmBtn = (Button) findViewById(R.id.btn_cfm);
        homeBtn = (Button) findViewById(R.id.btn_home);
        locBtn = (Button) findViewById(R.id.btn_location);
        connectBtn = (Button)findViewById(R.id.btn_connect);
        stopBtn = (Button)findViewById(R.id.btn_stop);

        latitudeET = (EditText) findViewById(R.id.et_latitude);
        longitudeET = (EditText) findViewById(R.id.et_longitude);

        wsTV = (TextView) findViewById(R.id.tv_ws);
        deviceTV = (TextView)  findViewById(R.id.tv_device);
        logTV = (TextView) findViewById(R.id.tv_log);
        logTV.setMovementMethod(ScrollingMovementMethod.getInstance());

        enableBtn.setOnClickListener(this);
        disableBtn.setOnClickListener(this);
        takeoffBtn.setOnClickListener(this);
        flyNavBtn.setOnClickListener(this);
        cfmBtn.setOnClickListener(this);
        homeBtn.setOnClickListener(this);
        locBtn.setOnClickListener(this);
        connectBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        logger = new logger(logTV);
    }

    private void initData(){

        appActivationManager = DJISDKManager.getInstance().getAppActivationManager();
        if (appActivationManager != null) {
            appActivationManager.addAircraftBindingStateListener(bindingStateListener);
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    }

    private void setUpListener() {
        bindingStateListener = bindingState -> MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceTV.setText("" + bindingState);
            }
        });
    }

    private void tearDownListener() {
        if (bindingStateListener !=null) {
            appActivationManager.removeAircraftBindingStateListener(bindingStateListener);
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceTV.setText("unknown");
                }
            });
        }
    }

    private boolean checkFlight()  {
        if (flightController == null) {
            deviceTV.setText("disconnected");
            return false;
        } else {
            deviceTV.setText("connected");
            return true;
        }
    }
    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_stop:
                fc.StopNav();
                logger.add("[NAV]", "stop manually");
                break;
            case R.id.btn_enable:
                fc.EnableVS();
                break;
            case R.id.btn_disable:
                fc.DisableVS();
                break;
            case R.id.btn_takeoff:
                fc.TakeOff();
                break;
            case R.id.btn_nav:
                //fc.AutoNav(targetLatitude, targetLongitude);
                fc.NavQueue(workQueue);
                break;
            case R.id.btn_cfm:
                float _latitude = Float.parseFloat(latitudeET.getText().toString());
                float _longitude = Float.parseFloat(longitudeET.getText().toString());
                if (_latitude > 0 && _longitude > 0) {
                    targetLatitude = _latitude;
                    targetLongitude = _longitude;
                    logger.add("[NAV]", "set target: "+targetLatitude+","+targetLongitude);
                }
                break;
            case R.id.btn_home:
                fc.GoHome();
                break;
            case R.id.btn_location:
                if (flightController!=null) {
                    LocationCoordinate3D curLoc =  flightController.getState().getAircraftLocation();
                    latitudeET.setText(String.valueOf(curLoc.getLatitude()));
                    longitudeET.setText(String.valueOf(curLoc.getLongitude()));
                    logger.add("[FC]", "location: " + curLoc.getLatitude() + "," + curLoc.getLongitude());
                }
                break;
            case R.id.btn_connect:
                try {
                    if (client != null) {
                        client.closeBlocking();
                    }
                    client = new WsConn(new URI(url), this);
                    client.connectBlocking();
                    showToast("connect success");
                    wsTV.setText("connect");
                } catch (Exception e) {
                    showToast("connect error");
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
