package de.iteratec.loomo.ros.nodes;

import android.util.Log;
import de.iteratec.loomo.location.MapPosition;
import geometry_msgs.PoseStamped;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

/**
 * Created by maximilian on 11.09.18.
 */

public class NavGoalPublisher extends AbstractNodeMain {
    private static final String TAG = "NavGoalPublisher";
    public static final String NODE_NAME = "NavGoalPublisher";
    public String topic_name;
    private ConnectedNode connectedNode;
    private double x;
    private double y;
    private Publisher<geometry_msgs.PoseStamped> publisher;
    private Publisher<actionlib_msgs.GoalID> cancelPublisher;

    public NavGoalPublisher() {
        Log.d(TAG, "Creating nav goal publisher");
        this.topic_name = "move_base_simple/goal";
        this.x = 0;
        this.y = 0;

    }

    public NavGoalPublisher(String topic) {
        Log.d(TAG, "Creating nav goal publisher");

        this.topic_name = topic;
        this.x = 0;
        this.y = 0;

    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of(NODE_NAME);
    }

    public void onStart(ConnectedNode connectedNode) {
        this.connectedNode = connectedNode;
        final Publisher<PoseStamped> publisher = connectedNode.newPublisher(this.topic_name, "geometry_msgs/PoseStamped");
        final Publisher<actionlib_msgs.GoalID> cancelPublisher = connectedNode.newPublisher("/move_base/cancel", "actionlib_msgs/GoalID");
        this.cancelPublisher = cancelPublisher;
        this.publisher = publisher;
        Log.d(TAG, "finished initializing");

    }


    public void publishGoal(double posx, double posy, org.apache.commons.math3.complex.Quaternion quaternion) {

        Log.d(TAG, "Publishing goal");

        geometry_msgs.PoseStamped poseStamped = publisher.newMessage();
        poseStamped.getHeader().setFrameId("map");
        poseStamped.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));
        poseStamped.getPose().getPosition().setX(x);
        poseStamped.getPose().getPosition().setY(y);
        //this is a random orientation. Has to be set accordingly in the future
        poseStamped.getPose().getOrientation().setX(quaternion.getQ1());
        poseStamped.getPose().getOrientation().setY(quaternion.getQ2());
        poseStamped.getPose().getOrientation().setZ(quaternion.getQ3());
        poseStamped.getPose().getOrientation().setW(quaternion.getQ0());
        publisher.publish(poseStamped);
    }

    public void publishGoal(double posx, double posy) {

        Log.d(TAG, "Publishing goal");
        org.apache.commons.math3.complex.Quaternion quaternion = org.apache.commons.math3.complex.Quaternion.IDENTITY;
        geometry_msgs.PoseStamped poseStamped = publisher.newMessage();
        poseStamped.getHeader().setFrameId("map");
        poseStamped.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));
        poseStamped.getPose().getPosition().setX(x);
        poseStamped.getPose().getPosition().setY(y);
        //this is a random orientation. Has to be set accordingly in the future
        poseStamped.getPose().getOrientation().setX(quaternion.getQ1());
        poseStamped.getPose().getOrientation().setY(quaternion.getQ2());
        poseStamped.getPose().getOrientation().setZ(quaternion.getQ3());
        poseStamped.getPose().getOrientation().setW(quaternion.getQ0());
        publisher.publish(poseStamped);
    }

    public void publishGoal(MapPosition mapPosition) {

        Log.d(TAG, "Publishing goal");
        org.apache.commons.math3.complex.Quaternion quaternion = mapPosition.getQuaternion();
        geometry_msgs.PoseStamped poseStamped = publisher.newMessage();
        poseStamped.getHeader().setFrameId("map");
        poseStamped.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));
        poseStamped.getPose().getPosition().setX(mapPosition.getX());
        poseStamped.getPose().getPosition().setY(mapPosition.getY());
        //this is a random orientation. Has to be set accordingly in the future
        poseStamped.getPose().getOrientation().setX(quaternion.getQ1());
        poseStamped.getPose().getOrientation().setY(quaternion.getQ2());
        poseStamped.getPose().getOrientation().setZ(quaternion.getQ3());
        poseStamped.getPose().getOrientation().setW(quaternion.getQ0());
        publisher.publish(poseStamped);
    }

    public void cancelGoal() {
        actionlib_msgs.GoalID cancelGoal = cancelPublisher.newMessage();
        cancelPublisher.publish(cancelGoal);
    }

    public geometry_msgs.PoseStamped formatMessage(geometry_msgs.Pose goalPosition) {
        geometry_msgs.PoseStamped poseStamped = publisher.newMessage();
        poseStamped.setPose(goalPosition);
        poseStamped.getHeader().setFrameId("map");
        poseStamped.getHeader().setStamp(Time.fromMillis(System.currentTimeMillis()));
        return poseStamped;
    }
}
