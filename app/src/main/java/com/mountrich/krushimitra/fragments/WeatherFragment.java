package com.mountrich.krushimitra.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.adapters.FiveDayForecastAdapter;
import com.mountrich.krushimitra.models.DailyForecast;
import com.mountrich.krushimitra.weather_api.CurrentWeatherResponse;
import com.mountrich.krushimitra.weather_api.ForecastResponse;
import com.mountrich.krushimitra.weather_api.RetrofitClient;
import com.mountrich.krushimitra.weather_api.WeatherApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherFragment extends Fragment {

    private static final String API_KEY = "1ecef33a983ec135fdd1f42844a86496";

    private TextView tvTemperature, tvWeatherType, tvLocation,tvError;
    private TextView tvAdvice1, tvAdvice2;
    private ImageView imgWeather;
    private RecyclerView rvFiveDay;
    private ProgressBar progressBar;
    View dummyLayout;
    ScrollView layoutContent;


    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        // Views
        tvTemperature = view.findViewById(R.id.tvTemp);
        tvWeatherType = view.findViewById(R.id.tvWeatherType);
        tvLocation = view.findViewById(R.id.tvLocation);

        tvAdvice1 = view.findViewById(R.id.tvAdvice1);
        tvAdvice2 = view.findViewById(R.id.tvAdvice2);
        imgWeather = view.findViewById(R.id.imgWeather);
        rvFiveDay = view.findViewById(R.id.rvSevenDay);
        tvError = view.findViewById(R.id.tvError);
        progressBar = view.findViewById(R.id.progressBar);

        dummyLayout = view.findViewById(R.id.dummyLayout);
        layoutContent = view.findViewById(R.id.layoutContent);


        rvFiveDay.setLayoutManager(new LinearLayoutManager(getContext()));

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());


        dummyLayout.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);

        getCurrentLocationWeather();

        return view;
    }

    // ================= LOCATION =================
    private void getCurrentLocationWeather() {

        if (!isAdded() || getContext() == null) return;

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (!isAdded() || location == null) return;

                    fetchWeatherByLocation(
                            location.getLatitude(),
                            location.getLongitude()
                    );
                });
    }


    // ================= API CALLS =================
    private void fetchWeatherByLocation(double lat, double lon) {

        WeatherApi api = RetrofitClient.getClient().create(WeatherApi.class);

        // ===== CURRENT WEATHER =====
        api.getCurrentWeather(lat, lon, "metric", API_KEY)
                .enqueue(new Callback<CurrentWeatherResponse>() {
                    @Override
                    public void onResponse(Call<CurrentWeatherResponse> call,
                                           Response<CurrentWeatherResponse> response) {

                        if (!isAdded() || getContext() == null) return;

                        if (!response.isSuccessful() || response.body() == null) return;

                        dummyLayout.setVisibility(View.GONE);
                        layoutContent.setVisibility(View.VISIBLE);

                        CurrentWeatherResponse data = response.body();

                        tvTemperature.setText(Math.round(data.main.temp) + "°C");
                        tvWeatherType.setText(data.weather.get(0).main);
                        tvLocation.setText(data.name);

                        long time = data.dt * 1000L;
//

                        String iconUrl =
                                "https://openweathermap.org/img/wn/" +
                                        data.weather.get(0).icon + "@2x.png";

                        if (!isAdded()) return;

                        Glide.with(getContext())
                                .load(iconUrl)
                                .into(imgWeather);

                        setFarmerAdvice(data.weather.get(0).main);
                    }

                    @Override
                    public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {
                        t.printStackTrace();
                        dummyLayout.setVisibility(View.GONE);
                        tvError.setVisibility(View.VISIBLE);


                        Log.d("TAG", "onFailure: "+t.getMessage());
                    }

                });

        // ===== FREE 5-DAY FORECAST =====
        api.getFiveDayForecast(lat, lon, "metric", API_KEY)
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(Call<ForecastResponse> call,
                                           Response<ForecastResponse> response) {

                        if (!isAdded() || getContext() == null) return;
                        if (!response.isSuccessful() || response.body() == null) return;

                        List<DailyForecast> forecastList = new ArrayList<>();
                        Map<String, DailyForecast> dayMap = new LinkedHashMap<>();

                        int timezone = response.body().city.timezone;


                        SimpleDateFormat input =
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        SimpleDateFormat dayFormat =
                                new SimpleDateFormat("EEE", Locale.getDefault());
                        dayFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                        for (ForecastResponse.ForecastItem item : response.body().list) {

                            // pick midday data
                            if (!item.dt_txt.contains("12:00:00")) continue;

                            long localTime =
                                    (item.dt + timezone) * 1000L;

                            String dayName =
                                    dayFormat.format(new Date(localTime));

                            dayMap.put(dayName, new DailyForecast(
                                    item.dt,
                                    timezone,
                                    dayName,
                                    item.weather.get(0).main,
                                    item.weather.get(0).icon,
                                    Math.round(item.main.temp - 2),
                                    Math.round(item.main.temp + 2)
                            ));
                        }


                        forecastList.addAll(dayMap.values());

                        if (!isAdded()) return;
                        FiveDayForecastAdapter adapter =
                                new FiveDayForecastAdapter(getContext(), forecastList);

                        rvFiveDay.setAdapter(adapter);
                    }

                    @Override
                    public void onFailure(Call<ForecastResponse> call, Throwable t) {

                    }
                });
    }



    // ================= FARMER ADVICE =================
    private void setFarmerAdvice(String condition) {

        if (condition.equalsIgnoreCase("Rain")) {
            tvAdvice1.setText("🚫 Do NOT spray pesticides");
            tvAdvice2.setText("🌱 Good soil moisture");
        } else if (condition.equalsIgnoreCase("Clear")) {
            tvAdvice1.setText("✅ Suitable for spraying");
            tvAdvice2.setText("🚜 Good for harvesting");
        } else {
            tvAdvice1.setText("⚠ Monitor weather");
            tvAdvice2.setText("🌾 Plan field work carefully");
        }
    }


    // ================= PERMISSION RESULT =================
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == 101 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocationWeather();
        }
    }
}
