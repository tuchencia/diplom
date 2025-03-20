package com.example.car_helper;

import java.util.Date;

public class PlannedExpense {
    private String description;
    private Date date;
    private String comment;

    public PlannedExpense(String description, Date date, String comment) {
        this.description = description;
        this.date = date;
        this.comment = comment;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }
}