package de.iteratec.loomo.ros.publisher;


import android.util.Log;
import com.segway.robot.algo.Pose2D;
import com.segway.robot.sdk.locomotion.sbv.AngularVelocity;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.locomotion.sbv.LinearVelocity;
import com.segway.robot.sdk.perception.sensor.Sensor;
import nav_msgs.Odometry;
import org.apache.commons.math3.complex.Quaternion;
import org.ros.message.MessageFactory;
import org.ros.message.Time;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * note: sometimes the orientation of the odometry is wrong. So far only restarting the entire robot fixes this.
 * Maybe some solution can be found here
 */
public class OdometryPublisher implements Runnable {
    long last = 0L;
    private static final String TAG = "OdometryPublisher";

    private MessageFactory mMessageFactory;
    private Publisher<Odometry> mOdomPubr;
    private ConnectedNode mConnectedNode;
    private OdomHolder odomHolder;
    private BaseHolder baseHolder;
    private int navigationDataSource;
    private Base base;
    Odometry odom;
    Pose2D initialpose;
    Pose2D origin;
    private boolean resetOdomonStart = true;
    private double angVel;
    private double linVel;
    public OdometryPublisher(MessageFactory mMessageFactory, ConnectedNode connectedNode, OdomHolder odomHolder, BaseHolder baseHolder, int navigationDataSource) {
        setmMessageFactory(mMessageFactory);
        setmConnectedNode(connectedNode);
        setmOdomPubr(connectedNode, navigationDataSource);
        this.navigationDataSource = Base.NAVIGATION_SOURCE_TYPE_ODOM;
        setOdomHolder(odomHolder);
        setBaseHolder(baseHolder);
        Log.d(TAG, "Odom Publisher Created");
        this.base = baseHolder.getmBase();


        //base.setNavigationDataSource(Base.NAVIGATION_SOURCE_TYPE_ODOM);
        //base.cleanOriginalPoint();
        //base.setOriginalPoint(base.getOdometryPose(-1));
    }

    @Override
    public void run() {
        //Log.d(TAG, "Odom run called");

        //Log.d(TAG, "getting ready to publish");
        publishOdom(base);

    }

    public void resetOdom() {
        Pose2D initialPose = base.getOdometryPose(-1);
        this.odomHolder.setInitialpose(initialPose);


    }

    public void setinitialPose(){
        Pose2D initialPose = base.getOdometryPose(-1);
        this.odomHolder.setInitialq(toQuaternion(0,0,-initialPose.getTheta()));
        this.odomHolder.setInitialpose(initialPose);
    }

    private void publishOdom(Base mBase) {
        //Log.d(TAG, "trying to publish");
        if (null != mBase) {
            long current = mBase.getOdometryPose(-1).getTimestamp();

            if (current > last) {

                odom = mMessageFactory.newFromType(Odometry._TYPE);
                initialpose = odomHolder.getInitialpose();
                origin = odomHolder.getOriginalinitialpose();
                // Log.v(TAG, String.format("stam: %d", current));

                double posX = (double) mBase.getOdometryPose(-1).getX();
                double posY = (double) mBase.getOdometryPose(-1).getY();

                double posTheata = (double) mBase.getOdometryPose(-1).getTheta();// + odomHolder.getInitialpose().getTheta();

                //Log.d(TAG, "posTheata: " + Double.toString(mBase.getOdometryPose(-1).getTheta()) +" original Theta: " + odomHolder.getInitialpose().getTheta());

                Quaternion q = toQuaternion(0, 0, posTheata);

                //q = q.multiply(initialq);
                //Log.d(TAG, q.toString());
                //q= q.multiply(initialq);

                LinearVelocity linearVelocity = mBase.getLinearVelocity();
                AngularVelocity angularVelocity = mBase.getAngularVelocity();
                angVel = (double) angularVelocity.getSpeed();
                linVel = (double) linearVelocity.getSpeed();
                //odom.getHeader().setStamp(mConnectedNode.getCurrentTime());
                odom.getHeader().setStamp(Time.fromNano((long) mConnectedNode.getCurrentTime().totalNsecs()));
                // Log.v("odomTimestamp:", odom.getHeader().getStamp().toString());
                odom.getHeader().setFrameId("odom");
                odom.setChildFrameId(Sensor.WORLD_ODOM_ORIGIN);
                odom.getPose().getPose().getPosition().setX(posX);
                odom.getPose().getPose().getPosition().setY(posY);
                odom.getPose().getPose().getOrientation().setW(q.getQ0());
                odom.getPose().getPose().getOrientation().setX(q.getQ1());
                odom.getPose().getPose().getOrientation().setY(q.getQ2());
                odom.getPose().getPose().getOrientation().setZ(q.getQ3());
                odom.getTwist().getTwist().getLinear().setX(linVel);
                odom.getTwist().getTwist().getAngular().setZ(angVel);
                //double diff = System.currentTimeMillis() - odom.getHeader().getStamp().totalNsecs() / 1000000;
                // Log.v("odomtime", Double.toString(diff));
                odom.getHeader().getStamp();
                if (!odom.getHeader().getFrameId().equals("")) {
                    mOdomPubr.publish(odom);
                }
                odomHolder.setOdom(odom);
                //Log.d(TAG, "Odom Published");
                last = current;

            }
        } else {
            Log.d(TAG, "mBase = null");
            this.base = baseHolder.getmBase();
        }
        // Log.d(TAG, "run: exit BasePublisherThread");
    }

    private void setmOdomPubr(ConnectedNode connectedNode, int navigationDataSource) {
        this.mOdomPubr = connectedNode.newPublisher("/odom", Odometry._TYPE);
    }

    public void setmMessageFactory(MessageFactory mMessageFactory) {
        this.mMessageFactory = mMessageFactory;
    }

    public void setOdomHolder(OdomHolder odomHolder) {
        this.odomHolder = odomHolder;
    }

    public void setmConnectedNode(ConnectedNode mConnectedNode) {
        this.mConnectedNode = mConnectedNode;
    }

    public void setBaseHolder(BaseHolder baseHolder) {
        this.baseHolder = baseHolder;
    }


    Quaternion toQuaternion(double pitch, double roll, double yaw) {

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

        Quaternion q = new Quaternion(w, x, y, z);
        return q;
    }

}


