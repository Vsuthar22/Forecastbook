package com.example.forecastbook.Api;

import com.example.forecastbook.Models.CurrentWeather;
import com.example.forecastbook.Models.UVindex;
import com.example.forecastbook.Models.Weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {



    @GET("weather")
    Call<CurrentWeather> getallstats(
            @Query("q") String cityName,
            @Query("appid") String apikey
    );



    @GET("uvi")
    Call<UVindex> getUVindex(
            @Query("lat") String lattitude,
            @Query("lon") String longtitude,
            @Query("appid") String apikey
    );




    @GET("forecast")
    Call<Weather> getlongterm(
            @Query("lat") String lattitude,
            @Query("lon") String longtitude,
            @Query("appid") String apikey
    );


    @GET("weather")
    Call<CurrentWeather> currentlocationdata(
            @Query("lat") String lattitude,
            @Query("lon") String longtitude,
            @Query("appid") String apikey
    );


    @GET("uvi")
    Call<UVindex> currentlocationUV(
            @Query("lat") String lattitude,
            @Query("lon") String longtitude,
            @Query("appid") String apikey
    );


}

