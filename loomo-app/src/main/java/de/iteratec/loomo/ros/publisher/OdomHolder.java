package de.iteratec.loomo.ros.publisher;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import com.segway.robot.algo.Pose2D;
import nav_msgs.Odometry;
import org.apache.commons.math3.complex.Quaternion;

public class OdomHolder {
    private Odometry odom = null;
    private Odometry initialodom;
    private Pose2D originalinitialpose = new Pose2D(0, 0, 0, 0, 0, System.currentTimeMillis());
    private Pose2D initialpose = new Pose2D(0, 0, 0, 0, 0, System.currentTimeMillis());

    private Quaternion initialq = Quaternion.IDENTITY;
    private boolean firstime=true;
    public OdomHolder() {

    }

    public Pose2D getInitialpose() {
        return initialpose;
    }

    public void setInitialpose(Pose2D initialpose) {
        if(firstime){
            originalinitialpose=initialpose;
            firstime=false;
        }
        this.initialpose = initialpose;
    }

    public Quaternion getInitialq() {
        return initialq;
    }

    public void setInitialq(Quaternion initialq) {
        this.initialq = initialq;
    }

    public Odometry getOdom() {
        return odom;
    }

    public void setOdom(Odometry odom) {
        this.odom = odom;
    }

    public Pose2D getOriginalinitialpose() {
        return originalinitialpose;
    }

    public void setOriginalinitialpose(Pose2D originalinitialpose) {
        this.originalinitialpose = originalinitialpose;
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
