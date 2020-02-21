package de.iteratec.loomo.state.action;

import de.iteratec.loomo.interaction.WifiConnectionService;
import de.iteratec.loomo.location.Location;
import de.iteratec.loomo.location.LocationService;
import de.iteratec.loomo.state.Event;
import de.iteratec.loomo.state.StateService;

public class ConnectToWifiAction extends Action {


    public ConnectToWifiAction() {
        super("<<<<< ENTER CONNECT_TO_WIFI");
    }

    @Override
    public void work() {
        if(LocationService.getInstance().getLocation()== Location.MUC){
            StateService.getInstance().triggerEvent(Event.FINISHED_WIFI_CONNECTION);
            return;

        }
        WifiConnectionService.getInstance().connectToWifi();
    }

}
