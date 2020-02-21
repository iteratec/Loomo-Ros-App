package de.iteratec.loomo.ros.listener;

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
import sensor_msgs.Image;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DepthImageListener implements Vision.FrameListener {

    private static final String TAG = "DepthImageListener";
    public static final String UC_1 = "16UC1";

    //the listener for the depthimages
    private double lastFrameStamp = 0.d; // in millisecond

    //the name of the frame_id of the camera
    private static final String RS_DEPTH_OPTICAL_FRAME = "rsdepth_center_neck_fix_frame";

    private long lastDepthImage = 0;

    private Publisher<Image> mRsDepthPubr;
    private Publisher<CameraInfo> mRsDepthInfoPubr;

    //the pool for the sensor publisher threads
    private ScheduledThreadPoolExecutor mPool;

    private Queue<Long> mDepthStamps;

    private int mRsDepthWidth;
    private int mRsDepthHeight;

    private double[] p;
    private double[] k;
    private double[] d;
    private double frameDropdiff;

    public DepthImageListener(ConnectedNode connectedNode, Publisher<Image> mRsDepthPubr, Publisher<CameraInfo> mRsDepthInfoPubr, Intrinsic mRsDepthIntrinsic, int mRsDepthWidth, int mRsDepthHeight) {
//        logger.logIfAllowed(TAG, "Startet DepthImageListener");
        this.mRsDepthPubr = mRsDepthPubr;
        this.mRsDepthInfoPubr = mRsDepthInfoPubr;
        mPool = new ScheduledThreadPoolExecutor(2, new ThreadPoolExecutor.DiscardOldestPolicy());


        mDepthStamps = new ConcurrentLinkedDeque<>();

        this.mRsDepthWidth = mRsDepthWidth;
        this.mRsDepthHeight = mRsDepthHeight;

        initCameraInfo(mRsDepthIntrinsic);
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

    @Override
    public void onNewFrame(int streamType, final Frame frame) {
        if (streamType != StreamType.DEPTH) {
            Log.e(TAG, "onNewFrame@mRsDepthListener: stream type not DEPTH! THIS IS A BUG");
            return;
        }
        double stampMsecond = Utils.platformStampInMillis(frame.getInfo().getPlatformTimeStamp());
//        logger.logIfAllowed(TAG, "timeStamp: " + stampMsecond);

        double lastDiff = stampMsecond - lastFrameStamp;


        //Lod.d(TAG, "frame diff: " + lastDiff);
        //Dropping frames that are older than frameDropDiff in ms;
        frameDropdiff = 31;
        if (lastFrameStamp > 0.1 && Math.abs(lastDiff) > frameDropdiff) {
//            logger.logIfAllowed(TAG, "onNewFrame@DepthImageListener: dropped frame diff is: " + lastDiff + "ms");
            lastFrameStamp = stampMsecond;

            return;
        }
        lastFrameStamp = stampMsecond;
        mDepthStamps.add(frame.getInfo().getPlatformTimeStamp());
        Time currentTime = new Time(lastFrameStamp / 1000);

        final Image tempImage = mRsDepthPubr.newMessage();

        int imageSeq = tempImage.getHeader().getSeq();
        tempImage.setWidth(mRsDepthWidth);
        tempImage.setHeight(mRsDepthHeight);
        tempImage.setStep(mRsDepthWidth * 2);
        tempImage.setEncoding(UC_1);
        tempImage.getHeader().setStamp(currentTime);
        tempImage.getHeader().setFrameId(RS_DEPTH_OPTICAL_FRAME);

//        logger.logIfAllowed("DepthImageListener", String.format("image seq %d", imageSeq));
        if(imageSeq>lastDepthImage+1) {
            mPool.schedule(new Runnable() {
                @Override
                public void run() {
                    ChannelBufferOutputStream mRsDepthOutStream = new ChannelBufferOutputStream(
                        MessageBuffers.dynamicBuffer());
                    WritableByteChannel channel = Channels.newChannel(mRsDepthOutStream);
                    try {
                        channel.write(frame.getByteBuffer());
                        tempImage.setData(mRsDepthOutStream.buffer().copy());
                        if (tempImage.getData().array().length > 0) {
                            tempImage.getHeader().setFrameId("camera_frame");

                            final CameraInfo tempInfo = mRsDepthInfoPubr.newMessage();
                            tempInfo.setWidth(mRsDepthWidth);
                            tempInfo.setHeight(mRsDepthHeight);
                            tempInfo.setK(k);
                            tempInfo.setD(d);
                            tempInfo.setP(p);
                            tempInfo.setDistortionModel("plumb_bob");
                            tempInfo.getRoi().setDoRectify(false);
                            tempInfo.setHeader(tempImage.getHeader());
                            //tempInfo.getRoi().setHeight(mRsDepthHeight);
                            //tempInfo.getRoi().setWidth(mRsDepthWidth);
//                            logger.logIfAllowed(TAG, "publishing depthimage");
                            mRsDepthPubr.publish(tempImage);
                            mRsDepthInfoPubr.publish(tempInfo);
                        } else {
                            Log.w("DepthImage", "depthimage got no data");
                        }
                    } catch (Exception e) {
                        Log.e("DepthImage", e.toString());
                        Log.e(TAG,
                            String.format("publishRsDepth: IO Exception[%s]", e.getMessage()));
                    } finally {
                        mRsDepthOutStream.buffer().clear();
                        try {
                            channel.close();
                        } catch (IOException e) {
                            Log.e("DepthImage", e.toString());
                        }
                    }

                }
            }, 0, TimeUnit.MILLISECONDS);

            lastDepthImage = imageSeq;
        }
        //}


    }
//
//    private boolean copyImageToMessage(Frame frame, Image tempImage) {
//        try {
//            channel.write(frame.getByteBuffer());
//            tempImage.setData(mRsDepthOutStream.buffer().copy());
//            mRsDepthOutStream.buffer().clear();
//        } catch (IOException exception) {
//            Log.e(TAG, String.format("publishRsDepth: IO Exception[%s]", exception.getMessage()));
//            mRsDepthOutStream.buffer().clear();
//            return false;
//        }
//        return true;
//    }

}
