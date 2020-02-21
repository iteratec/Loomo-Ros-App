package de.iteratec.loomo.ros.publisher;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.util.Log;
import android.util.Pair;
import com.segway.robot.algo.tf.AlgoTfData;
import com.segway.robot.algo.tf.Translation;
import com.segway.robot.sdk.perception.sensor.Sensor;
import geometry_msgs.Quaternion;
import geometry_msgs.Transform;
import geometry_msgs.TransformStamped;
import geometry_msgs.Vector3;
import nav_msgs.Odometry;
import org.ros.message.MessageFactory;
import org.ros.message.Time;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import tf2_msgs.TFMessage;

import java.util.Arrays;
import java.util.List;


public class TFPublisher implements Runnable {

    private static final String TAG = "TFPublisher";

    private Sensor sensorAPI;
    private MessageFactory messageFactory;
    private Publisher<TFMessage> tfMessagePublisher;
    private ConnectedNode connectedNode;
    private OdomHolder odomHolder;
    private SensorHolder sensorHolder;
    private List<String> frameNames;
    private List<Pair<Integer, Integer>> frameIndices;

    public TFPublisher(SensorHolder sensor, MessageFactory mMessageFactory, ConnectedNode connectedNode, OdomHolder odomHolder) {
        this.sensorHolder = sensor;
        this.messageFactory = mMessageFactory;
        this.connectedNode = connectedNode;
        this.tfMessagePublisher = connectedNode.newPublisher("/tf", TFMessage._TYPE);
        this.odomHolder = odomHolder;
        Log.d(TAG, "TF Publisher Created");
         this.frameNames= Arrays.asList(Sensor.WORLD_ODOM_ORIGIN, Sensor.WORLD_EVIO_ORIGIN, Sensor.BASE_ODOM_FRAME, Sensor.BASE_POSE_FRAME,
            Sensor.NECK_POSE_FRAME, Sensor.HEAD_POSE_Y_FRAME, Sensor.HEAD_POSE_P_FRAME, Sensor.RS_DEPTH_FRAME, Sensor.RS_FE_FRAME,
            Sensor.RS_COLOR_FRAME, Sensor.HEAD_POSE_P_R_FRAME,
            Sensor.PLATFORM_CAM_FRAME);
        this.frameIndices= Arrays.asList(new Pair<>(0,1),new Pair<>(1,2), new Pair<>(2,3), new Pair<>(3,4), new Pair<>(4,5),
            new Pair<>(5,6), new Pair<>(6,7), new Pair<>(7,8), new Pair<>(8,9), new Pair<>(9,10)
            , new Pair<>(10,11));
        }

    @Override
    public void run() {
        if (sensorAPI == null) {
            sensorAPI = sensorHolder.getSensor();
        }
        //Log.d(TAG, "run called");

/*
        final List<String> frameNames = Arrays.asList(Sensor.WORLD_ODOM_ORIGIN, Sensor.BASE_POSE_FRAME,
            Sensor.BASE_ODOM_FRAME, Sensor.NECK_POSE_FRAME, Sensor.HEAD_POSE_Y_FRAME,
            Sensor.RS_COLOR_FRAME, Sensor.RS_DEPTH_FRAME, Sensor.HEAD_POSE_P_R_FRAME,
            Sensor.PLATFORM_CAM_FRAME);
            */
        //final List<Pair<Integer, Integer>> frameIndices = Arrays.asList(new Pair<>(0, 1));


/*
        for (int i=1; i<frameNames.size()-1;i++) {
            frameIndices.add(new Pair<>(i,i+1));
        }*/

        if (null != sensorAPI) {
            Time stamp = connectedNode.getCurrentTime();
            if (null != stamp) {
                TFMessage tfMessage = tfMessagePublisher.newMessage();

                Odometry odom = odomHolder.getOdom();
                if (null != odom) {
                    TransformStamped transformStamped = transformOdom(odom);
                    if (!isValidTf(transformStamped)) {
                        return;
                    }
                    tfMessage.getTransforms().add(transformStamped);
                }

                for (Pair<Integer, Integer> index : frameIndices) {
                    String target = frameNames.get(index.first);
                    String source = frameNames.get(index.second);
                    AlgoTfData tfData;
                    try {
                        tfData = sensorAPI.getTfData(source, target, -1, 100);
                    } catch (Exception ex) {
                        Log.d(TAG, "no data from Sensor API");
                        continue;
                    }

                    TransformStamped transformStamped = algoTf2TfStamped(tfData, stamp);
                    if (!isValidTf(transformStamped)) {
                        return;
                    }
                    tfMessage.getTransforms().add(transformStamped);
                }
                String source= Sensor.RS_DEPTH_FRAME;
                String target= "camera_frame";
                AlgoTfData tfData= sensorAPI.getTfData(Sensor.BASE_ODOM_FRAME, Sensor.RS_DEPTH_FRAME,-1,100);
                org.apache.commons.math3.complex.Quaternion q= toQuaternion(PI/2,0,PI/2);
                com.segway.robot.algo.tf.Quaternion rotation=
                    new com.segway.robot.algo.tf.Quaternion((float)q.getQ1(),(float)q.getQ2(),(float)q.getQ3(),(float)q.getQ0());
                tfData.set(target,source,System.currentTimeMillis(),rotation,new Translation(0,0,0),0);
                TransformStamped transformStamped = algoTf2TfStamped(tfData, stamp);
                tfMessage.getTransforms().add(transformStamped);

                String source_laser= Sensor.BASE_POSE_FRAME;
                String target_laser= "laser";
                org.apache.commons.math3.complex.Quaternion q_laser= toQuaternion(0,0,-PI/2);
                com.segway.robot.algo.tf.Quaternion rotation_laser=
                        new com.segway.robot.algo.tf.Quaternion((float)q_laser.getQ1(),(float)q_laser.getQ2(),(float)q_laser.getQ3(),(float)q_laser.getQ0());
                tfData.set(target_laser,source_laser,System.currentTimeMillis(),rotation_laser,new Translation(0.2f,0.2f,0),0);
                TransformStamped transformStamped_laser = algoTf2TfStamped(tfData, stamp);
                tfMessage.getTransforms().add(transformStamped_laser);
                if (tfMessage.getTransforms().size() > 1) {
                    boolean isvalidTf = true;
                    for (TransformStamped tfStamp : tfMessage.getTransforms()) {
                        if (tfStamp.getChildFrameId().equals("")) {
                            //Log.v("TFPublisher", "child_frame_id not set");
                            isvalidTf = false;
                        } else if (tfStamp.getHeader().getFrameId().equals("")) {
                            //Log.v("TFPublisher", "frame_id not set");
                            isvalidTf = false;
                        }
                    }

                    double diff = System.currentTimeMillis() - tfMessage.getTransforms().get(0).getHeader().getStamp().totalNsecs() / 1000000;
                    // Log.v("TFDiff", Double.toString(diff));

                    if (isvalidTf) {
                        //Log.d(TAG, "publishing TF");
                        tfMessagePublisher.publish(tfMessage);
                    } else {
                        Log.d(TAG, "TF not valid");
                    }

                }
                // Log.w(TAG, "run: exit TFPublisher");
            }
        } else {
            Log.d(TAG, "SensorAPI = null");
        }
    }

