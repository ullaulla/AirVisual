package com.example.main_drawer.ui.gallery;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface AirVisualApi {
    @Headers("Accept: application/json")
    @GET("v2/nearest_city?key=0793eeb1-2901-46d3-b0b0-b1ff3a0aee99")
    Call<FineDustResult> getFineByGeo(@Query("lat") double lat, @Query("lng") double lng, @Query("m") double m);
}
