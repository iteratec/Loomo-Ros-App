package de.iteratec.loomo.state.action;

import android.os.AsyncTask;
import de.iteratec.loomo.interaction.HeadLightService;
import de.iteratec.loomo.interaction.SpeakService;
import de.iteratec.loomo.location.Location;
import de.iteratec.loomo.location.LocationService;
import de.iteratec.loomo.navigation.Destination;
import de.iteratec.loomo.navigation.NavigationService;
import de.iteratec.loomo.ros.RosService;
import de.iteratec.loomo.state.Event;
import de.iteratec.loomo.state.StateService;

public class EnterStartTourAction extends Action {

    private Destination destination;
    private String text;

    public EnterStartTourAction(Destination destination, String text) {
        super("<<<<< Enter START_TOUR");
        this.destination =destination;
        this.text=text;
    }

    @Override
    public void work() {
        if(destination==Destination.ELEVATOR_WAITING_HALL && LocationService.getInstance().getLocation()==Location.MUC){
            new LongOperation().execute("");
            return;
        }
        SpeakService.getInstance().say(text);
        NavigationService.getInstance().startNavigation(destination);
        HeadLightService.getInstance().setOn();
    }


    //RosService.getInstance().turnAround(1,4);
    //RosService.getInstance().stopAngularVelocity();

private class LongOperation extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        //if(RosService.getInstance().checkDistances()) {
        RosService.getInstance().moveForward(0.7f,4);
        RosService.getInstance().stopLinearVelocity();
        StateService.getInstance().triggerEvent(Event.DESTINATION_ARRIVED);

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
