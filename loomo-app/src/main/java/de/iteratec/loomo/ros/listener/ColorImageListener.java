package de.iteratec.loomo.ros.listener;

import android.graphics.Bitmap;
import android.util.Log;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.sdk.vision.calibration.Intrinsic;
import com.segway.robot.sdk.vision.frame.Frame;
import com.segway.robot.sdk.vision.stream.StreamType;
import de.iteratec.loomo.util.Utils;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.ros.internal.message.MessageBuffers;
import org.ros.message.Time;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import sensor_msgs.CameraInfo;
import sensor_msgs.CompressedImage;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ColorImageListener implements Vision.FrameListener {
    private static final String TAG = "ColorImageListener";

    private static final String RS_COLOR_OPTICAL_FRAME = "rsdepth_center_neck_fix_frame";
    public static final String JPEG = "jpeg";
    public static final String PLUMB_BOB = "plumb_bob";

    //the listener for the color images of the realsense camera
    private double lastFrameStamp = 0.d; // in millisecond

    private Bitmap mRsColorBitmap;
    private ChannelBufferOutputStream mRsColorOutStream;
    private ConnectedNode connectedNode;

    //the publishers used by this node.
    private final Publisher<CompressedImage> mRsColorCompressedPubr;
    private final Publisher<CameraInfo> mRsDepthInfoPubr;

    //the pool for the sensor publisher threads
    private ScheduledThreadPoolExecutor mPool;

    private Intrinsic mRsColorIntrinsic;
    private int mRsColorWidth = 640;
    private int mRsColorHeight = 480;
    private int lastFrameID;

    private double[] p;
    private double[] k;
    private double[] d;
    private int framesProcessed;
    private double frameDropdiff;

    public ColorImageListener(ConnectedNode connectedNode, Publisher<CompressedImage> mRsColorCompressedPubr, Publisher<CameraInfo> mRsDepthInfoPubr, Intrinsic mRsColorIntrinsic, int mRsColorWidth, int mRsColorHeight) {
        this.lastFrameID = 0;
        this.framesProcessed = 0;
        this.mRsColorCompressedPubr = mRsColorCompressedPubr;
        this.mRsDepthInfoPubr = mRsDepthInfoPubr;

        this.connectedNode = connectedNode;
        mPool = new ScheduledThreadPoolExecutor(2, new ThreadPoolExecutor.DiscardOldestPolicy());

        mRsColorOutStream = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());

        this.mRsColorWidth = mRsColorWidth;
        this.mRsColorHeight = mRsColorHeight;
        this.mRsColorIntrinsic = mRsColorIntrinsic;

        this.initCameraInfo(mRsColorIntrinsic);

        mRsColorBitmap = Bitmap.createBitmap(mRsColorWidth, mRsColorHeight, Bitmap.Config.ARGB_8888);

    }

    @Override
    public void onNewFrame(int streamType, Frame frame) {
        if(true){
            frame=null;
            return;
        }

        if (streamType != StreamType.COLOR) {
            Log.e(TAG, "onNewFrame@mRsColorListener: stream type not COLOR! THIS IS A BUG");
            return;
        }
        double stampMsecond = Utils.platformStampInMillis(frame.getInfo().getPlatformTimeStamp());
//        logger.logIfAllowed(TAG, "timeStamp: " + stampMsecond);
        double lastDiff = stampMsecond - lastFrameStamp;
//        logger.logIfAllowed(TAG, "frame diff: " + lastDiff);

        //Dropping frames that are older than frameDropDiff in ms;
        frameDropdiff = 31;
        if (lastFrameStamp > 0.1 && Math.abs(lastDiff) > frameDropdiff) {
//            logger.logIfAllowed(TAG, "onNewFrame@mRsColorListener: dropped frame diff is: " + lastDiff + "ms");
            lastFrameStamp = stampMsecond;

            return;
        }

        try {

            lastFrameStamp = stampMsecond;

//                Time currentTime = mConnectedNode.getCurrentTime();
            Time currentTime = new Time(stampMsecond / 1000);
            mRsColorBitmap.copyPixelsFromBuffer(frame.getByteBuffer()); // copy once
            final CameraInfo tempInfo = mRsDepthInfoPubr.newMessage();
            final CompressedImage image = mRsColorCompressedPubr.newMessage();

            tempInfo.setWidth(mRsColorWidth);
            tempInfo.setHeight(mRsColorHeight);
            tempInfo.setK(k);
            tempInfo.setD(d);
            tempInfo.setP(p);
            tempInfo.setDistortionModel(PLUMB_BOB);
            tempInfo.getRoi().setDoRectify(true);
            image.setFormat(JPEG);
            image.getHeader().setStamp(currentTime);
            image.getHeader().setFrameId(RS_COLOR_OPTICAL_FRAME);

            mRsColorBitmap.compress(Bitmap.CompressFormat.JPEG, 100, mRsColorOutStream);
            image.setData(mRsColorOutStream.buffer().copy());              // copy twice
            mRsColorOutStream.buffer().clear();

            //Duration diff = connectedNode.getCurrentTime().subtract(currentTime);
            //logger.logIfAllowed(TAG, String.format("publishRsColor: diff[%g]ms, time[%d], ros time[%d]",
            //   (double) diff.totalNsecs() / 1.0E6D, frame.getInfo().getPlatformTimeStamp(), currentTime.totalNsecs()));

            //imageTransportNode.setImage(image);

            mPool.schedule(new Runnable() {
                @Override
                public void run() {
                    mRsColorCompressedPubr.publish(image);
                    mRsDepthInfoPubr.publish(tempInfo);
                }
            }, 0, TimeUnit.MILLISECONDS);
        } catch (RuntimeException re) {
            Log.w(TAG, "Problem compressing image" + re.getMessage(), re);
        }

    }

    private void initCameraInfo(Intrinsic mRsDepthIntrinsic) {
        this.p = new double[12];
        p[0] = mRsDepthIntrinsic.focalLength.x;
        p[1] = 0;
        p[2] = mRsDepthIntrinsic.principal.x;
        p[3] = 0;
        p[4] = 0;
        p[5] = mRsDepthIntrinsic.focalLength.y;
        p[6] = mRsDepthIntrinsic.principal.y;
        p[7] = 0;
        p[8] = 0;
        p[9] = 0;
        p[10] = 1;
        p[11] = 0;

        this.k = new double[9];
        k[0] = mRsDepthIntrinsic.focalLength.x;
        k[1] = 0;
        k[2] = mRsDepthIntrinsic.principal.x;
        k[3] = 0;
        k[4] = mRsDepthIntrinsic.focalLength.y;
        k[5] = mRsDepthIntrinsic.principal.y;
        k[6] = 0;
        k[7] = 0;
        k[8] = 1;

        this.d = Doubles.toArray(Floats.asList(mRsDepthIntrinsic.distortion.value.clone()));
    }

}
