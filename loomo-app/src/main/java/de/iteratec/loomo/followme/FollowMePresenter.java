package de.iteratec.loomo.followme;

import android.util.Log;

import com.segway.robot.algo.dts.BaseControlCommand;
import com.segway.robot.algo.dts.DTSPerson;
import com.segway.robot.algo.dts.PersonTrackingListener;
import com.segway.robot.algo.dts.PersonTrackingProfile;
import com.segway.robot.algo.dts.PersonTrackingWithPlannerListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.vision.DTS;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.support.control.HeadPIDController;

import de.iteratec.loomo.LoomoApplication;


/**
 * @author jacob
 */

public class FollowMePresenter {

    private static final String TAG = "FollowMePresenter";

    private static final int TIME_OUT = 10 * 1000;

    private HeadPIDController mHeadPIDController = new HeadPIDController();
    private Vision mVision;
    private Head mHead;
    private Base mBase;


    private boolean isVisionBind;
    private boolean isHeadBind;
    private boolean isBaseBind;

    private DTS mDts;

    /**
     * true means obstacle avoidance function is available, otherwise false
     */
    private boolean isObstacleAvoidanceOpen;
    private PersonTrackingProfile mPersonTrackingProfile;

    private long startTime;

    private RobotStateType mCurrentState;

    private static FollowMePresenter instance;


    public enum RobotStateType {
        INITIATE_DETECT, TERMINATE_DETECT, INITIATE_TRACK, TERMINATE_TRACK
    }

    public FollowMePresenter() {

    }

    public static synchronized FollowMePresenter getInstance() {
        if (instance == null) {
            instance = new FollowMePresenter();
        }
        return instance;
    }


    public void startPresenter() {

        mVision = Vision.getInstance();
        mHead = Head.getInstance();
        mBase = Base.getInstance();
        setHead();
        setVision();
        setBase();
        /**
         * the second parameter is the distance between loomo and the followed target. must > 1.0f
         */
        mPersonTrackingProfile = new PersonTrackingProfile(3, 1.0f);
    }


    public void stopPresenter() {
        if (mDts != null) {
            mDts.stop();
            mDts = null;
        }
        mHeadPIDController.stop();
        resetHead();
    }


    public void actionInitiateTrack() {
        if (mCurrentState == RobotStateType.INITIATE_TRACK) {
            return;
        }
        startTime = System.currentTimeMillis();
        mCurrentState = RobotStateType.INITIATE_TRACK;
        if (isObstacleAvoidanceOpen) {
            mDts.startPlannerPersonTracking(null, mPersonTrackingProfile, 60 * 1000 * 1000, mPersonTrackingWithPlannerListener);
        } else {
            mDts.startPersonTracking(null, 60 * 1000 * 1000, mPersonTrackingListener);
        }
    }

    private void setHead() {
        if (mHead.isBind()) {
            Log.i(TAG, "Head is bind");
            isHeadBind = true;
            resetHead();
            mHeadPIDController.init(new HeadControlHandlerImpl(mHead));
            mHeadPIDController.setHeadFollowFactor(1.0f);
        } else {
            mHead.bindService(LoomoApplication.getContext(), mHeadBindStateListener);
            Log.i(TAG, "Head is now bind");

        }

    }

    private void setVision() {
        isVisionBind = true;
        mDts = mVision.getDTS();
        mDts.setVideoSource(DTS.VideoSource.CAMERA);
        mDts.start();
    }

    private void setBase() {
        if (mBase.isBind()) {
            Log.i(TAG, "base is bind");
            isBaseBind = true;
        }
    }

    /**************************  detecting and tracking listeners   *****************************/

