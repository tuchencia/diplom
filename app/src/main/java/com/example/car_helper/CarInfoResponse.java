package com.example.car_helper;

public class CarInfoResponse {
    private String brand; // Марка
    private String model; // Модель
    private String year;  // Год выпуска
    private String vin;   // VIN

    // Геттеры и сеттеры
    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getYear() {
        return year;
    }

    public String getVin() {
        return vin;
    }
}
