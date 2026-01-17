package com.mountrich.krushimitra.models;
public class DailyForecast {

    public long dt;
    public int timezone;
    public String day;
    public String condition;
    public String icon;
    public int minTemp;
    public int maxTemp;

    public DailyForecast(long dt, int timezone, String day,
                         String condition, String icon,
                         int minTemp, int maxTemp) {

        this.dt = dt;
        this.timezone = timezone;
        this.day = day;
        this.condition = condition;
        this.icon = icon;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public long getDt() {
        return dt;
    }

    public int getTimezone() {
        return timezone;
    }
}
