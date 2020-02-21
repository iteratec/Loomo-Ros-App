package de.iteratec.loomo.followme;

import com.segway.robot.sdk.locomotion.head.Angle;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.support.control.HeadPIDController;

/**
 * @author jacob
 */
public class HeadControlHandlerImpl implements HeadPIDController.HeadControlHandler {
    private Head mHead;
    private static HeadControlHandlerImpl instance;

    public HeadControlHandlerImpl(Head head) {
        mHead = head;
    }

    public static HeadControlHandlerImpl getInstance() {
        return instance;
    }

    @Override
    public float getJointYaw() {
        Angle angle = mHead.getHeadJointYaw();
        if (angle == null) {
            return 0;
        }
        return angle.getAngle();
    }

    @Override
    public float getJointPitch() {
        Angle angle = mHead.getHeadJointPitch();
        if (angle == null) {
            return 0;
        }
        return angle.getAngle();
    }

    @Override
    public void setYawAngularVelocity(float velocity) {
        mHead.setYawAngularVelocity(velocity);
    }

    @Override
    public void setPitchAngularVelocity(float velocity) {
        mHead.setPitchAngularVelocity(velocity);
    }
}
