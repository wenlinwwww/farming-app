package com.example.aq_bluering.ui.Dashboard.Detail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.R;
import com.example.aq_bluering.UsernameShare;
import com.example.aq_bluering.databinding.FragmentDashboardDetailBinding;
import com.example.aq_bluering.Connection.JsonParser;
import com.example.aq_bluering.Connection.WeatherApi;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class Detail extends Fragment {

    private FragmentDashboardDetailBinding binding;
    private DetailViewModel viewModel;
    private ArrayList<String> farmList = new ArrayList<>();
    private String currentFarmName = null;
    private String currentFieldID = null;
    private String currentSensorID = null;
    private String currentFieldName = null;
    private boolean fieldReady = false;

    private UsernameShare usernameShare;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        try {
            usernameShare = new ViewModelProvider(requireActivity()).get(UsernameShare.class);
            String userName = usernameShare.getUsername().getValue();
            viewModel.setUsername(userName);
        } catch (Exception e) {
            Log.e("error in obtain username", e.getMessage());
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentDashboardDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TextView timeView = root.findViewById(R.id.timeShow);
        TextView moist50View = root.findViewById(R.id.mois50Show);
        TextView moist100View = root.findViewById(R.id.mois100Show);
        TextView moist150View = root.findViewById(R.id.mois150Show);
        TextView tempView = root.findViewById(R.id.tempShow);
        TextView batteryView = root.findViewById(R.id.battaryShow);
        TextView sensorIdView = root.findViewById(R.id.sensorShow);
        TextView aliasView = root.findViewById(R.id.aliasShow);
        TextView dailyTempView = root.findViewById(R.id.textDailyTemp);
        TextView highLowTempView = root.findViewById(R.id.textHighLowTemp);
        TextView locationView = root.findViewById(R.id.location);
        View no_data_view = root.findViewById(R.id.no_data);
        TextView no_data_msg = root.findViewById(R.id.no_data_msg);
        no_data_msg.setText("Loading........");
        no_data_msg.setVisibility(View.VISIBLE);
        no_data_view.setVisibility(View.VISIBLE);

        JsonParser jsonParser = new JsonParser();

        viewModel.getSensorDetailResponse().observe(getViewLifecycleOwner(), response -> {
            if (jsonParser.inValidJson(response)) {
                no_data_view.setVisibility(View.VISIBLE);
                no_data_msg.setText("No data for this sensor");
                no_data_msg.setVisibility(View.VISIBLE);
            } else {

                no_data_msg.setVisibility(View.GONE);
                no_data_view.setVisibility(View.GONE);

                // renew the ui
                timeView.setText(setTime(jsonParser.getValue(response, "time")));
                moist50View.setText(jsonParser.getValue(response, "cap50") + "%");
                moist100View.setText(jsonParser.getValue(response, "cap100") + "%");
                moist150View.setText(jsonParser.getValue(response, "cap150") + "%");
                tempView.setText(jsonParser.getValue(response, "temperature") + "°C");
                batteryView.setText(jsonParser.getValue(response, "battery_vol") + "V");
                sensorIdView.setText(jsonParser.getValue(response, "sensor_id"));
            }

        });

        viewModel.getAliasData().observe(getViewLifecycleOwner(), alias -> {
            // renew ui
            aliasView.setText(jsonParser.getMultipleValue(alias, "sensor_id", currentSensorID, "alias"));
        });

        viewModel.getWeatherData().observe(getViewLifecycleOwner(), weatherData -> {
            WeatherApi weatherApi = new WeatherApi();
            if (weatherData != null) {
                String lowTemp = weatherApi.getWeatherApiMainValue(weatherData, "temp_min");
                String highTemp = weatherApi.getWeatherApiMainValue(weatherData, "temp_max");
                String dailyTemp = weatherApi.getWeatherApiMainValue(weatherData, "temp");
                highLowTempView.setText(highTemp + " ° / " + lowTemp + " °");
                dailyTempView.setText(dailyTemp + " °");
            }
        });

        viewModel.getLocationData().observe(getViewLifecycleOwner(), weatherData -> {
            WeatherApi weatherApi = new WeatherApi();
            if (weatherData != null) {
                String timezone = weatherApi.getWeatherTimeZone(weatherData, "timezone");
                String[] parts = timezone.split("/");
                locationView.setText(parts[0] + "\n" + parts[1]);
            }
        });


        //portion to set up menu bar on top for selecting farm, field, and sensor
        Spinner farmSpinner = binding.spinnerFarm;
        Spinner fieldSpinner = binding.spinnerField;
        Spinner sensorSpinner = binding.spinnerSensor;

        //set up listener for receiving data response from api
        //initial farm list
        viewModel.getFarmResponse().observe(getViewLifecycleOwner(), s -> {
            farmList.clear();
            farmList = jsonParser.getValueList(s, "farm_name");
            //default value for current selection of farm is the first farm (if any farm exists)
            if (farmList.isEmpty()) {
                farmList.add("No available farm");
            }
            setFarmList(farmSpinner);
        });

        //initial field list for a given farm name
        viewModel.getFieldResponse().observe(getViewLifecycleOwner(), s -> {
            fieldReady = true;
            ArrayList<String> fieldList = jsonParser.getValueListByName(s, "farm_name",
                    currentFarmName, "field_name");
            if (fieldList.isEmpty()) {
                fieldList.add("No Available Field");
            }
            setFieldList(fieldSpinner, fieldList);
        });

        //update sensor list for a given filed name
        viewModel.getSensorResponse().observe(getViewLifecycleOwner(), s -> {
            ArrayList<String> sensorList = jsonParser.getValueList(s, "sensor_id");
            if (sensorList.isEmpty()) {
                sensorList.add("No Available Sensor");
            }
            setSensorList(sensorSpinner, sensorList);
        });

        //set up menu selection listener
        // set when a farm name selection changed, update field list
        farmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentFarmName = farmSpinner.getSelectedItem().toString();
                ArrayList<String> fields = new ArrayList<>();
                if (fieldReady) {
                    fields = jsonParser.getValueListByName(viewModel.getFieldResponse().getValue(), "farm_name",
                            currentFarmName, "field_name");
                }
                if (fields.isEmpty()) {
                    fields.add("No Available Field");
                }
                setFieldList(fieldSpinner, fields);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        //when the current field selection changed, get a new sensor list
        fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentFieldName = fieldSpinner.getSelectedItem().toString();
                if (!currentFieldName.equals("No Available Field")) {
                    String fieldID = jsonParser.getMultipleValue(viewModel.getFieldResponse().getValue(), "field_name",
                            currentFieldName, "field_id");
                    currentFieldID = fieldID;
                    viewModel.fetchSensorList(fieldID);
                } else {
                    ArrayList<String> empty = new ArrayList<>();
                    empty.add("No Available Sensor");
                    setSensorList(sensorSpinner,empty);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        //update currentSensorName
        sensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSensorID = sensorSpinner.getSelectedItem().toString();
                viewModel.fetchSensorDetails(currentSensorID, currentFieldName);
                viewModel.fetchAliasFromApi(currentFieldID);
                viewModel.fetchWeatherData(currentFieldID, currentSensorID);
                viewModel.fetchLocationData(currentFieldID, currentSensorID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        //set open a new fragment when click on weather section
        binding.weatherContainer.setOnClickListener(view -> openWeather());

        //get farm & field data from API
        viewModel.fetchFarmList();
        viewModel.fetchFieldList();
        //end here

        return root;
    }


    //for menu
    private void setFarmList(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_option_white);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(farmList);
    }

    private void setFieldList(Spinner spinner, ArrayList<String> fieldList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_option_white);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(fieldList);
    }

    private void setSensorList(Spinner spinner, ArrayList<String> sensorList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_option_white);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(sensorList);
    }
    //end here


    private String setTime(String timeString) {
        String dateResult = timeString;
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = inputFormat.parse(timeString);
            dateResult = outputFormat.format(date);
        } catch (Exception e) {
            Log.e("Invalid date format from database", e.getMessage());
        }
        return dateResult;
    }

    private void openWeather() {
        WeatherFragment weatherFragment = new WeatherFragment(currentFieldID,currentSensorID,viewModel);
        weatherFragment.show(getChildFragmentManager(), "WeatherDialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}