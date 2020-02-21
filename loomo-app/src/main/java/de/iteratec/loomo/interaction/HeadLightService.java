package de.iteratec.loomo.interaction;

import android.util.Log;
import com.segway.robot.sdk.locomotion.head.Head;

public class HeadLightService {

    private static final String TAG = "HeadLightService";

    private static final int OFF = 0;
    public static final int BLUE = 1;
    private static final int BLUE_CIRCULAR = 2;
    private static final int BLUE_PULSATE_CIRCULAR = 3;
    private static final int BLUE_PULSATE_WHITE = 4;
    private static final int RED_BLINKING = 5;
    private static final int GREEN_BLINKING = 6;
    private static final int GREEN_PULSATE = 7;
    private static final int YELLOW_PULSATE = 8;
    private static final int BLUE_PULSATE = 9;
    private static final int PURPLE_PULSATE = 10;
    private static final int PURPLE_PULSATE_CIRCULAR = 11;
    private static final int BLUE_PULSATE_CIRCULAR_2 = 12;

    private static HeadLightService instance;
    private Head headApi;

    public static synchronized HeadLightService getInstance() {
        if (instance == null) {
            instance = new HeadLightService();
        }
        return instance;
    }

    public void init(Head head) {
        this.headApi = head;
        this.headApi.setHeadLightMode(0);
    }

    private HeadLightService() {
    }

    public void setFollowMe() {
        setMode(PURPLE_PULSATE);
    }

    private void setMode(int color) {
        if (this.headApi != null) {
            Log.d(TAG, "Setting color of headlight: " + color + ", Bind state: " + this.headApi.isBind());
            this.headApi.setHeadLightMode(color);
        } else {
            Log.w(TAG, "No headapi set.");
        }
    }

    public void setThinking() {
        setMode(BLUE_PULSATE);
    }

    public void setListening() {
        setMode(BLUE_PULSATE_CIRCULAR);
    }

    public void setWaiting() {
        setMode(BLUE_CIRCULAR);
    }

    public void setOff() {
        setMode(OFF);
    }

    public void setOn() {
        setMode(BLUE);
    }

    public void setError() {
        setMode(RED_BLINKING);
    }
}
