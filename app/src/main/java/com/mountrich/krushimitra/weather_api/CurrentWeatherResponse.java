package com.mountrich.krushimitra.weather_api;
import java.util.List;

public class CurrentWeatherResponse {
    public Main main;
    public List<Weather> weather;
    public String name;
    public long dt;

    public class Main {
        public float temp;
        public float temp_min;
        public float temp_max;
    }

    public class Weather {
        public String main;
        public String icon;
    }
}
