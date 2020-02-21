package de.iteratec.loomo;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import de.iteratec.loomo.conversation.DroidSpeechConversationService;
import de.iteratec.loomo.interaction.SpeakService;
import de.iteratec.loomo.navigation.NavigationService;
import de.iteratec.loomo.ros.RosService;
import de.iteratec.loomo.state.StateService;
import org.ros.RosCore;
import org.ros.exception.RosRuntimeException;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LoomoApplication extends Application {

    private static final String LOG_TAG = "LoomoApplication";

    private ScheduledThreadPoolExecutor mPool;
    private static LoomoApplication mContext;
    private RosCore rosCore;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        setCustomErrorHandler();

        SpeakService.getInstance().init(this);
        DroidSpeechConversationService.getInstance().init(this);
        StateService.getInstance().init();

        mPool = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.DiscardOldestPolicy());
        startRosMaster();

        Log.i(LOG_TAG, "Starting application ... ");
    }

    private void startRosMaster() {
        mPool.schedule(new Runnable() {
            @Override
            public void run() {
                startMasterBlocking(false);
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SpeakService.getInstance().stop();
        NavigationService.getInstance().stop();
        RosService.getInstance().stop();
        stopRos();
    }

    private void stopRos() {
        Log.d(LOG_TAG, "Shutting down ros core.");
        this.rosCore.shutdown();
    }

    private void setCustomErrorHandler() {
        Log.d(LOG_TAG, "Set custom error handler");
        Thread.UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(
                new CustomExceptionHandler(currentHandler));
    }

    private void startMasterBlocking(boolean isPrivate) {
        String rosHostname = "192.168.1.139";
        if (isPrivate) {
            rosCore = RosCore.newPrivate();
        } else if (rosHostname != null) {
            rosCore = RosCore.newPublic("192.168.1.139", 11311);
        } else {
            rosCore = RosCore.newPublic(11311);
        }
        rosCore.start();
        try {
            rosCore.awaitStart();
        } catch (Exception e) {
            throw new RosRuntimeException(e);
        }
        Log.i(LOG_TAG, "ROS master URI: " + rosCore.getUri());
    }

}