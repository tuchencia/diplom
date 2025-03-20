package com.example.car_helper;

import java.util.Date;

public class Expense {
    private String description;
    private double amount;
    private Date date;
    private String comment; // Новое поле для комментария

    public Expense(String description, double amount, Date date, String comment) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.comment = comment;
    }

    // Геттеры и сеттеры
    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}