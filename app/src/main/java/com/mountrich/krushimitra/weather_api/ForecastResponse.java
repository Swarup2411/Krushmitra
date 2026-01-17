package com.mountrich.krushimitra.weather_api;

import java.util.List;


public class ForecastResponse {

    public List<ForecastItem> list;
    public City city;   // ✅ ADD THIS

    public class City {
        public String name;
        public int timezone;
        public String country;
    }

    public class ForecastItem {
        public long dt;
        public Main main;
        public List<Weather> weather;
        public String dt_txt;
    }

    public class Main {
        public float temp;
    }

    public class Weather {
        public String main;
        public String icon;
    }
}

