package de.iteratec.loomo.ros;

import actionlib_msgs.GoalID;
import actionlib_msgs.GoalStatus;
import actionlib_msgs.GoalStatusArray;
import android.content.Context;
import android.util.Log;
import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.algo.minicontroller.ObstacleStateChangedListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.perception.sensor.InfraredData;
import com.segway.robot.sdk.perception.sensor.Sensor;
import com.segway.robot.sdk.perception.sensor.UltrasonicData;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.sdk.vision.calibration.Intrinsic;
import com.segway.robot.sdk.vision.stream.StreamInfo;
import com.segway.robot.sdk.vision.stream.StreamType;
import de.iteratec.loomo.interaction.SpeakService;
import de.iteratec.loomo.ros.listener.ColorImageListener;
import de.iteratec.loomo.ros.listener.DepthImageListener;
import de.iteratec.loomo.ros.publisher.*;
import de.iteratec.loomo.state.Event;
import de.iteratec.loomo.state.StateService;
import java.util.ArrayList;
import java.util.List;
import nav_msgs.Odometry;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import sensor_msgs.CameraInfo;
import sensor_msgs.CompressedImage;
import sensor_msgs.Image;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/*
This is the main class which provides the Bridge to Ros for Loomo. It was provided by segwayrobotics and modified by Maximilian Steiner
 */
public class LoomoRosBridgeNode extends AbstractNodeMain {
    //Cyclic Barrier is used to synchronize the dethimage and camerainfo messages so they arrive at the same time. It ended up unused as it didn't provide the expected functionality
    //final CyclicBarrier gate= new CyclicBarrier(3);
    private static final String TAG = "LoomoRosBridgeNode";
    private Context context;
    //the loomo api object for its sensors
    private Sensor sensorAPI;
    //the loomo api object for its Base
    private Base baseAPI;
    boolean unbindAllowed=false;

    //calibration values for the camera.
    private Intrinsic colorIntrinsic, depthIntrinsic;
    private int colorWidth = 640;
    private int colorHeight = 480;
    private int depthWidth = 320;
    private int depthHeight = 240;

    //the node and message factory
    private ConnectedNode connectedNode;
    private MessageFactory messageFactory;

    private Runnable sensorPublisher;
    private ScheduledFuture sensorPublisherFuture;
    private Runnable baseSensorPublisher;
    private Runnable baseSensorPublisherVLS;
    private ScheduledFuture baseSensorPublisherFutureVLS;

    private ScheduledFuture baseSensorPublisherFuture;
    private Runnable irPublisher;
    private ScheduledFuture irPublisherFuture;

    private Publisher<Image> depthPublisher;
    private Publisher<CompressedImage> colorCompressedPublisher;
    private Publisher<CameraInfo> depthInfoPublisher;
    private Publisher<CameraInfo> colorInfoPublisher;


    //the pool for the sensor publisher threads
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private BaseHolder baseHolder = new BaseHolder();
    private OdomHolder odomHolder = new OdomHolder();
    private SensorHolder sensorHolder = new SensorHolder();
    private Vision visionAPI;
    private CountDownLatch bindServicesLatch;
    //private ServiceClient<Empty,Empty> serviceClient;
    private Odometry odomMessage;


    public static final String NODE_NAME = "loomo_ros_bridge";

    public LoomoRosBridgeNode(Context context) {
        super();
        this.context = context;
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(4);

    }

    public void resetOdom() {
        //baseAPI.setControlMode(Base.CONTROL_MODE_NAVIGATION);
        baseAPI.cleanOriginalPoint();
        Pose2D pose2D = baseAPI.getOdometryPose(-1);
        baseAPI.setOriginalPoint(pose2D);
        baseAPI.setControlMode(Base.CONTROL_MODE_RAW);
        /*
        OdometryPublisher odomp = (OdometryPublisher) this.baseSensorPublisher;
        odomp.resetOdom();*/
    }


