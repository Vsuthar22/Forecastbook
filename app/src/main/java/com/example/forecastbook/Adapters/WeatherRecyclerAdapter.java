package com.example.forecastbook.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forecastbook.Formatting;
import com.example.forecastbook.Models.LongTerm;
import com.example.forecastbook.R;
import com.example.forecastbook.UnitConvertor;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WeatherRecyclerAdapter extends RecyclerView.Adapter<WeatherViewHolder> {

    private List<LongTerm> itemList;
    private Context context;
    Formatting formatting = new Formatting(context);


    public WeatherRecyclerAdapter(Context context, List<LongTerm> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);
        WeatherViewHolder viewHolder = new WeatherViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int i) {

        LongTerm longtermitem = itemList.get(i);

        String desc = longtermitem.getDescription();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm");

        holder.itemDescription.setText("Description: " + desc.substring(0, 1).toUpperCase() + desc.substring(1, desc.length()));
        holder.itemHumidity.setText("Humidity: " + longtermitem.getHumidity() + "%");
        holder.itemPressure.setText("Pressure: " + longtermitem.getPressure() + " hPa/mBar");
        holder.itemyWind.setText("Wind:" + longtermitem.getWind() + " m/s");
        Double tempK = Double.valueOf((longtermitem.getTemperature()));
        holder.itemTemperature.setText(String.format("%.1f", UnitConvertor.KelvintoCelcius(tempK)) + " °C");
        holder.itemDate.setText(longtermitem.getDate());

        Typeface weatherFont = android.graphics.Typeface.createFromAsset(context.getAssets(), "fonts/weather.ttf");
        holder.itemIcon.setTypeface(weatherFont);

        holder.itemIcon.setText(longtermitem.getIcon());




    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
