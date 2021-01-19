package com.example.agenda_familial;

public class Events {
    String Event, Time,Date,Month,Year;

    public String getEvent() {
        return Event;
    }

    public void setEvent(String event) {
        Event = event;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getMonth() {
        return Month;
    }

    public void setMonth(String month) {
        Month = month;
    }

    public String getYear() {
        return Year;
    }

    public void setYear(String year) {
        Year = year;
    }

    public Events(String event, String time, String date, String month, String year) {
        this.Event = event;
        this.Time = time;
        this.Date = date;
        this.Month = month;
        this.Year = year;
    }
}