    @Override
    public void onStart(final ConnectedNode connectedNode) {
        //connecting and declaring topics
        super.onStart(connectedNode);
        Log.d(TAG, "Starting LoomoRosBridgeNode");

        this.connectedNode = connectedNode;
        messageFactory = connectedNode.getTopicMessageFactory();

        /************* Try to move to DepthImageListener caused deadlock **********/
        // Latches used to guarantee correct order of initialisation of services and publishers
        CountDownLatch publisherLatch = new CountDownLatch(1);
        try {
            depthPublisher = connectedNode.newPublisher("loomo/realsense/depth/image_raw", Image._TYPE);
            depthInfoPublisher = connectedNode.newPublisher("loomo/realsense/depth/camera_info", CameraInfo._TYPE);
            colorCompressedPublisher = connectedNode.newPublisher("loomo/realsense/rgb/compressed", CompressedImage._TYPE);
            colorInfoPublisher = connectedNode.newPublisher("loomo/realsense/rgb/camera_info", CameraInfo._TYPE);
            Log.d(TAG, "initialised publisher");
            publisherLatch.countDown();
        } catch (RuntimeException re) {
            Log.w(TAG, "Problem creating publisher.", re);
            publisherLatch.countDown();
        }

        waitForLatchUnlock(publisherLatch, "initialise Publishers");
        Subscriber<geometry_msgs.Twist> subscriber = connectedNode.newSubscriber("/cmd_vel", geometry_msgs.Twist._TYPE);
        Subscriber<actionlib_msgs.GoalStatusArray> goalSubscriber = connectedNode.newSubscriber("/move_base/status", GoalStatusArray._TYPE);
        //Subscriber<std_msgs.String> stateSubsciber = connectedNode.newSubscriber("/loomo/state","/std_msgs/String");
        //stateSubsciber.addMessageListener(stateListener);
        subscriber.addMessageListener(mSpeedListener);
        goalSubscriber.addMessageListener(goalListener);
        try {
            bindServicesLatch = new CountDownLatch(3);

            bindServices();
            waitForLatchUnlock(bindServicesLatch, "binding Services");
        } catch (RuntimeException re) {
            Log.w(TAG, "Problem invoking ros listener.", re);
        }

        odomMessage= messageFactory.newFromType(Odometry._TYPE);
        baseSensorPublisher = new OdometryPublisher(messageFactory, connectedNode, odomHolder, baseHolder, Base.NAVIGATION_SOURCE_TYPE_ODOM);
        //baseSensorPublisherVLS = new OdometryPublisher(messageFactory, connectedNode, odomHolder, baseHolder, Base.NAVIGATION_SOURCE_TYPE_VLS);
        irPublisher = new IrPublisher(sensorHolder, messageFactory, connectedNode);
        sensorPublisher = new TFPublisher(sensorHolder, messageFactory, connectedNode, odomHolder);
        Log.d(TAG, "Finished starting LoomoRosBridgeNode");
        CountDownLatch startSensorTransmissionLatch = new CountDownLatch(1);
        startSensorTransmission(startSensorTransmissionLatch);
        waitForLatchUnlock(startSensorTransmissionLatch, "starting Sensor Transmission");

        /* connection to move_base service
        try {
            this.serviceClient = connectedNode.newServiceClient("clear_costmap", std_srvs.Empty._TYPE);
        } catch (ServiceNotFoundException e) {
            Log.d(TAG,"Service not found");
        }*/

    }

/*
    public void clear_costmap(){
        final std_srvs.Empty request = this.serviceClient.newMessage();
        serviceClient.call(request, new ServiceResponseListener<Empty>() {
            @Override
            public void onSuccess(std_srvs.Empty response) {
                Log.d(TAG, "service call succes");
            }

            @Override
            public void onFailure(RemoteException e) {
                throw new RosRuntimeException(e);
            }
        });

    }*/

    @Override
    public void onShutdown(Node node) {
        super.onShutdown(node);
        unbindAllowed=true;
        unbindServices();
    }

