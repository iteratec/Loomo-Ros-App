package de.iteratec.loomo.ros;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import de.iteratec.loomo.ros.nodes.AmclNode;
import de.iteratec.loomo.ros.nodes.DepthimageToLaserScanNode;
import de.iteratec.loomo.ros.nodes.MoveBaseNode;
import de.iteratec.loomo.ros.nodes.ScanFilterNode;
import de.iteratec.loomo.ros.nodes.ScanFilterNodeAmcl;
import de.iteratec.loomo.R;
import de.iteratec.loomo.location.Location;
import de.iteratec.loomo.location.LocationService;
import de.iteratec.loomo.navigation.NavigationService;
import de.iteratec.loomo.ros.map.MapGenerator;
import de.iteratec.loomo.ros.nodes.*;
import de.iteratec.loomo.state.StateMachine.State;
import de.iteratec.loomo.state.StateService;

import org.ros.helpers.ParameterLoaderNode;
import org.ros.node.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class RosService {
    private static final String TAG = "RosService";
    private static RosService instance;
    private NodeMainExecutor nodeMainExecutor;
    // Resources

    public static synchronized RosService getInstance() {
        if (instance == null) {
            Log.d(TAG, "didnt initialise Ros Service");
            return null;
        }
        return instance;
    }

    public void setInstance(RosService instance) {
        this.instance = instance;
    }

    private static ArrayList<Pair<Integer, String>> mResourcesToLoad = new ArrayList<Pair<Integer, String>>() {{
        add(new Pair<>(R.raw.costmap_common_params_depthimages, MoveBaseNode.NODE_NAME + "/local_costmap"));
        add(new Pair<>(R.raw.costmap_common_params_depthimages, MoveBaseNode.NODE_NAME + "/global_costmap"));
        add(new Pair<>(R.raw.local_costmap_params, MoveBaseNode.NODE_NAME + "/local_costmap"));
        add(new Pair<>(R.raw.global_costmap_params, MoveBaseNode.NODE_NAME + "/global_costmap"));
        add(new Pair<>(R.raw.base_local_planner_params, MoveBaseNode.NODE_NAME + "/TrajectoryPlannerROS"));
        add(new Pair<>(R.raw.dwa_local_planner_params, MoveBaseNode.NODE_NAME + "/DWAPlannerROS"));
        add(new Pair<>(R.raw.move_base_params, MoveBaseNode.NODE_NAME));
        add(new Pair<>(R.raw.depthimage_to_laserscan, "/depthimage_to_laserscan"));
        add(new Pair<>(R.raw.amcl, "/amcl"));
        //add(new Pair<>(R.raw.depth_laser_clearing, "/laser_filter"));
        //add(new Pair<>(R.raw.depth_laser_filter, "/laser_filter_amcl"));
        add(new Pair<>(R.raw.laser_filter_clearing, "/laser_filter"));
        add(new Pair<>(R.raw.laser_filter, "/laser_filter_amcl"));




    }};
    CountDownLatch startNodeslatch = new CountDownLatch(1);
    private LoomoRosBridgeNode mBridgeNode;
    private URI masterUri;
    private String hostName;
    private MoveBaseNode moveBaseNode;
    private AmclNode amclNode;
    private NodeMain[] activenodes;
    private ParameterLoaderNode mParameterLoaderNode;
    private DepthimageToLaserScanNode depthimageToLaserscanNode;
    private InitialPosePublisher initialPosePublisher;
    private MapPublisher mapServerNode;


    private NavGoalPublisher navGoalPublisherNode;
    private ScanFilterNode scanFilterNode;
    private ScanFilterNodeAmcl scanFilterNodeAmcl;
    private ArrayList<ParameterLoaderNode.Resource> mOpenedResources = new ArrayList<>();
    private Context context;

    public RosService(Context context) {
        this.context = context;
        for (Pair<Integer, String> ip : mResourcesToLoad) {
            mOpenedResources.add(new ParameterLoaderNode.Resource(
                    context.getResources().openRawResource(ip.first.intValue()), ip.second));
        }
        mBridgeNode = new LoomoRosBridgeNode(context);
        this.instance = this;
    }

    public void stop() {
        mBridgeNode.unbindServices();
        //shutting down nodes
        if (activenodes != null) {
            for (NodeMain node : activenodes) {
                nodeMainExecutor.shutdownNodeMain(node);
                Log.d(TAG, "shutting down node: " + node.getDefaultNodeName());
            }
            activenodes = null;
        }
    }

    public void init(NodeMainExecutor nodeMainExecutor, URI masterUri, String masterHostname) {
        Log.d(TAG, "init: called");
        this.nodeMainExecutor = nodeMainExecutor;
        this.masterUri = masterUri;
        this.hostName = masterHostname;

        startNodes();

        waitForLatchUnlock(startNodeslatch, "waiting until nodes are initialised");
        //TODO: ref zu Voice Action
        NavigationService.getInstance().setNavGoalPublisher(navGoalPublisherNode);
    }

    protected void startNodes() {
        configureParameterServer();
        startLoomoRosBridge();
        startDepthimageToLaserscan();
        startAmcl();
        startMapPublisher();
        startMoveBase();
        startInitialPosePublisher();
        startNavGoalPublisher();
       // startScanFilterNode(new String[]{"scan:=scan_depthimage","scan_filtered:=scan_for_clearing"});
       // startScanFilterNodeAmcl( new String[]{"scan:=scan_depthimage","scan_filtered:=scan_for_amcl"});
        startScanFilterNode(new String[]{"scan_filtered:=scan_for_clearing"});
        startScanFilterNodeAmcl( new String[]{"scan_filtered:=scan_for_amcl"});

        startNodeslatch.countDown();

    }
    private void startScanFilterNodeAmcl( String[] remappingArguments) {
        Log.d(TAG, "Starting ScanFilterNode...");
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(hostName);
        nodeConfiguration.setMasterUri(masterUri);
        scanFilterNodeAmcl = new ScanFilterNodeAmcl(remappingArguments);
        addactiveNode(scanFilterNodeAmcl);
        nodeMainExecutor.execute(scanFilterNodeAmcl, nodeConfiguration);
    }
    private void startScanFilterNode(String[] remappingArguments) {
        Log.d(TAG, "Starting ScanFilterNode...");
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(hostName);
        nodeConfiguration.setMasterUri(masterUri);
        scanFilterNode = new ScanFilterNode(remappingArguments);
        addactiveNode(scanFilterNode);
        nodeMainExecutor.execute(scanFilterNode, nodeConfiguration);
    }

    private void startInitialPosePublisher() {
        Log.d(TAG, "Starting NavGoalPublisher...");
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(hostName);
        nodeConfiguration.setMasterUri(masterUri);
        nodeConfiguration.setNodeName(InitialPosePublisher.NODE_NAME);
        String[] remappingArguments = {};
        initialPosePublisher = new InitialPosePublisher();
        addactiveNode(initialPosePublisher);
        nodeMainExecutor.execute(initialPosePublisher, nodeConfiguration);
    }


    private void startNavGoalPublisher() {
        Log.d(TAG, "Starting NavGoalPublisher...");
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(hostName);
        nodeConfiguration.setMasterUri(masterUri);
        nodeConfiguration.setNodeName(NavGoalPublisher.NODE_NAME);
        String[] remappingArguments = {};
        navGoalPublisherNode = new NavGoalPublisher();
        addactiveNode(navGoalPublisherNode);
        nodeMainExecutor.execute(navGoalPublisherNode, nodeConfiguration);
        NavigationService.getInstance().setNavGoalPublisher(navGoalPublisherNode);
    }

    private void startMapPublisher() {
        Log.d(TAG, "Starting map_server...");
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(hostName);
        nodeConfiguration.setMasterUri(masterUri);
        nodeConfiguration.setNodeName("map_server_amcl");
        mapServerNode = new MapPublisher(new MapGenerator(context));
        addactiveNode(mapServerNode);
        nodeMainExecutor.execute(mapServerNode, nodeConfiguration);
    }



    private void startDepthimageToLaserscan() {
        Log.d(TAG, "Starting depthimage to laserscan...");
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(hostName);
        nodeConfiguration.setMasterUri(masterUri);
        nodeConfiguration.setNodeName(DepthimageToLaserScanNode.NODE_NAME);
        String[] remappingArguments = {"image:=/loomo/realsense/depth/image_raw", "scan:=scan_depthimage"};
        depthimageToLaserscanNode = new DepthimageToLaserScanNode(remappingArguments);
        addactiveNode(depthimageToLaserscanNode);
        nodeMainExecutor.execute(depthimageToLaserscanNode, nodeConfiguration);
    }

    private synchronized void startMoveBase() {
        Log.d(TAG, "Starting move base native node wrapper...");
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(hostName);
        nodeConfiguration.setMasterUri(masterUri);
        nodeConfiguration.setNodeName(MoveBaseNode.NODE_NAME);
        //String[] remappingArguments = {"map:=mb_map", "base_link:=base_center_wheel_axis_frame", "robot_base_frame:=base_center_wheel_axis_frame", "scan:=scan_depthimage"};
        String[] remappingArguments = {"map:=mb_map", "base_link:=base_center_wheel_axis_frame", "robot_base_frame:=base_center_wheel_axis_frame"};
        moveBaseNode = new MoveBaseNode(remappingArguments);
        addactiveNode(moveBaseNode);
        nodeMainExecutor.execute(moveBaseNode, nodeConfiguration);
    }

    private synchronized void startAmcl() {
        Log.d(TAG, "Starting amcl native node wrapper...");
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(hostName);
        nodeConfiguration.setMasterUri(masterUri);
        nodeConfiguration.setNodeName(AmclNode.NODE_NAME);
        //String[] remappingArguments = {"scan:=scan_depthimage","map:=amcl_map"};
        String[] remappingArguments = {"scan:=scan_for_amcl","map:=amcl_map"};
        amclNode = new AmclNode(remappingArguments);
        addactiveNode(amclNode);
        nodeMainExecutor.execute(amclNode, nodeConfiguration);
    }

    private synchronized void startLoomoRosBridge() {
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(hostName);
        nodeConfiguration.setMasterUri(masterUri);
        nodeConfiguration.setNodeName(LoomoRosBridgeNode.NODE_NAME);
        nodeMainExecutor.execute(mBridgeNode, nodeConfiguration);
    }

    private void startParameterLoaderNode(final CountDownLatch latch) {
        // Create node to load configuration to Parameter Server
        Log.d(TAG, "Setting parameters in Parameter Server");
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(hostName);
        nodeConfiguration.setMasterUri(masterUri);
        nodeConfiguration.setNodeName(ParameterLoaderNode.NODE_NAME);
        mParameterLoaderNode = new ParameterLoaderNode(mOpenedResources);
        nodeMainExecutor.execute(mParameterLoaderNode, nodeConfiguration,
                new ArrayList<NodeListener>() {{
                    add(new DefaultNodeListener() {
                        @Override
                        public void onShutdown(Node node) {
                            latch.countDown();
                        }

                        @Override
                        public void onError(Node node, Throwable throwable) {
                            Log.e(TAG, "Error loading parameters to ROS parameter server: " + throwable.getMessage(), throwable);
                        }
                    });
                }});
    }

    private void addactiveNode(NodeMain node) {
        if (activenodes == null) {
            activenodes = new NodeMain[1];
            activenodes[0] = node;
        } else {
            NodeMain[] tempnodes = new NodeMain[activenodes.length + 1];
            int i = 0;
            for (NodeMain tempnode : activenodes) {
                tempnodes[i] = tempnode;
                i++;
            }
            tempnodes[activenodes.length] = node;
            Log.d(TAG, "adding node: " + node.getDefaultNodeName());
            activenodes = tempnodes;
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

    //creating a latch for Parameter server so it waits until parameters are set
    private void configureParameterServer() {
        CountDownLatch latch = new CountDownLatch(1);
        startParameterLoaderNode(latch);
        waitForLatchUnlock(latch, "parameter");
    }

    public MoveBaseNode getMoveBaseNode() {
        return moveBaseNode;
    }

    public void setMoveBaseNode(MoveBaseNode moveBaseNode) {
        this.moveBaseNode = moveBaseNode;
    }

    public AmclNode getAmclNode() {
        return amclNode;
    }

    public void setAmclNode(AmclNode amclNode) {
        this.amclNode = amclNode;
    }

    public NodeMain[] getActivenodes() {
        return activenodes;
    }

    public void setActivenodes(NodeMain[] activenodes) {
        this.activenodes = activenodes;
    }

    public ParameterLoaderNode getmParameterLoaderNode() {
        return mParameterLoaderNode;
    }

    public void setmParameterLoaderNode(ParameterLoaderNode mParameterLoaderNode) {
        this.mParameterLoaderNode = mParameterLoaderNode;
    }

    public DepthimageToLaserScanNode getDepthimageToLaserscanNode() {
        return depthimageToLaserscanNode;
    }

    public void setDepthimageToLaserscanNode(
            DepthimageToLaserScanNode depthimageToLaserscanNode) {
        this.depthimageToLaserscanNode = depthimageToLaserscanNode;
    }

    public InitialPosePublisher getInitialPosePublisher() {
        return initialPosePublisher;
    }

    public void setInitialPosePublisher(
            InitialPosePublisher initialPosePublisher) {
        this.initialPosePublisher = initialPosePublisher;
    }



    public NavGoalPublisher getNavGoalPublisherNode() {
        return navGoalPublisherNode;
    }

    public void setNavGoalPublisherNode(NavGoalPublisher navGoalPublisherNode) {
        this.navGoalPublisherNode = navGoalPublisherNode;
    }

    public void initialiseNavigation() {
        //resetOdom();
        CountDownLatch map_publish_latch = new CountDownLatch(1);
        Location location= LocationService.getInstance().getLocation();
        State state = StateService.getInstance().getSm().getState();
        switch(location) {
            case MUC:
                if(false){
                    map_publish_latch = new CountDownLatch(1);
                    mapServerNode.publishMapforAmcl(R.raw.slab_lift_try_inf, R.raw.slab_lift_try, map_publish_latch);
                    waitForLatchUnlock(map_publish_latch, "waiting for maps");
                    map_publish_latch = new CountDownLatch(1);
                    mapServerNode.publishMapforMb(R.raw.slab_lift_try_inf, R.raw.slab_lift_try_edited, map_publish_latch);
                    waitForLatchUnlock(map_publish_latch, "waiting for maps");
                    initialPosePublisher.publishInitialPose(state);
                    break;
                }else{
                    map_publish_latch = new CountDownLatch(1);
                    mapServerNode.publishMapforAmcl(R.raw.muc_eg_elev_openv1_complete_info, R.raw.muc_eg_elev_openv1_complete, map_publish_latch);
                    waitForLatchUnlock(map_publish_latch, "waiting for maps");
                    map_publish_latch = new CountDownLatch(1);
                    mapServerNode.publishMapforMb(R.raw.muc_eg_elev_openv1_complete_info, R.raw.muc_eg_elev_openv1_complete_edited_osize, map_publish_latch);
                    waitForLatchUnlock(map_publish_latch, "waiting for maps");
                    initialPosePublisher.publishInitialPose(state);
                    break;
                }
            case MUC_OG:
                map_publish_latch = new CountDownLatch(1);
               // mapServerNode.publishMapforAmcl(R.raw.elevatormap_info, R.raw.elevatormap, map_publish_latch);
                mapServerNode.publishMapforAmcl(R.raw.muc_map_og_yaml, R.raw.muc_map_og_pgm_mirrored, map_publish_latch);
                waitForLatchUnlock(map_publish_latch, "waiting for maps");
                map_publish_latch = new CountDownLatch(1);
               // mapServerNode.publishMapforMb(R.raw.elevatormap_info, R.raw.elevatormap_edited, map_publish_latch);
                mapServerNode.publishMapforMb(R.raw.muc_map_og_yaml, R.raw.muc_map_og_pgm_edited_mirrored, map_publish_latch);
                waitForLatchUnlock(map_publish_latch, "waiting for maps");
                initialPosePublisher.publishInitialPose(state);
                break;
        }


    }

    public void resetOdom() {
        mBridgeNode.resetOdom();
    }

    public void moveForward(float speed, int step){
        mBridgeNode.moveForward(speed, step);
    }

    public void turnAround(float angel, int step) {
        mBridgeNode.turnAround(angel, step);
    }

  public void clearCostmap(){
        //mBridgeNode.clear_costmap();
  }

  public boolean checkDistances(){
        return mBridgeNode.checkDistances();
  }

    public void stopAngularVelocity(){
        mBridgeNode.stopAngularVelocity();
    }

    public void stopLinearVelocity(){
        mBridgeNode.stopLinearVelocity();
    }


    public void setBaseVelocity(float linearVelocity, float angularVelocity) {mBridgeNode.setBaseVelocity(linearVelocity, angularVelocity);}

    public void setObstacleAvoidance(){mBridgeNode.setObstacleAvoidance();}

    public void changeParameters(String param, int i) {
        depthimageToLaserscanNode.setParameters(param,i);
        nodeMainExecutor.shutdownNodeMain(depthimageToLaserscanNode);
        startDepthimageToLaserscan();
    }

    public void drive(int i, int i1) {

    }
}
