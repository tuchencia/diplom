package com.example.car_helper;

public class Fine {
    private String number; // Номер штрафа
    private String date;   // Дата нарушения
    private String amount; // Сумма штрафа
    private String status; // Статус оплаты

    public Fine(String number, String date, String amount, String status) {
        this.number = number;
        this.date = date;
        this.amount = amount;
        this.status = status;
    }

    public String getNumber(){
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Геттеры и сеттеры
}