    @Override
    public void onShutdownComplete(Node node) {
        super.onShutdownComplete(node);
        try {
            scheduledThreadPoolExecutor.shutdown();

            depthPublisher.shutdown();
            depthInfoPublisher.shutdown();
            colorCompressedPublisher.shutdown();
            colorInfoPublisher.shutdown();
        } catch (Exception e) {
            Log.w(TAG, "onShutdownComplete: scheduledThreadPoolExecutor.shutdown() ", e);
        }
    }

    @Override
    public void onError(Node node, Throwable throwable) {
        super.onError(node, throwable);
        unbindServices();
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("loomo_ros_bridge_node");
    }

    private void startSensorTransmission(CountDownLatch latch) {

        Log.d(TAG, "Start sensor transmission");
        //we start the publishing of the tf and odom.
        baseSensorPublisherFuture = scheduledThreadPoolExecutor.scheduleWithFixedDelay(baseSensorPublisher, 5000, 20, TimeUnit.MILLISECONDS);
        //baseSensorPublisherFutureVLS = scheduledThreadPoolExecutor.scheduleWithFixedDelay(baseSensorPublisherVLS, 20, 20, TimeUnit.MILLISECONDS);

        sensorPublisherFuture = scheduledThreadPoolExecutor.scheduleWithFixedDelay(sensorPublisher, 5000, 20, TimeUnit.MILLISECONDS);
        //irPublisherFuture = scheduledThreadPoolExecutor.scheduleWithFixedDelay(irPublisher, 5000, 20, TimeUnit.MILLISECONDS);
        latch.countDown();
    }

    public void bindServices() {
        sensorAPI = Sensor.getInstance();
        visionAPI = Vision.getInstance();
        baseAPI = Base.getInstance();

        Log.d(TAG, "Start binding services.");
        if (sensorAPI.bindService(context, mSensorBindListener)) {
            Log.d(TAG, "Bind sensor service success！");
            bindServicesLatch.countDown();
        } else {
            Log.d(TAG, "Bind sensor service failed!");
        }

        if (visionAPI.bindService(context, mBindVisionListener)) {
            Log.d(TAG, "Bind vision service success");
            bindServicesLatch.countDown();

        } else {
            Log.d(TAG, "Bind vision service failed");
        }

        if (baseAPI.bindService(context, mBaseBindListener)) {
            Log.d(TAG, "Bind base service success！");

            bindServicesLatch.countDown();
        } else {
            Log.d(TAG, "Bind base service failed!");
        }

    }

    public void unbindServices() {
        Log.d(TAG, "unbind segway listener");
        sensorAPI.unbindService();
        baseAPI.unbindService();
        if (null != visionAPI) {
            StreamInfo[] infos = visionAPI.getActivatedStreamInfo();
            for (StreamInfo info : infos) {
                switch (info.getStreamType()) {
                    case StreamType.COLOR:
                        visionAPI.stopListenFrame(StreamType.COLOR);
                        break;
                    case StreamType.DEPTH:
                        visionAPI.stopListenFrame(StreamType.DEPTH);
                        break;
                }
            }
            visionAPI.unbindService();
        }
    }
    private List<GoalID> finishedgoals = new ArrayList<GoalID>();
    private MessageListener<actionlib_msgs.GoalStatusArray> goalListener = new MessageListener<actionlib_msgs.GoalStatusArray>() {
        @Override
        public void onNewMessage(actionlib_msgs.GoalStatusArray msg) {
            //Log.d(TAG,"received Goal status! Size: " + msg.getStatusList().size());
            if (msg.getStatusList().size() >= 1) {
                for(GoalStatus status: msg.getStatusList()){
                    if(!finishedgoals.contains(status.getGoalId())){
                        if ((int)status.getStatus() == 3) {
                            finishedgoals.add(status.getGoalId());
                            StateService state = StateService.getInstance();
                            state.triggerEvent(Event.DESTINATION_ARRIVED);
                        }
                    }
                }

                //Log.d(TAG,"status: " + msg.getStatusList().get(msg.getStatusList().size()-1).getText() + " ID: " + Integer.toString(msg.getStatusList().get(msg.getStatusList().size()-1).getStatus()));

            }
//            Log.d("Speed2", String.format("ControlMode: %d  CartMode %b", baseAPI.getControlMode(),baseAPI.isInCartMode()));
//            Log.d(TAG, String.format("vel: [%f, %f]", linVel,angVel));
        }
    };