    private TransformStamped algoTf2TfStamped(AlgoTfData tfData, Time stamp) {
        //creating a correctly stamped tf tree
        Vector3 vector3 = messageFactory.newFromType(Vector3._TYPE);
        vector3.setX(tfData.t.x);
        vector3.setY(tfData.t.y);
        vector3.setZ(tfData.t.z);
        Quaternion quaternion = messageFactory.newFromType(Quaternion._TYPE);
        //trying to normalize quaternion according to https://github.com/introlab/rtabmap_ros/issues/172
        quaternion.setX(tfData.q.x);
        quaternion.setY(tfData.q.y);
        quaternion.setZ(tfData.q.z);
        quaternion.setW(tfData.q.w);
        Transform transform = messageFactory.newFromType(Transform._TYPE);
        transform.setTranslation(vector3);
        transform.setRotation(quaternion);
        TransformStamped transformStamped = messageFactory.newFromType(TransformStamped._TYPE);
        transformStamped.setTransform(transform);
        transformStamped.setChildFrameId(tfData.tgtFrameID);
        transformStamped.getHeader().setFrameId(tfData.srcFrameID);
//        transformStamped.getHeader().setStamp(Time.fromMillis(Utils.platformStampInMillis(stamp)));
        transformStamped.getHeader().setStamp(stamp);

        return transformStamped;
    }

    /**
     * creating a correctly stamped odom message
     *
     * @return transformStamped
     */
    private TransformStamped transformOdom(Odometry odom) {
        TransformStamped transformStamped = messageFactory.newFromType(TransformStamped._TYPE);
        transformStamped.setHeader(odom.getHeader());
        transformStamped.setChildFrameId(odom.getChildFrameId());
//        transformStamped.getHeader().setStamp(Time.fromMillis(Utils.platformStampInMillis(stamp)));
        transformStamped.getHeader().setStamp(connectedNode.getCurrentTime());

        transformStamped.getTransform().getTranslation().setX(odom.getPose().getPose().getPosition().getX());
        transformStamped.getTransform().getTranslation().setY(odom.getPose().getPose().getPosition().getY());
        transformStamped.getTransform().getTranslation().setZ(odom.getPose().getPose().getPosition().getZ());

        transformStamped.getTransform().setRotation(odom.getPose().getPose().getOrientation());

        return transformStamped;
    }

    /**
     * the method to check if the tf is valid
     *
     * @param tf stamp to transform
     * @return
     */
    private boolean isValidTf(TransformStamped tf) {
        Quaternion quaternion = messageFactory.newFromType(Quaternion._TYPE);
        quaternion.setX(0.);
        quaternion.setY(0.);
        quaternion.setZ(0.);
        quaternion.setW(0.);
        int countnotzero = 0;
        if (tf.getTransform().getRotation().getW() != 0.0) countnotzero++;
        if (tf.getTransform().getRotation().getW() != 0.0) countnotzero++;
        if (tf.getTransform().getRotation().getW() != 0.0) countnotzero++;
        if (tf.getTransform().getRotation().getW() != 0.0) countnotzero++;
        //a quaternion cant be valid with less than 2 values set
        return (countnotzero >= 2);
    }

    org.apache.commons.math3.complex.Quaternion toQuaternion(double pitch, double roll, double yaw) {

        // Abbreviations for the various angular functions
        double cy = cos(yaw * 0.5);
        double sy = sin(yaw * 0.5);
        double cr = cos(roll * 0.5);
        double sr = sin(roll * 0.5);
        double cp = cos(pitch * 0.5);
        double sp = sin(pitch * 0.5);

        double w = cy * cr * cp + sy * sr * sp;
        double x = cy * sr * cp - sy * cr * sp;
        double y = cy * cr * sp + sy * sr * cp;
        double z = sy * cr * cp - cy * sr * sp;

        org.apache.commons.math3.complex.Quaternion q = new org.apache.commons.math3.complex.Quaternion(w, x, y, z);
        return q;
    }

}
