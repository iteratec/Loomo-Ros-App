package de.iteratec.loomo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartRosDeveloperActivityAtBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
 /*       if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent activityIntent = new Intent(context, RosDeveloperActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }*/
        String robotMode = "com.segway.robot.action.TO_ROBOT";
        String sbvMode = "com.segway.robot.action.TO_SBV";
        if (robotMode.equals(intent.getAction())) {
            Intent activityIntent = new Intent(context, RosDeveloperActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        } else if (sbvMode.equals(intent.getAction())) {
            Log.i("SBV", "SBV");
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }
}

