package de.iteratec.loomo.interaction;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Retrofit;

public class PepperConnectionService {

    private static PepperConnectionService instance;
    PepperApi pepperApi;

    private PepperConnectionService(){
        Retrofit retrofit = new Retrofit.Builder()
                //TODO
                .baseUrl("http://PEPPER-IP:8966/")
                .build();
        pepperApi = retrofit.create(PepperApi.class);
    }


    public static synchronized PepperConnectionService getInstance() {
        if (instance == null) {
            instance = new PepperConnectionService();
        }
        return instance;
    }

    public void testMessageToPepper(){
        Call<Void> repos = pepperApi.say("Test");
        try {
            repos.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
