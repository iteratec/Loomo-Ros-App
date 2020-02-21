package de.iteratec.loomo.ros.publisher;

import com.segway.robot.sdk.locomotion.sbv.Base;

public class BaseHolder {

    private Base mBase;

    public Base getmBase() {
        return mBase;
    }

    public void setmBase(Base mBase) {
        this.mBase = mBase;
    }

    public void refreshPoint() {
        mBase.cleanOriginalPoint();
    }
}
