package de.iteratec.loomo.ros.nodes;

import android.util.Log;
import de.iteratec.loomo.location.Location;
import de.iteratec.loomo.location.LocationService;
import de.iteratec.loomo.location.MapPosition;
import de.iteratec.loomo.navigation.Destination;
import de.iteratec.loomo.state.StateMachine.State;
import de.iteratec.loomo.state.UseCaseStateMachine;
import geometry_msgs.PoseWithCovarianceStamped;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import java.util.concurrent.CountDownLatch;

/**
 * Created by maximilian on 17.09.18.
 */

public class InitialPosePublisher extends AbstractNodeMain {
    private static final String TAG = "InitialPosePublisher";
    public static final String NODE_NAME = "init_pose_publisher";
    private Publisher<PoseWithCovarianceStamped> initialPosePublisher;
    private String topicName;
    ConnectedNode connectedNode;
    CountDownLatch initPublisher;

    public InitialPosePublisher(String topicName) {
        this.topicName = topicName;
    }

    public InitialPosePublisher() {
        this.topicName = "initialpose";

    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of(NODE_NAME);
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        initPublisher = new CountDownLatch(1);
        super.onStart(connectedNode);
        this.connectedNode = connectedNode;
        initialPosePublisher = connectedNode.newPublisher(topicName, PoseWithCovarianceStamped._TYPE);
        initPublisher.countDown();
    }

    public void publishInitialPose(State state) {
        if (initialPosePublisher == null) {
            return;
        }
        waitForLatchUnlock(initPublisher, "waiting forinitPosePublsher to initialise");
        Log.d(TAG, "publishing initialpose");
        PoseWithCovarianceStamped message = connectedNode.getTopicMessageFactory().newFromType(PoseWithCovarianceStamped._TYPE);
        Location activeLocation = LocationService.getInstance().getLocation();
        MapPosition initialPosition=activeLocation.getInitialPosition(Destination.EG_DOOR);
        if(state== UseCaseStateMachine.WAITING_AT_DOOR || state == UseCaseStateMachine.EG_DOOR) {
             initialPosition= activeLocation.getInitialPosition(Destination.EG_ELEVATOR);
        }
        if(state == UseCaseStateMachine.WAITING_AT_ELEVATOR){
            initialPosition= activeLocation.getInitialPosition(Destination.OG_DOOR);
        }
        else if(state ==UseCaseStateMachine.ELEVATOR){
            initialPosition = activeLocation.getInitialPosition(Destination.ELEVATOR);
        }
        /*else if(state == UseCaseStateMachine.WAITING_AT_OGDOOR){
            initialPosition= activeLocation.getInitialPosition(Destination.OG_DOOR);
        }*/
        message.getPose().getPose().getPosition().setX(initialPosition.getX());
        message.getPose().getPose().getPosition().setY(initialPosition.getY());
        org.apache.commons.math3.complex.Quaternion orientation = initialPosition.getQuaternion();
        message.getPose().getPose().getOrientation().setX(orientation.getQ1());
        message.getPose().getPose().getOrientation().setY(orientation.getQ2());
        message.getPose().getPose().getOrientation().setZ(orientation.getQ3());
        message.getPose().getPose().getOrientation().setW(orientation.getQ0());
        message.getHeader().setStamp(connectedNode.getCurrentTime());
        message.getHeader().setFrameId("map");
        double covariance[] = {
            0.05, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.05, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.06853891945200942};
        message.getPose().setCovariance(covariance);
        initialPosePublisher.publish(message);
    }

    private void waitForLatchUnlock(CountDownLatch latch, String latchName) {
        try {
            Log.d(TAG, "Waiting for " + latchName + " latch release...");
            latch.await();
            Log.d(TAG, latchName + " latch released!");
        } catch (InterruptedException ie) {
            Log.w(TAG, "Warning: continuing before " + latchName + " latch was released");
        }
    }

}
