package de.iteratec.loomo.state.action;

import android.os.AsyncTask;

import de.iteratec.loomo.interaction.SpeakService;
import de.iteratec.loomo.location.Floor;
import de.iteratec.loomo.location.Location;
import de.iteratec.loomo.location.LocationService;
import de.iteratec.loomo.ros.RosService;

public class InitOGNavigationAction extends Action {

    public InitOGNavigationAction() {
        super("<<<<<<<Initialising Navigation");
    }

    @Override
    public void work() {
        if(LocationService.getInstance().getLocation()== Location.MUC){
            return;
        }
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
            SpeakService.getInstance().say("Neue Karte wurde erfolgreich geladen");
        }

        @Override
        protected void onPreExecute() {
            LocationService.getInstance().getLocation().setActiveFloor(Floor.OG);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}

