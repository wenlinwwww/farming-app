package com.example.aq_bluering.ui.Dashboard.Detail;

import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.aq_bluering.R;
import com.example.aq_bluering.databinding.WeatherFragmentBinding;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WeatherFragment extends DialogFragment {

    private WeatherFragmentBinding binding;
    private DetailViewModel viewModel;
    private String currentFieldID;
    private String currentSensorID;


    public WeatherFragment (String fieldID, String sensorID, DetailViewModel viewModel){
        this.currentFieldID = fieldID;
        this.currentSensorID=sensorID;
        this.viewModel=viewModel;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = WeatherFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TextView card1_tempView1 = root.findViewById(R.id.card1_tempView1);
        TextView card1_tempView2 = root.findViewById(R.id.card1_tempView2);
        TextView card1_tempView3 = root.findViewById(R.id.card1_tempView3);
        TextView card2_tempView1 = root.findViewById(R.id.card2_tempView1);
        TextView card2_tempView2 = root.findViewById(R.id.card2_tempView2);
        TextView card2_tempView3 = root.findViewById(R.id.card2_tempView3);
        TextView card3_tempView1 = root.findViewById(R.id.card3_tempView1);
        TextView card3_tempView2 = root.findViewById(R.id.card3_tempView2);
        TextView card3_tempView3 = root.findViewById(R.id.card3_tempView3);
        TextView card4_tempView1 = root.findViewById(R.id.card4_tempView1);
        TextView card4_tempView2 = root.findViewById(R.id.card4_tempView2);
        TextView card4_tempView3 = root.findViewById(R.id.card4_tempView3);
        TextView card5_tempView1 = root.findViewById(R.id.card5_tempView1);
        TextView card5_tempView2 = root.findViewById(R.id.card5_tempView2);
        TextView card5_tempView3 = root.findViewById(R.id.card5_tempView3);
        ImageView imageView1 = root.findViewById(R.id.weather_icon1);
        ImageView imageView2 = root.findViewById(R.id.weather_icon2);
        ImageView imageView3 = root.findViewById(R.id.weather_icon3);
        ImageView imageView4 = root.findViewById(R.id.weather_icon4);
        ImageView imageView5 = root.findViewById(R.id.weather_icon5);
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        // Use 'binding' to access your views

        viewModel.getFiveDayData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                // renew ui
//                card1_tempView1.setText(s);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(s, JsonObject.class);
                JsonArray listArray = jsonObject.getAsJsonArray("list");
                List<String[]> weatherList = new ArrayList<>();
                for (int i = 0; i < listArray.size(); i++) {
                    JsonObject listItem = listArray.get(i).getAsJsonObject();
                    // get dtText value
                    String dtTxt = listItem.get("dt_txt").getAsString();
                    // get every day 12:00 weather data
                    if (dtTxt.endsWith("21:00:00")) {
                        JsonObject mainObject = listItem.getAsJsonObject("main");
                        JsonObject weatherObject = listItem.getAsJsonArray("weather").get(0).getAsJsonObject();
                        JsonObject windObject = listItem.getAsJsonObject("wind");

                        int temp = (int) mainObject.get("temp").getAsDouble();
                        int humidity = mainObject.get("humidity").getAsInt();
                        String weatherDescription = weatherObject.get("description").getAsString();
                        double windSpeed = windObject.get("speed").getAsDouble();
                        String datePart = dtTxt.split(" ")[0];
                        String icon = weatherObject.get("icon").getAsString();
                        String[] weatherData = new String[6];
                        weatherData[0] = String.valueOf(temp);
                        weatherData[1] = String.valueOf(humidity);
                        weatherData[2] = String.valueOf(windSpeed);
                        weatherData[3] = datePart;
                        weatherData[4] = weatherDescription;
                        weatherData[5] = "https://openweathermap.org/img/wn/" + icon + ".png";
                        weatherList.add(weatherData);
                    }
                }

                // day 1 icon
                Glide.with(requireContext())
                        .load(weatherList.get(0)[5])
                        .placeholder(R.drawable.weather_icon)
                        .error(R.drawable.weather_icon) // when fail to load image
                        .into(imageView1);
                // day 2 icon
                Glide.with(requireContext())
                        .load(weatherList.get(1)[5])
                        .placeholder(R.drawable.weather_icon)
                        .error(R.drawable.weather_icon) // when fail to load image
                        .into(imageView2);

                // day 3 icon
                Glide.with(requireContext())
                        .load(weatherList.get(2)[5])
                        .placeholder(R.drawable.weather_icon)
                        .error(R.drawable.weather_icon) // when fail to load image
                        .into(imageView3);

                // day 4 icon
                Glide.with(requireContext())
                        .load(weatherList.get(3)[5])
                        .placeholder(R.drawable.weather_icon)
                        .error(R.drawable.weather_icon) // when fail to load image
                        .into(imageView4);

                // day 5 icon
                Glide.with(requireContext())
                        .load(weatherList.get(4)[5])
                        .placeholder(R.drawable.weather_icon)
                        .error(R.drawable.weather_icon) // when fail to load image
                        .into(imageView5);

                // day 1 detail
                card1_tempView1.setText(weatherList.get(0)[0]+"°C");
                card1_tempView2.setText("Humidity: " + weatherList.get(0)[1]+"%\n\n"
                        + " Wind: " + weatherList.get(0)[2] + "m/s");
                card1_tempView3.setText(weatherList.get(0)[3]
                        + "\n\n" + weatherList.get(0)[4]);
                // day 2 detail
                card2_tempView1.setText(weatherList.get(1)[0]+"°C");
                card2_tempView2.setText("Humidity: " + weatherList.get(1)[1]+"%\n\n"
                        + " Wind: " + weatherList.get(1)[2] + "m/s");
                card2_tempView3.setText(weatherList.get(1)[3]
                        + "\n\n" + weatherList.get(1)[4]);
                // day 3 detail
                card3_tempView1.setText(weatherList.get(2)[0]+"°C");
                card3_tempView2.setText("Humidity: " + weatherList.get(2)[1]+"%\n\n"
                        + " Wind: " + weatherList.get(2)[2] + "m/s");
                card3_tempView3.setText(weatherList.get(2)[3]
                        + "\n\n" + weatherList.get(2)[4]);
                // day 4 detail
                card4_tempView1.setText(weatherList.get(3)[0]+"°C");
                card4_tempView2.setText("Humidity: " + weatherList.get(3)[1]+"%\n\n"
                        + " Wind: " + weatherList.get(3)[2] + "m/s");
                card4_tempView3.setText(weatherList.get(3)[3]
                        + "\n\n" + weatherList.get(3)[4]);
                // day 5 detail
                card5_tempView1.setText(weatherList.get(4)[0]+"°C");
                card5_tempView2.setText("Humidity: " + weatherList.get(4)[1]+"%\n\n"
                        + " Wind: " + weatherList.get(4)[2] + "m/s");
                card5_tempView3.setText(weatherList.get(4)[3]
                        + "\n\n" + weatherList.get(4)[4]);
            }
        });

        viewModel.fetchFiveDayWeather(currentFieldID, currentSensorID);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}
