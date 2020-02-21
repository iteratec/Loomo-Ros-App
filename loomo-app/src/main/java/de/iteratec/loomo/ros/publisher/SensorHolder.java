package de.iteratec.loomo.ros.publisher;


import com.segway.robot.sdk.perception.sensor.Sensor;

/**
 * Created by maximilian on 27.08.18.
 */

public class SensorHolder {
    private Sensor sensor = null;

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

}