    private MessageListener<geometry_msgs.Twist> mSpeedListener = new MessageListener<geometry_msgs.Twist>() {
        //here the cmd_vel topic is initialised. It forwards the velocity commands to the robots base
        @Override
        public void onNewMessage(geometry_msgs.Twist msg) {
            Log.d("Speed1", String.format("ControlMode: %d  CartMode %b", baseAPI.getCartMile(), baseAPI.isInCartMode()));

            double linVel = msg.getLinear().getX();
            double angVel = msg.getAngular().getZ();
            //baseAPI.setCartMode(true);

            baseAPI.setLinearVelocity((float) linVel);
            baseAPI.setAngularVelocity((float) angVel);
//            Log.d("Speed2", String.format("ControlMode: %d  CartMode %b", baseAPI.getControlMode(),baseAPI.isInCartMode()));
//            Log.d(TAG, String.format("vel: [%f, %f]", linVel,angVel));
        }
    };

    private ServiceBinder.BindStateListener mBindVisionListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            Log.i(TAG, "onBindVision");
            waitForLatchUnlock(bindServicesLatch, "rgbTransfer");
            startRgbdTransfer();
        }

        @Override
        public void onUnbind(String reason) {
            Log.i(TAG, "onUnbindVision: " + reason);
            try{
                stopRgbdTransfer();
            }catch (com.segway.robot.sdk.vision.VisionServiceException e){
                Log.d(TAG, e.toString());
            }
            if(!unbindAllowed) {
                Log.d(TAG, "unintentional unbind. Attempting to rebind");
                bindVision();
            }
            unbindAllowed=false;
        }
    };
    private void bindVision(){
        if(visionAPI.bindService(context, mBindVisionListener)){
            Log.d(TAG, "bind vision succes");
        };

    }
    private void startRgbdTransfer() {
        if (null == visionAPI) {
            Log.w(TAG, "startRgbdTransfer(): did not bind service!");
            bindVision();

            return;
        }
        Log.i(TAG, "Starting image listener");

        StreamInfo[] infos = visionAPI.getActivatedStreamInfo();
        for (StreamInfo info : infos) {
            switch (info.getStreamType()) {
                case StreamType.COLOR:
                    if (visionAPI == null) {
                        Log.d(TAG, "couldn't initialise color listener");
                        break;
                    }
                    Log.i(TAG, "Starting color image listener");
                    initCameraInfo(2, visionAPI.getColorDepthCalibrationData().colorIntrinsic,
                            info.getWidth(), info.getHeight());

                    visionAPI.startListenFrame(StreamType.COLOR, new ColorImageListener(this.connectedNode, colorCompressedPublisher, colorInfoPublisher, colorIntrinsic, colorWidth, colorHeight));
                    break;
                case StreamType.DEPTH:
                    Log.i(TAG, "Starting depth image listener");
                    if (visionAPI == null || visionAPI.getColorDepthCalibrationData() == null
                        || visionAPI.getColorDepthCalibrationData().depthIntrinsic == null) {
                        Log.d(TAG, "couldn't initialise depth listener");
                        break;
                    }
                    initCameraInfo(3, visionAPI.getColorDepthCalibrationData().depthIntrinsic,
                            info.getWidth(), info.getHeight());
                    visionAPI.startListenFrame(StreamType.DEPTH, new DepthImageListener(this.connectedNode, depthPublisher, depthInfoPublisher, depthIntrinsic, depthWidth, depthHeight));
                    break;
            }
        }
    }

    private synchronized void stopRgbdTransfer() {
        if (null == visionAPI) {
            Log.w(TAG, "stopRgbdTransfer(): did not bind service!");
            return;
        }
        visionAPI.stopListenFrame(StreamType.COLOR);
        visionAPI.stopListenFrame(StreamType.DEPTH);
    }

    private ServiceBinder.BindStateListener mSensorBindListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            Log.d(TAG, "onBindSensor: ");
            sensorHolder.setSensor(sensorAPI);
        }

        @Override
        public void onUnbind(String reason) {
            Log.i(TAG, "onUnbindSensor: " + reason);
            sensorPublisherFuture.cancel(true);
            try {
                scheduledThreadPoolExecutor.shutdown();
            } catch (Exception e) {
                Log.w(TAG, "onUnbind: sensorPublisher.join() ", e);
            }
        }
    };

    public ServiceBinder.BindStateListener getmBaseBindListener() {
        return mBaseBindListener;
    }

    public void moveForward(float speed, int step){
        baseAPI.setControlMode(Base.CONTROL_MODE_RAW);
        for (int i = 1; i<= step; i++){
            baseAPI.setLinearVelocity(speed);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void turnAround(float speed,int step){
        baseAPI.setControlMode(Base.CONTROL_MODE_RAW);
        for (int i = 1; i<= step; i++){
            baseAPI.setAngularVelocity(speed);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkDistances(){
        boolean isFree = false;

        UltrasonicData ultrasonicData = sensorAPI.getUltrasonicDistance();
        Log.i("distance", "ultrasonicData: "+ ultrasonicData);
        InfraredData infraredData = sensorAPI.getInfraredDistance();
        Log.i("distance", "infraredData: "+ infraredData);
        if((Math.abs(ultrasonicData.getDistance()-infraredData.getRightDistance())<50) && (Math.abs(ultrasonicData.getDistance()-infraredData.getLeftDistance()) <50)){
            isFree = true;
            Log.i("distance", "" + isFree);
        } else if(Math.abs(infraredData.getLeftDistance()-infraredData.getRightDistance())<50) {
            isFree = true;
            Log.i("distance", "" + isFree);
        } else if(infraredData.getLeftDistance()-infraredData.getRightDistance() >50) {
            turnAround(1,2);
            stopAngularVelocity();
            //setBaseVelocity(0.5f,1);
            moveForward(0.5f,1);
            stopLinearVelocity();
            turnAround(-1,2);
            stopAngularVelocity();
            isFree = true;
        }else if (infraredData.getRightDistance()-infraredData.getLeftDistance() >50) {
            turnAround(-1,2);
            stopAngularVelocity();
            moveForward(0.5f,1);
            stopLinearVelocity();
            turnAround(1,2);
            stopAngularVelocity();
            isFree = true;
        }

        return isFree;
    }


    public void stopAngularVelocity() {
        baseAPI.setAngularVelocity(0);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopLinearVelocity(){
        baseAPI.setLinearVelocity(0);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setBaseVelocity(float linearVelocity, float angularVelocity) {
        baseAPI.setControlMode(Base.CONTROL_MODE_RAW);
        baseAPI.setLinearVelocity(linearVelocity);
        baseAPI.setAngularVelocity(angularVelocity);
        stopAngularVelocity();
        stopLinearVelocity();
    }

    public void setObstacleAvoidance(){
        baseAPI.setControlMode(Base.CONTROL_MODE_NAVIGATION);
        Log.i("ultrasonic","bindState: " + sensorAPI.isBind());
        if(!baseAPI.isUltrasonicObstacleAvoidanceEnabled()){
            baseAPI.setUltrasonicObstacleAvoidanceEnabled(true);
        }
        Log.i("ultrasonic", "isUltrasonicObstacleAvoidanceEnabled: " + baseAPI.isUltrasonicObstacleAvoidanceEnabled());
        baseAPI.setUltrasonicObstacleAvoidanceDistance(0.75f);
        baseAPI.setObstacleStateChangeListener(new ObstacleStateChangedListener() {
            @Override
            public void onObstacleStateChanged(int ObstacleAppearance) {
                Log.i("ultrasonic", "obstacleAppearance: " + ObstacleAppearance);
                if (ObstacleAppearance == 1) {
                    turnAround(-1, 2);
                    stopAngularVelocity();
                    setBaseVelocity(0.5f, 1);
                    turnAround(1, 1);
                    stopAngularVelocity();
                    //baseAPI.setControlMode(Base.CONTROL_MODE_RAW);
                } else {
                    moveForward(0.7f, 4);
                    stopLinearVelocity();
                    //baseAPI.setControlMode(Base.CONTROL_MODE_RAW);
                }
            }
        });
    }



    /**
     * private VLSPoseListener vlsPoseListener = new VLSPoseListener() {
     *
     * @Override public void onVLSPoseUpdate(long timestamp, float pose_x, float pose_y, float pose_theta, float v, float w) {
     * Log.d(TAG, "onVLSPoseUpdate() called with: timestamp = [" + timestamp + "], pose_x = [" + pose_x + "], pose_y = [" + pose_y + "], pose_theta = [" + pose_theta + "], v = [" + v + "], w = [" + w + "]");
     * }
     * };
     */

    private ServiceBinder.BindStateListener mBaseBindListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            Log.i(TAG, "onBindBase: resetting odom");
            odomHolder.setInitialpose(baseAPI.getOdometryPose(-1));
            StringBuilder value = new StringBuilder("intialTheta:");
            value.append(odomHolder.getInitialpose().getTheta());
            Log.d(TAG, value.toString());
            odomHolder.setOriginalinitialpose(baseAPI.getOdometryPose(-1));
            //baseAPI.setControlMode(Base.CONTROL_MODE_NAVIGATION);
            //baseAPI.cleanOriginalPoint();
            Pose2D pose2D = baseAPI.getOdometryPose(-1);
            //baseAPI.setOriginalPoint(pose2D);
            Log.d(TAG, "setting base holder");
            baseAPI.setControlMode(Base.CONTROL_MODE_RAW);
            baseHolder.setmBase(baseAPI);


        }

        @Override
        public void onUnbind(String reason) {
            Log.i(TAG, "onUnbindBase: " + reason);
            baseAPI = null;
            try {
                baseSensorPublisherFuture.cancel(true);
            } catch (Exception e) {
                Log.w(TAG, "onUnbind: baseSensorPublisher.join() ", e);
            }
        }
    };

    private void initCameraInfo(int type, Intrinsic ins, int width, int height) {
        //initialising the camera info
        if (type == 1) {
            // platform camera intrinsic not supported yet
            Log.w(TAG, "updateCameraInfo: platform camera intrinsic not supported yet!");
        } else if (type == StreamType.DEPTH) {
            colorIntrinsic = ins;
            colorWidth = width;
            colorHeight = height;
        } else {
            depthIntrinsic = ins;
            depthWidth = width;
            depthHeight = height;
        }
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


    public void navigationByCoordinates(int x,int y){
        baseAPI.setControlMode(Base.CONTROL_MODE_NAVIGATION);
        baseAPI.setOnCheckPointArrivedListener(checkpointListener);
        baseAPI.addCheckPoint(x,y,0);
    }

    public void navigationByCoordinates(int x,int y,float theta){
        baseAPI.setControlMode(Base.CONTROL_MODE_NAVIGATION);
        baseAPI.setOnCheckPointArrivedListener(checkpointListener);
        baseAPI.addCheckPoint(x,y,theta);

    }

    CheckPointStateListener checkpointListener = new CheckPointStateListener() {
        @Override
        public void onCheckPointArrived(CheckPoint checkPoint, final Pose2D realPose, boolean isLast) {
            baseAPI.setControlMode(Base.CONTROL_MODE_RAW);
            SpeakService.getInstance().say("Checkpoint reached");
        }

        @Override
        public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {
            baseAPI.setControlMode(Base.CONTROL_MODE_RAW);
            SpeakService.getInstance().say("Checkpoint failed");


        }
    };

}
