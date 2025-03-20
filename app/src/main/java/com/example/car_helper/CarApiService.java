package com.example.car_helper;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface CarApiService {
    @GET("1.0/offer/cars") // Пример эндпоинта
    Call<CarInfoResponse> getCarInfo(
            @Header("Authorization") String apiKey, // API-ключ
            @Query("license_plate") String licensePlate // Госномер
    );
}
