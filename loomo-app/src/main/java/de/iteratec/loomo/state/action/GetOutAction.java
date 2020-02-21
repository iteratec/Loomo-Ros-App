package de.iteratec.loomo.state.action;

import android.os.AsyncTask;
import de.iteratec.loomo.ros.RosService;


public class GetOutAction extends Action {


    public GetOutAction() {
        super("<<<<<<<Initialising Navigation");
    }


    @Override
    public void work() {
        new LongOperation().execute("");
    }

    private class LongOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //TODO
            //if(RosService.getInstance().checkDistances()) {
            RosService.getInstance().moveForward(-0.7f, 4);
            //}
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


