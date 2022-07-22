package com.iot.aircraftNav;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import static java.lang.Math.PI;

import org.java_websocket.client.WebSocketClient;

import dji.common.error.DJIError;
import dji.common.flightcontroller.Attitude;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.realname.AircraftBindingState;
import dji.common.realname.AppActivationState;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.realname.AppActivationManager;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    protected Button loginBtn;
    protected Button logoutBtn;
    protected Button attitudeBtn;
    protected Button enableBtn;
    protected Button takeoffBtn;
    protected Button disableBtn;
    protected Button northBtn;
    protected Button southBtn;
    protected Button westBtn;
    protected Button eastBtn;
    protected Button flyNavBtn;
    protected Button cfmBtn;
    protected Button homeBtn;
    protected Button locBtn;
    protected Button connectBtn;
    protected EditText latitudeET;
    protected EditText longitudeET;
    protected TextView bindingStateTV;
    protected TextView appActivationStateTV;
    private AppActivationManager appActivationManager;
    private AppActivationState.AppActivationStateListener activationStateListener;
    private AircraftBindingState.AircraftBindingStateListener bindingStateListener;
    private FlightController flightController;
    private Timer timer;

    private static final String url = "ws://81.68.245.247:8888/ws";

    private WebSocketClient client;

    public void setTargetLatitude(double targetLatitude) {
        this.targetLatitude = targetLatitude;
    }

    private double targetLatitude = 0;

    public void setTargetLongitude(double targetLongitude) {
        this.targetLongitude = targetLongitude;
    }

    private double targetLongitude = 0;


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
        initTimer();
    }

    private void initTimer() {
        if (timer == null) {
            timer = new Timer();

        }
    }

    private void initFlighterController() {
        Aircraft aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();
        if (null == aircraft || !aircraft.isConnected()) {
            showToast("cannot get flighter");
            return;
        } else {
            flightController = aircraft.getFlightController();
            flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);

            flightController.setYawControlMode(YawControlMode.ANGLE);
            flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
            flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
            showToast("flighter init");
        }
     }

//     private void initTimer() {
//        timer = new Timer();
//     }

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
        bindingStateTV = (TextView) findViewById(R.id.tv_binding_state_info);
        appActivationStateTV = (TextView) findViewById(R.id.tv_activation_state_info);
//        loginBtn = (Button) findViewById(R.id.btn_login);
//        logoutBtn = (Button) findViewById(R.id.btn_logout);
        attitudeBtn = (Button) findViewById(R.id.btn_attitude);

        enableBtn = (Button) findViewById(R.id.btn_enable);
        takeoffBtn = (Button) findViewById(R.id.btn_takeoff);
        disableBtn = (Button) findViewById(R.id.btn_disable);
        southBtn = (Button) findViewById(R.id.btn_south);
        northBtn = (Button) findViewById(R.id.btn_north);
        westBtn = (Button) findViewById(R.id.btn_west);
        eastBtn = (Button) findViewById(R.id.btn_east);
        flyNavBtn = (Button) findViewById(R.id.btn_nav);
        cfmBtn = (Button) findViewById(R.id.btn_cfm);
        homeBtn = (Button) findViewById(R.id.btn_home);
        locBtn = (Button) findViewById(R.id.btn_location);
        connectBtn = (Button)findViewById(R.id.btn_connect);
        latitudeET = (EditText) findViewById(R.id.et_latitude);
        longitudeET = (EditText) findViewById(R.id.et_longitude);

