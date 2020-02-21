package de.iteratec.loomo.interaction;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.List;
import java.util.Observable;

import de.iteratec.loomo.LoomoApplication;


public class WifiConnectionService extends Observable{

    private final String hhNetworkSSID = "iteradev";
    private final String hhNetworkPass = "REPLACEME";
    private WifiManager wifiManager;
    private static WifiConnectionService instance;

    public WifiConnectionService(){
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + hhNetworkSSID + "\"";
        conf.preSharedKey = "\""+ hhNetworkPass +"\"";
        wifiManager = (WifiManager) LoomoApplication.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }

        wifiManager.addNetwork(conf);

    }

    public static synchronized WifiConnectionService getInstance() {
        if (instance == null) {
            instance = new WifiConnectionService();
        }
        return instance;
    }

    public void connectToWifi(){
        wifiManager.disconnect();
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + hhNetworkSSID + "\"")) {
                //wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reassociate();
                break;
            }
        }
    }

}
