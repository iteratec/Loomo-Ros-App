package de.iteratec.loomo.activity;

import android.os.Bundle;
import android.util.Log;
import de.iteratec.loomo.R;
import de.iteratec.loomo.ros.SimplePublisherNode;
import de.iteratec.loomo.util.ToastUtil;
import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends RosActivity {

    private static final String LOG_TAG = "MainActivity";

    public MainActivity() throws URISyntaxException {
        super("RosAndroidExample", "RosAndroidExample", new URI("http://192.168.1.139:11311/"));
    }

    private String getDefaultHostAddress() {
        return InetAddressFactory.newNonLoopback().getHostAddress();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToastUtil.showToast(this, "Test activity started.");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        Log.i(LOG_TAG, "Init test activity");

        NodeMain node = new SimplePublisherNode();

        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(getMasterUri());

        nodeMainExecutor.execute(node, nodeConfiguration);
    }
}
