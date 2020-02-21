package de.iteratec.loomo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import de.iteratec.loomo.state.Event;
import de.iteratec.loomo.state.StateMachine.State;
import de.iteratec.loomo.state.StateService;
import de.iteratec.loomo.state.UseCaseStateMachine;

public class WifiBroadcastReceiver extends BroadcastReceiver{
    private final String hhssid = "\"" + "iteradev-HAM" + "\"";


    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(info != null && info.isConnected()) {

            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            String ssid= wifiManager.getConnectionInfo().getSSID();
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if(hhssid.equals(ssid) && networkInfo.isConnected()){
                State state = StateService.getInstance().getSm().getState();
                StateService stateService = StateService.getInstance();
                if(state==UseCaseStateMachine.CONNECT_TO_WIFI) {
                    stateService.triggerEvent(Event.FINISHED_WIFI_CONNECTION);

                }
            }
        }
    }
}

