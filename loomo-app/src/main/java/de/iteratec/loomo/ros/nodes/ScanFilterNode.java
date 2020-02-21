package de.iteratec.loomo.ros.nodes;

import android.util.Log;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.NativeNodeMain;
import org.ros.node.Node;

/**
 * Created by maximilian on 18.09.18.
 */

public class ScanFilterNode extends NativeNodeMain {
    private static final String TAG = "ScanFilterNode";
    private static final String LIB_NAME = "laser_filter_jni";
    public static final String NODE_NAME = "laser_filter";
    private String dynamicNodeName;
    private ConnectedNode connectedNode;

    public ScanFilterNode() {
        super(LIB_NAME);
        dynamicNodeName =NODE_NAME;
    }


    public ScanFilterNode(String[] remappingArguments) {
        super(LIB_NAME, remappingArguments);
        dynamicNodeName =NODE_NAME;

    }
    public ScanFilterNode(String node_name, String[] remappingArguments) {
        super(LIB_NAME, remappingArguments);
        dynamicNodeName =node_name;

    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        super.onStart(connectedNode);
        this.connectedNode = connectedNode;
        Log.d(TAG, "onStartcalled " + NODE_NAME);
        //setParameters();
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of(NODE_NAME);
    }


    @Override
    protected native int execute(String rosMasterUri, String rosHostname, String rosNodeName, String[] remappingArguments);

    //Stub
    @Override
    protected native int shutdown();


    @Override
    public void onError(Node node, Throwable throwable) {
        if (super.executeReturnCode != 0) {
            // Handle execution error
        } else if (super.shutdownReturnCode != 0) {
            // Handle shutdown error
        }
    }


}
