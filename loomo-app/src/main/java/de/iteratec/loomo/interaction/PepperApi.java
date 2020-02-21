package de.iteratec.loomo.interaction;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PepperApi {

    @GET("sayText?{text}")
    Call<Void> say(@Path("text") String text);

}
