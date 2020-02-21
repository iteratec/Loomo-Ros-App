package de.iteratec.loomo.state.action;

import android.os.AsyncTask;
import android.util.Log;

import de.iteratec.loomo.interaction.HeadLightService;

import de.iteratec.loomo.ros.RosService;

public class EnterElevatorAction extends Action {

    public EnterElevatorAction() {
        super("<<<<< Enter ELEVATOR");
        Log.i("elevator", ""+EnterElevatorAction.class);
        }

    @Override
    public void work() {
        // TODO
        HeadLightService.getInstance().setThinking();
        new LongOperation().execute("");
        //RosService.getInstance().turnAround(1,4);
        //RosService.getInstance().stopAngularVelocity();
    }

    private class LongOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //if(RosService.getInstance().checkDistances()) {
                RosService.getInstance().moveForward(0.7f,4);
                RosService.getInstance().stopLinearVelocity();
                RosService.getInstance().turnAround(1,4);
                RosService.getInstance().stopAngularVelocity();
            //}

            //RosService.getInstance().setObstacleAvoidance();
            //RosService.getInstance().moveForward(0.7f, 4);
            //RosService.getInstance().stopLinearVelocity();

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}