    /**
     * person tracking without obstacle avoidance
     */
    private PersonTrackingListener mPersonTrackingListener = new PersonTrackingListener() {
        @Override
        public void onPersonTracking(DTSPerson person) {
            if (person == null) {
                if (System.currentTimeMillis() - startTime > TIME_OUT) {
                    resetHead();
                }
                return;
            }
            startTime = System.currentTimeMillis();
            if (isServicesAvailable()) {
                mHead.setMode(Head.MODE_ORIENTATION_LOCK);
                mHeadPIDController.updateTarget(person.getTheta(), person.getDrawingRect(), 480);

                mBase.setControlMode(Base.CONTROL_MODE_FOLLOW_TARGET);
                float personDistance = person.getDistance();
                // There is a bug in DTS, while using person.getDistance(), please check the result
                // The correct distance is between 0.35 meters and 5 meters
                if (personDistance > 0.35 && personDistance < 5) {
                    float followDistance = (float) (personDistance - 2f);
                    float theta = person.getTheta();
                    mBase.updateTarget(followDistance, theta);
                }
            }
        }

        @Override
        public void onPersonTrackingResult(DTSPerson person) {

        }

        @Override
        public void onPersonTrackingError(int errorCode, String message) {
            mCurrentState = null;
        }
    };

    /**
     * person tracking with obstacle avoidance
     */
    private PersonTrackingWithPlannerListener mPersonTrackingWithPlannerListener = new PersonTrackingWithPlannerListener() {
        @Override
        public void onPersonTrackingWithPlannerResult(DTSPerson person, BaseControlCommand baseControlCommand) {
            if (person == null) {
                if (System.currentTimeMillis() - startTime > TIME_OUT) {
                    resetHead();
                }
                return;
            }
            if (isServicesAvailable()) {
                startTime = System.currentTimeMillis();
                mHead.setMode(Head.MODE_ORIENTATION_LOCK);
                mHeadPIDController.updateTarget(person.getTheta(), person.getDrawingRect(), 480);

                switch (baseControlCommand.getFollowState()) {
                    case BaseControlCommand.State.NORMAL_FOLLOW:
                        setBaseVelocity(baseControlCommand.getLinearVelocity(), baseControlCommand.getAngularVelocity());
                        break;
                    case BaseControlCommand.State.HEAD_FOLLOW_BASE:
                        mBase.setControlMode(Base.CONTROL_MODE_FOLLOW_TARGET);
                        float personDistance = person.getDistance();
                        // There is a bug in DTS, while using person.getDistance(), please check the result
                        // The correct distance is between 0.35 meters and 5 meters
                        if (personDistance > 0.35 && personDistance < 5) {
                            float followDistance = (float) (personDistance - 0.75f);
                            mBase.updateTarget(followDistance, person.getTheta());
                        }
                        break;
                    case BaseControlCommand.State.SENSOR_ERROR:
                        setBaseVelocity(0, 0);
                        break;
                }
            }
        }

        @Override
        public void onPersonTrackingWithPlannerError(int errorCode, String message) {
            mCurrentState = null;
        }
    };

    private void setBaseVelocity(float linearVelocity, float angularVelocity) {
        mBase.setControlMode(Base.CONTROL_MODE_RAW);
        mBase.setLinearVelocity(linearVelocity);
        mBase.setAngularVelocity(angularVelocity);
    }

    /***********************************  switch mode  *******************************************/

    public void setObstacleAvoidanceOpen(boolean isObstacleAvoidanceOpen) {
        this.isObstacleAvoidanceOpen = isObstacleAvoidanceOpen;
    }

    public boolean getObstacleAvoidanceOpen() {
        closeDetectorTrack();
        return isObstacleAvoidanceOpen;
    }

    public void closeDetectorTrack() {
        mDts.stopDetectingPerson();
        mDts.stopPersonTracking();
        mDts.stopPlannerPersonTracking();
        mCurrentState = null;
    }


    /***************************************** bind services **************************************/

    private ServiceBinder.BindStateListener mHeadBindStateListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            isHeadBind = true;
            resetHead();
            mHeadPIDController.init(new HeadControlHandlerImpl(mHead));
            mHeadPIDController.setHeadFollowFactor(1.0f);
        }

        @Override
        public void onUnbind(String reason) {
            isHeadBind = false;
        }
    };


    private boolean isServicesAvailable() {
        return isVisionBind && isHeadBind && isBaseBind;
    }

    /**
     * reset head when timeout
     */
    private void resetHead() {
        mHead.setMode(Head.MODE_SMOOTH_TACKING);
        mHead.setWorldYaw(0);
        mHead.setWorldPitch(0.6f);
    }

}
