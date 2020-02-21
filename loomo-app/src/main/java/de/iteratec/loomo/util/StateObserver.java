package de.iteratec.loomo.util;

import android.util.Log;

import de.iteratec.loomo.activity.RosDeveloperActivity;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by maximilian on 09.10.18.
 */

public class StateObserver implements Observer{
  private static final String TAG = "StateObserver";

  RosDeveloperActivity activity;

  public StateObserver(RosDeveloperActivity activity) {
    this.activity = activity;
  }


  @Override
  public void update(Observable observable, Object o) {
    Log.d(TAG, "updating observers");
    activity.updateActiveState();
  }
}
