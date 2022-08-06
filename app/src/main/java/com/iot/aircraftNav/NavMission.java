package com.iot.aircraftNav;

import static java.lang.Math.PI;

import java.sql.Struct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.sdk.flightcontroller.FlightController;

/**
 * NavMission 自动巡航任务
 */
public class NavMission {

    private ScheduledFuture future1 , future2 ;
    private double angle;
    private double epsilon = 1e-6;
    private double fast_epsilon = 1e-4;
    private double targetLatitude, targetLongitude;
    private FlightController flightController;
    private Thread t;

    /**
     * NavMission 代表一次自动巡航任务
     * @param flightController 外部注入的飞行控制器
     * @param targetLatitude 目标纬度
     * @param targetLongitude 目标经度
     */
    public NavMission(FlightController flightController ,double targetLatitude, double targetLongitude) {
        LocationCoordinate3D curLoc = flightController.getState().getAircraftLocation();
        this.angle = getDirection(curLoc.getLatitude(), curLoc.getLongitude(), targetLatitude, targetLongitude);
        this.targetLatitude = targetLatitude;
        this.targetLongitude = targetLongitude;
        this.flightController = flightController;
    }

    /**
     * Start 开启自动巡航任务
     * Start 按顺序执行两个任务:
     *      1. 计算目标位置方位角并转向. 这里我们设定好一个足够长的时间让飞机进行转向
     *      2. 飞机飞行至目标地点. 当纬度差距在fast_epsilon之后, 以较快的速度5飞行; 当差距小于fast_epsilon时, 飞机以较低速度1飞行. 当差距在epsilon中时, 判定为达到目标点, 停止飞行.
     *      注意: 我们需要控制速度来达到较好的精确度.
     */
    public void Start() throws Error{
            t = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {

                            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
                            future1 = service.scheduleAtFixedRate(new Runnable() {
                                private int cnt = 0;
                                @Override
                                public void run() {
                                    cnt ++;
                                    flightController.sendVirtualStickFlightControlData(new FlightControlData(0, 0, (float) angle, 0), djiError -> {
                                        if (djiError != null) {
                                            throw  new Error(djiError.getDescription());
                                        }
                                    });

                                    if (cnt == 25 || t.isInterrupted()) {
                                        future1.cancel(true);
                                    }
                                }

                            }, 0, 200, TimeUnit.MILLISECONDS);

                            while (!future1.isDone()) {
                                try {
                                    Thread.sleep(100);
                                }catch (InterruptedException e) {
                                    break;
                                }
                            }

                            ScheduledExecutorService service2 = Executors.newSingleThreadScheduledExecutor();
                            future2 = service2.scheduleAtFixedRate(new Runnable() {
                                @Override
                                public void run() {
                                    if (t.isInterrupted()) {
                                        future2.cancel(true);
                                    }
                                    LocationCoordinate3D curLoc = flightController.getState().getAircraftLocation();
                                    if (Math.abs(curLoc.getLatitude()) - targetLatitude > fast_epsilon) { // 远距离高速行驶
                                        flightController.sendVirtualStickFlightControlData(new FlightControlData(0, 5, (float) angle, 0), djiError -> {
                                            if (djiError != null) {
                                                throw new Error(djiError.getDescription());
                                            }
                                        });
                                    } else { // 近距离低速行驶
                                        if (Math.abs(curLoc.getLatitude()  - targetLatitude) < epsilon) {
                                            future2.cancel(true);
                                        }
                                        flightController.sendVirtualStickFlightControlData(new FlightControlData(0,1,(float) angle, 0), djiError ->{
                                            if (djiError != null) {
                                                throw  new Error(djiError.getDescription());
                                            }
                                        });
                                    }
                                }
                            }, 0, 200, TimeUnit.MILLISECONDS);
                        }
                    }
            );

            t.start();
    }

    public void Stop() {
        t.interrupt();
    }

    /**
     * 计算两个gps坐标之间的方位角(使用WGS84坐标系)
     * 注意, 我们只能处理北半球的情况
     * @param lat1 起点纬度
     * @param lon1 起点经度
     * @param lat2 起点纬度
     * @param lon2 起点经度
     * @return 方位角. [-180,180] 顺时针为正, 逆时针为负
     */
    double getDirection(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        double deltaFI = Math.log(Math.tan(lat2 / 2 + PI / 4) / Math.tan(lat1 / 2 + PI / 4));
        double deltaLON = Math.abs(lon1 - lon2) % 180;
        double theta = Math.atan2(deltaLON, deltaFI);
        if (lon2 < lon1) {
            return -Math.toDegrees(theta);
        }
        return Math.toDegrees(theta);
    }

}
