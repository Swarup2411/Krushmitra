package com.mountrich.krushimitra.adapters;




import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mountrich.krushimitra.R;
import com.mountrich.krushimitra.models.DailyForecast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FiveDayForecastAdapter
        extends RecyclerView.Adapter<FiveDayForecastAdapter.ForecastViewHolder> {

    private Context context;
    private List<DailyForecast> forecastList;

    public FiveDayForecastAdapter(Context context, List<DailyForecast> forecastList) {
        this.context = context;
        this.forecastList = forecastList;
    }


    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.forecast_item_ui, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {

        DailyForecast item = forecastList.get(position);

        holder.tvDay.setText(item.day);
        holder.tvCondition.setText(item.condition);
        holder.tvTempRange.setText(
                item.minTemp + "° / " + item.maxTemp + "°"
        );

        // 🌦 Set icon based on weather
        holder.imgForecastIcon.setImageResource(
                getWeatherIcon(item.icon)
        );


        holder.tvDate.setText(
                formatDate(item.getDt(), item.getTimezone())
        );


    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    // 🧠 Weather icon mapping (Farmer friendly)
    private int getWeatherIcon(String iconCode) {

        if (iconCode == null) return R.drawable.weather_icon;

        switch (iconCode) {
            case "01d":
            case "01n":
                return R.drawable.weather_icon;

            case "02d":
            case "02n":
            case "03d":
            case "03n":
            case "04d":
            case "04n":
                return R.drawable.weather_icon;

            case "09d":
            case "09n":
            case "10d":
            case "10n":
                return R.drawable.weather_icon;

            case "11d":
            case "11n":
                return R.drawable.weather_icon;

            default:
                return R.drawable.weather_icon;
        }
    }

    // 🔹 ViewHolder
    static class ForecastViewHolder extends RecyclerView.ViewHolder {

        TextView tvDay, tvCondition, tvTempRange,tvDate;
        ImageView imgForecastIcon;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            tvTempRange = itemView.findViewById(R.id.tvTempRange);
            imgForecastIcon = itemView.findViewById(R.id.imgForecastIcon);
        }
    }

    private String formatDate(long dt, int timezoneOffset) {

        long correctTimeMillis = (dt + timezoneOffset) * 1000;

        Date date = new Date(correctTimeMillis);

        SimpleDateFormat sdf =
                new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return sdf.format(date);
    }


}
