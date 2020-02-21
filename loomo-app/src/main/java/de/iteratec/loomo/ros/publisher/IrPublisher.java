package de.iteratec.loomo.ros.publisher;

import com.segway.robot.sdk.perception.sensor.InfraredData;
import com.segway.robot.sdk.perception.sensor.Sensor;
import com.segway.robot.sdk.perception.sensor.UltrasonicData;
import org.ros.message.MessageFactory;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import sensor_msgs.LaserScan;

import static java.lang.Math.PI;


public class IrPublisher implements Runnable {

    private static final String TAG = "IrPublisher";

    private Sensor mSensor;
    private MessageFactory mMessageFactory;
    private Publisher<LaserScan> mIRPubr;
    private ConnectedNode mConnectedNode;
    private SensorHolder sensorHolder;

    private long current = 0;

    public IrPublisher(SensorHolder sensor, MessageFactory mMessageFactory, ConnectedNode connectedNode) {
        this.sensorHolder = sensor;
        this.mMessageFactory = mMessageFactory;
        this.mConnectedNode = connectedNode;
        this.mIRPubr = connectedNode.newPublisher("/loomo/ir_scan", LaserScan._TYPE);
    }

    //remains unused for now
    @Override
    public void run() {
        if (mSensor == null) {
            this.mSensor = sensorHolder.getSensor();
        }
        if (null != mSensor) {

            InfraredData data_inf = mSensor.getInfraredDistance();
            if (current == 0 || data_inf.getTimestamp() - current > 200) {

                current = data_inf.getTimestamp();
                UltrasonicData data_ult = mSensor.getUltrasonicDistance();
                float[] data = {data_inf.getLeftDistance() / 1000.0f, data_ult.getDistance() / 1000.0f, data_inf.getRightDistance() / 1000.0f};
                float[] intensity = {100.f, 100.f, 100.f};

                LaserScan scan = mMessageFactory.newFromType(LaserScan._TYPE);
                scan.getHeader().setStamp(mConnectedNode.getCurrentTime());
                scan.getHeader().setFrameId(Sensor.RS_DEPTH_FRAME);

                float[] angle = {-15.0f, 0.0f, 15.0f};
                scan.setAngleMin(angle[0] * (float) PI / 180.0f);
                scan.setAngleMax(angle[2] * (float) PI / 180.0f);
                scan.setAngleIncrement((angle[2] - angle[0]) * (float) PI / 180.0f);
                scan.setTimeIncrement((1 / 400.0f) / (2));
                scan.setRangeMin(0.3f);
                scan.setRangeMax(1.5f);
                scan.setRanges(data);
                scan.setIntensities(intensity);

                mIRPubr.publish(scan);
            }
        }
        // Log.d(TAG, "run: exit IRPublisherThread");
    }

}
