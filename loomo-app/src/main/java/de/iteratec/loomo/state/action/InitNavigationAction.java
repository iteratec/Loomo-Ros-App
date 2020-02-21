package de.iteratec.loomo.state.action;

import android.os.AsyncTask;

import de.iteratec.loomo.interaction.SpeakService;
import de.iteratec.loomo.ros.RosService;
import de.iteratec.loomo.state.Event;
import de.iteratec.loomo.state.StateService;

public class InitNavigationAction extends Action {

    private String text;

    public InitNavigationAction(String text) {
        super("<<<<<<<Initialising Navigation");
        this.text=text;
    }

    @Override
    public void work() {
        SpeakService.getInstance().say(text);
        new LongOperation().execute("");

    }

    private class LongOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            RosService.getInstance().initialiseNavigation();

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            StateService state = StateService.getInstance();
            state.triggerEvent(Event.FINISHED_NAV_INIT);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