//        loginBtn.setOnClickListener(this);
//        logoutBtn.setOnClickListener(this);
        attitudeBtn.setOnClickListener(this);
        enableBtn.setOnClickListener(this);
        takeoffBtn.setOnClickListener(this);
        disableBtn.setOnClickListener(this);
        southBtn.setOnClickListener(this);
        northBtn.setOnClickListener(this);
        westBtn.setOnClickListener(this);
        eastBtn.setOnClickListener(this);
        flyNavBtn.setOnClickListener(this);
        cfmBtn.setOnClickListener(this);
        homeBtn.setOnClickListener(this);
        locBtn.setOnClickListener(this);
        connectBtn.setOnClickListener(this) ;

    }

    private void initData(){
        setUpListener();
        appActivationManager = DJISDKManager.getInstance().getAppActivationManager();
        if (appActivationManager != null) {
            appActivationManager.addAppActivationStateListener(activationStateListener);
            appActivationManager.addAircraftBindingStateListener(bindingStateListener);
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appActivationStateTV.setText("" + appActivationManager.getAppActivationState());
                    bindingStateTV.setText("" + appActivationManager.getAircraftBindingState());
                }
            });
        }
    }

    private void setUpListener() {
        // Example of Listener
        activationStateListener = new AppActivationState.AppActivationStateListener() {
            @Override
            public void onUpdate(final AppActivationState appActivationState) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appActivationStateTV.setText("" + appActivationState);
                    }
                });
            }
        };

        bindingStateListener = bindingState -> MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bindingStateTV.setText("" + bindingState);
            }
        });
    }

    private void tearDownListener() {
        if (activationStateListener != null) {
            appActivationManager.removeAppActivationStateListener(activationStateListener);
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appActivationStateTV.setText("Unknown");
                }
            });
        }
        if (bindingStateListener !=null) {
            appActivationManager.removeAircraftBindingStateListener(bindingStateListener);
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bindingStateTV.setText("Unknown");
                }
            });
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
//            case R.id.btn_login:
//                loginAccount();
//                break;
//            case R.id.btn_logout:
//                logoutAccount();
//                break;
            case R.id.btn_attitude:
                if (flightController != null) {
                    Attitude att = flightController.getState().getAttitude();
                    showToast("Attitude: " + "yaw: " + att.yaw + " roll: " + att.roll + " pitch: " + att.pitch);

                    LocationCoordinate3D curLoc = flightController.getState().getAircraftLocation();
                    if (flightController != null) {
                        double dirction = getDirection(curLoc.getLatitude(), curLoc.getLongitude(), targetLatitude, targetLongitude);
                        if (timer != null) {
                            timer.schedule(new TimerTask() {
                                int cnt = 0;
                                @Override
                                public void run() {
                                    cnt ++;

                                    flightController.sendVirtualStickFlightControlData(new FlightControlData(0, 0, (float) dirction, 0), djiError -> {
                                        if (djiError != null) {
                                            showToast(djiError.getDescription());
                                        }
                                    });

                                    if (cnt == 25) {
                                        cancel();
                                    }
                                }
                            }, 0, 200);
                        }

                    }
                }
                break;
            case R.id.btn_enable:
                if (flightController != null){

                    flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null){
                                showToast(djiError.getDescription());
                            }else
                            {
                                showToast("Enable Virtual Stick Success");
                            }
                        }
                    });

                } else {
                    showToast("missing flight");
                }
                break;
            case R.id.btn_takeoff:
                if (flightController != null) {
                    flightController.startTakeoff(
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    if (djiError != null) {
                                        showToast(djiError.getDescription());
                                    } else {
                                        showToast("take off success");
                                    }
                                }
                            }
                    );
                } else {
                    showToast("missing flight");
                }
                break;
            case R.id.btn_disable:
                if (flightController != null) {
                    flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            } else {
                                showToast("Disable virtual stick success");
                            }
                        }
                    });
                } else {
                    showToast("missing flight");
                }
                break;
            case R.id.btn_north:
                if (timer != null) {
                    timer.schedule(new TimerTask() {
                        private  int cnt= 0;

                        @Override
                        public void run() {
                            Attitude att = flightController.getState().getAttitude();
                            flightController.sendVirtualStickFlightControlData(new FlightControlData(0,3,(float) att.yaw, 0), djiError -> {
                                cnt ++;
                                if (djiError != null) {
                                    showToast(djiError.getDescription());
                                }
                                if (cnt == 10) {
                                    cancel();
                                }
                            });
                        }
                    }, 0, 200);
                }
                break;
            case R.id.btn_south:
                if (timer != null) {
                    timer.schedule(new sendCmd(DIRECTION.SOUTH, 10,10), 0, 200);
                }
                break;
            case R.id.btn_west:
                if (timer != null) {
                    timer.schedule(new sendCmd(DIRECTION.WEST, 10, 10), 0,  200 );
                }
                break;

            case R.id.btn_east:
                if (timer != null) {
                    timer.schedule(new sendCmd(DIRECTION.EAST, 10, 10), 0, 200 );
                }
                break;
            case R.id.btn_nav:
                LocationCoordinate3D loc = flightController.getState().getAircraftLocation();
                double angle = getDirection(loc.getLatitude(), loc.getLongitude(), targetLatitude, targetLongitude);
                flyInDir f = new flyInDir(angle);
                f.Start();
                break;
            case R.id.btn_cfm:
                float _latitude = Float.parseFloat(latitudeET.getText().toString());
                float _longitude = Float.parseFloat(longitudeET.getText().toString());
                if (_latitude > 0 && _longitude > 0) {
                    targetLatitude = _latitude;
                    targetLongitude = _longitude;
                    showToast("get: "+  targetLongitude + " " + targetLatitude);

                }
                break;
            case R.id.btn_home:
                if (flightController != null) {
                    flightController.startGoHome(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            } else {
                                showToast("arrive at home now");
                            }
                        }
                    });
                }
                break;
            case R.id.btn_location:
                if (flightController!=null) {
                    LocationCoordinate3D curLoc =  flightController.getState().getAircraftLocation();
                    latitudeET.setText(String.valueOf(curLoc.getLatitude()));
                    longitudeET.setText(String.valueOf(curLoc.getLongitude()));
                    showToast("loc: " + "latitude: " + curLoc.getLatitude() + " longitude: " + curLoc.getLongitude() );
                }
                break;
            case R.id.btn_connect:
                try {
                    client = new WsConn(new URI(url), this);
                    client.connectBlocking();
                    showToast("connect success");
                } catch (Exception e) {
                    showToast("connect error");
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private void loginAccount(){
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        showToast("Login Success");
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        showToast("Login Error:"
                                + error.getDescription());
                    }
                });
    }

    private void logoutAccount(){
        UserAccountManager.getInstance().logoutOfDJIUserAccount(error -> {
            if (null == error) {
                showToast("Logout Success");
            } else {
                showToast("Logout Error:"
                        + error.getDescription());
            }
        });
    }

    enum DIRECTION {
        NORTH,
        SOUTH,
        WEST,
        EAST
    }

    class sendCmd extends TimerTask {
        private int limit = 10 ;
        private float pitch = 0, roll = 0;
        private float speed;

        public sendCmd(DIRECTION DIR, int limit, int speed) {
            if (limit > 0) {
                this.limit = limit;
            }

            if (speed > 0) {
                this.speed = speed;
            }

            switch (DIR) {
                case NORTH:
                    roll = speed;
                    break;
                case SOUTH:
                    roll = -speed;
                    break;
                case EAST:
                    pitch = speed;
                    break;
                case WEST:
                    pitch = -speed;
                    break;
            }
        }
        private  int cnt = 0;
        @Override
        public void run() {
            if (flightController != null) {
                cnt ++;
                flightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                pitch, roll, 0, 0
                        ), djiError -> {
                            if (djiError != null) {
                                showToast(djiError.getDescription());
                            }
                        }
                );

                if (cnt == limit) {
                    LocationCoordinate3D curLocation = flightController.getState().getAircraftLocation();
                    showToast("location: " + "latitude: " + curLocation.getLatitude() + " longitude: " + curLocation.getLongitude());
                    cancel();
                }
            } else {
                showToast("missing flight");
                cancel();
            }
         }
    }


    class straightFlight {
        private ScheduledFuture future;
        private double pitch = 0;
        private double roll = 0;
        private double esp = 1e-5;
        private double target = 0;
        private  DIRECTION dir;

        public straightFlight(DIRECTION DIR, double target) {
            float speed  = 3;
            switch (DIR) {
                case NORTH:
                    roll = speed;
                    break;
                case SOUTH:
                    roll = -speed;
                    break;
                case EAST:
                    pitch = speed;
                    break;
                case WEST:
                    pitch = -speed;
                    break;
            }
            this.target = target;
            this.dir = DIR;
        }

        public void Start() {


            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            future = service.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (flightController != null) {


                        flightController.sendVirtualStickFlightControlData(
                                new FlightControlData(
                                        (float)pitch, (float) roll, 0, 0
                                ), djiError -> {
                                    if (djiError != null) {
                                        showToast(djiError.getDescription());
                                    }
                                }
                        );

                        LocationCoordinate3D curLocation = flightController.getState().getAircraftLocation();

                        if (dir == DIRECTION.NORTH || dir == DIRECTION.SOUTH) {
                            if (Math.abs(curLocation.getLatitude() - target) < esp) {
                                future.cancel(true);
                            }
                        } else {
                            if (Math.abs(curLocation.getLongitude() - target) < esp) {
                                future.cancel(true);
                            }
                        }
                    } else {
                        showToast("missing flight");
                        future.cancel(true);
                    }
                }
            }, 100, 200, TimeUnit.MILLISECONDS);
        }

        public ScheduledFuture Future() {
            return future;
        }
    }


    class flyInDir {
        private ScheduledFuture future1 , future2 ;
        private double angle;

        public flyInDir(double angle) {
            this.angle = angle;
        }
        public void Start() {
            Thread t = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {

                            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
                            future1 = service.scheduleAtFixedRate(new Runnable() {
                                private int cnt = 0;

                                @Override
                                public void run() {

                                    cnt++;

                                    flightController.sendVirtualStickFlightControlData(new FlightControlData(0, 0, (float) angle, 0), djiError -> {
                                        if (djiError != null) {
                                            showToast(djiError.getDescription());
                                        }
                                    });

                                    if (cnt == 25) {
                                        future1.cancel(true);
                                    }
                                }

                            }, 0, 200, TimeUnit.MILLISECONDS);

                            while (!future1.isDone()) {
                            }
                            ScheduledExecutorService service2 = Executors.newSingleThreadScheduledExecutor();
                            future2 = service2.scheduleAtFixedRate(new Runnable() {
                                @Override
                                public void run() {

                                    flightController.sendVirtualStickFlightControlData(new FlightControlData(0, 3, (float) angle, 0), djiError -> {
                                        if (djiError != null) {
                                            showToast(djiError.getDescription());
                                        }
                                        LocationCoordinate3D curLoc = flightController.getState().getAircraftLocation();
                                        if (Math.abs(curLoc.getLatitude() - targetLatitude) < 1e-5) {
                                            future2.cancel(true);

                                        }
                                    });
                                }
                            }, 100, 200, TimeUnit.MILLISECONDS);
                        }
                    }
            );
            t.start();

        }
    }


    double getDirection(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        double deltaFI = Math.log(Math.tan(lat2 / 2 + PI / 4) / Math.tan(lat1 / 2 + PI / 4));
        double deltaLON = Math.abs(lon1 - lon2) % 180;
        double theta = Math.atan2(deltaLON, deltaFI);
        return Math.toDegrees(theta);
    }


}
