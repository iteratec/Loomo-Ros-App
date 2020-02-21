package de.iteratec.loomo.ros.nodes;

import android.util.Log;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.NativeNodeMain;
import org.ros.node.Node;
import org.ros.node.parameter.ParameterTree;

/**
 * Created by maximilian on 28.08.18.
 */

public class DepthimageToLaserScanNode extends NativeNodeMain {
    private static final String TAG = "DepthimageToLaserScan";
    private static final String LIB_NAME = "depthimage_to_laserscan_jni";
    public static final String NODE_NAME = "depthimage_to_laserscan";
    private ConnectedNode connectedNode;

    public DepthimageToLaserScanNode() {
        super(LIB_NAME);
    }

    public DepthimageToLaserScanNode(String[] remappingArguments) {
        super(LIB_NAME, remappingArguments);
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        super.onStart(connectedNode);
        this.connectedNode = connectedNode;

        //setParameters();
        Log.d(TAG, "onStartcalled" + connectedNode.toString());
        //Setting move_base general Parameters

    }
    public void setParameters(String namespace, Object value) {
        namespace="/"+NODE_NAME +"/"+ namespace;
        ParameterTree params = connectedNode.getParameterTree();
        if(value instanceof Integer) {
            params.set(namespace, (int)value);
        }else if(value instanceof Double){
            params.set(namespace,(double)value);
        }
        else {
            throw new IllegalArgumentException("Not a supportet parameter type");
        }


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
