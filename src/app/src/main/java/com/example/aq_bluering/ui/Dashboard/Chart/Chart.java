package com.example.aq_bluering.ui.Dashboard.Chart;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.aq_bluering.R;
import com.example.aq_bluering.UsernameShare;
import com.example.aq_bluering.databinding.FragmentDashboardChartBinding;
import com.example.aq_bluering.Connection.JsonParser;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kotlinx.serialization.json.Json;

public class Chart extends Fragment {

    private FragmentDashboardChartBinding binding;
    private ChartViewModel viewModel;

    private LineChart batteryChart;
    private LineChart evapChart;
    private BarChart precipChart;
    private LineChart mosChart;

    //for menu
    private ArrayList<String> farmList = new ArrayList<>();
    private String currentFarmName = null;
    private String currentFieldID = null;
    private String currentSensorID = null;
    private String currentFieldName = null;
    private boolean fieldReady = false;
    private boolean sensorReady = false;

    private UsernameShare usernameShare;

    //end


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChartViewModel.class);
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


        binding = FragmentDashboardChartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        JsonParser jsonParser = new JsonParser();

        evapChart = binding.lineChart;
        batteryChart = binding.batteryChart;
        mosChart = binding.MosChart;
        precipChart = binding.precipChart;
        setupLineChart(evapChart, "Evaporation");
        setupLineChart(batteryChart, "Battery Voltage");
        setupBarChart(precipChart, "Weather Data");
        setupLineChart(mosChart, "Average Soil Moisture/Temperature Variation");


        // display evaporation chart
        viewModel.getResponseData().observe(getViewLifecycleOwner(), response -> {
            //list of date display in axis
            ArrayList<String> dateList = new ArrayList<>();
            ArrayList<String> dateTimeList = new ArrayList<>();
            List<Entry> entries = new ArrayList<>();
            List<String> evapData = jsonParser.getValues(response, "evaporation");
            List<String> time = jsonParser.getValues(response, "date");
            for (int i = 0; i < time.size(); i++) {
                try {
                    //format date display on chart axis
                    SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy");
                    Date date = inputFormat.parse(time.get(i));
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd");
                    //date formatted for x axis
                    String displayedDate = displayFormat.format(date);
                    dateList.add(displayedDate);
                    //date formatted for display in info window
                    String dateTime = outputFormat.format(date);
                    dateTimeList.add(dateTime);
                    //end here
                    float evaporationValue = Float.parseFloat(evapData.get(i));
                    entries.add(new Entry(i, evaporationValue));
                } catch (Exception e) {
                    Log.e("parse date format error", e.getMessage());
                }

            }
            LineDataSet dataSet1 = new LineDataSet(entries, "Evaporation");
            dataSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            LineData lineData1 = new LineData(dataSet1);
            evapChart.setData(lineData1);

            //set chart axis
            XAxis xAxis = evapChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            YAxis yAxisLeft = evapChart.getAxisLeft();

            xAxis.setGranularity(2f);
            yAxisLeft.setGranularity(2f);

            xAxis.setValueFormatter(new IndexAxisValueFormatter(dateList));
            evapChart.getAxisRight().setEnabled(false);
            yAxisLeft.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    DecimalFormat decimalFormat = new DecimalFormat("0.0");
                    String formattedValue = decimalFormat.format(value);
                    return formattedValue + "mm";
                }
            });
            dataSet1.setLineWidth(3f);
            dataSet1.setValueTextSize(8f);
            Legend legend = evapChart.getLegend();
            legend.setEnabled(true);
            evapChart.invalidate();

            ChartInfoWindow window = new ChartInfoWindow(this.getContext(), R.layout.chart_detail_window,
                    "evaporation", dateTimeList, evapChart.getLineData());

            evapChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    window.setChartView(evapChart);
                    evapChart.setMarker(window);
                }

                @Override
                public void onNothingSelected() {
                    evapChart.setMarker(null);
                }
            });
            if (entries.isEmpty()) {
                binding.loadingEvaporationText.setText("No evaporation data available currently");
            } else {
                binding.loadingEvaporation.setVisibility(View.GONE);
            }

        });


        viewModel.getSensorDetailsResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                //display battery chart
                displayBatteryChart(s, jsonParser);

                //display mos chart with different caps
                displayMoistureChart(s, jsonParser);
            }
        });

        //display precipitation chart
        viewModel.getWeatherResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                //list of date display in axis
                ArrayList<String> dateList = new ArrayList<>();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray daysArray = jsonObject.getJSONArray("days");
                    List<BarEntry> entries = new ArrayList<>();
                    for (int i = 0; i < daysArray.length(); i++) {
                        JSONObject dayObject = daysArray.getJSONObject(i);
                        String datetimeStr = dayObject.getString("datetime");
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = format.parse(datetimeStr);
                        SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd");
                        String displayedDate = displayFormat.format(date);
                        dateList.add(displayedDate);
                        //end here

                        float precip = (float) dayObject.getDouble("precip");
                        entries.add(new BarEntry(i, precip));
                        // }
                    }

                    BarDataSet dataSet = new BarDataSet(entries, "Precipitation");
                    BarData barData = new BarData(dataSet);
                    precipChart.setData(barData);

                    //set chart axis
                    XAxis xAxis = precipChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(dateList));
                    xAxis.setAxisMinimum(-0.5f);
                    YAxis yAxisLeft = precipChart.getAxisLeft();
                    yAxisLeft.setGranularity(0.5f);
                    yAxisLeft.setAxisMinimum(0f);
                    precipChart.getAxisRight().setEnabled(false);
                    yAxisLeft.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            DecimalFormat decimalFormat = new DecimalFormat("0.0");
                            String formattedValue = decimalFormat.format(value);
                            return formattedValue + "mm";
                        }
                    });
                    dataSet.setValueTextSize(8f);
                    Legend legend = precipChart.getLegend();
                    legend.setEnabled(true);

                    precipChart.setExtraOffsets(0f, 0f, 0f, 0f);

                    precipChart.invalidate();
                    if (entries.isEmpty()) {
                        binding.loadingPrecipitationText.setText("No precipitation data available currently");
                    } else {
                        binding.loadingPrecipitation.setVisibility(View.GONE);
                    }

                } catch (JSONException | ParseException e) {
                    throw new RuntimeException(e);
                }
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
            } else {
                sensorReady = true;
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
                if (!currentFieldName.equals("No Available Field") && currentFieldName != null) {
                    String fieldID = jsonParser.getMultipleValue(viewModel.getFieldResponse().getValue(), "field_name",
                            currentFieldName, "field_id");
                    currentFieldID = fieldID;
                    viewModel.fetchSensorList(fieldID);
                    String fieldPolygon = jsonParser.getMultipleValue(viewModel.getFieldResponse().getValue(), "field_id", fieldID, "points");
                    ArrayList<LatLng> polygonPoints = jsonParser.getCoordinates(fieldPolygon);
                    viewModel.fetchWeatherData(polygonPoints);
                } else {
                    ArrayList<String> empty = new ArrayList<>();
                    empty.add("No Available Sensor");
                    setSensorList(sensorSpinner, empty);
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
                String currentSelection = sensorSpinner.getSelectedItem().toString();
                if (!currentSelection.equals("No Available Sensor") && sensorReady) {
                    currentSensorID = currentSelection;
                    viewModel.fetchEvaFromApi(currentFieldID);
                    //need to be var
                    // viewModel.fetchMosFromApi(currentSensorID, currentFieldName);
                    viewModel.fetchSensorDetails(currentFieldName, 2);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });


        //get farm & field data from API
        viewModel.fetchFarmList();
        viewModel.fetchFieldList();
        //end here

        return root;
    }

    private void displayBatteryChart(String s, JsonParser jsonParser) {
        //list of date display in axis
        ArrayList<String> dateList = new ArrayList<>();
        ArrayList<String> dateTimeList = new ArrayList<>();
        //batter value list of a sensor in the past 7 days
        ArrayList<String> batterValueList = jsonParser.extractLastTenData(s, "sensor_id",
                currentSensorID, "battery_vol");
        ArrayList<String> time = jsonParser.extractLastTenData(s, "sensor_id", currentSensorID, "time");

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < time.size(); i++) {
            try {
                //format date display on chart axis
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                Date date = inputFormat.parse(time.get(i));
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd");
                //date formatted for x axis
                String displayedDate = displayFormat.format(date);
                dateList.add(displayedDate);
                //date formatted for display in info window
                String dateTime = outputFormat.format(date);
                dateTimeList.add(dateTime);
                //end here
                entries.add(new Entry(i, Float.parseFloat(batterValueList.get(i))));

            } catch (Exception e) {
                Log.e("parse date format error", e.getMessage());
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Battery_Voltage");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData = new LineData(dataSet);
        batteryChart.setData(lineData);

        //set chart axis
        XAxis xAxis = batteryChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateList));
        YAxis yAxisLeft = batteryChart.getAxisLeft();
        yAxisLeft.setGranularity(1f);
        batteryChart.getAxisRight().setEnabled(false);

        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                String formattedValue = decimalFormat.format(value);
                return formattedValue + "V";
            }
        });
        dataSet.setLineWidth(3f);
        dataSet.setValueTextSize(8f);
        Legend legend = batteryChart.getLegend();
        legend.setEnabled(true);
        batteryChart.invalidate();


        ChartInfoWindow window = new ChartInfoWindow(this.getContext(), R.layout.chart_detail_window,
                "battery", dateTimeList, batteryChart.getLineData());
        batteryChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                window.setChartView(batteryChart);
                batteryChart.setMarker(window);

            }

            @Override
            public void onNothingSelected() {
                batteryChart.setMarker(null);
            }
        });

        if (entries.isEmpty()) {
            binding.loadingBatteryText.setText("No battery data available currently");
        } else {
            binding.loadingBattery.setVisibility(View.GONE);
        }

    }

    private void displayMoistureChart(String s, JsonParser jsonParser) {
        ArrayList<String> dateTimeList = new ArrayList<>();
        ArrayList<String> dateList = new ArrayList<>();
        //batter value list of a sensor in the past 7 days
        ArrayList<String> cap50List = jsonParser.extractLastTenData(s, "sensor_id",
                currentSensorID, "cap50");
        ArrayList<String> cap100List = jsonParser.extractLastTenData(s, "sensor_id",
                currentSensorID, "cap100");
        ArrayList<String> cap150List = jsonParser.extractLastTenData(s, "sensor_id",
                currentSensorID, "cap150");
        ArrayList<String> temperatureList = jsonParser.extractLastTenData(s, "sensor_id",
                currentSensorID, "temperature");
        ArrayList<String> time = jsonParser.extractLastTenData(s, "sensor_id", currentSensorID, "time");

        List<Entry> entriesCap50 = new ArrayList<>();
        List<Entry> entriesCap100 = new ArrayList<>();
        List<Entry> entriesCap150 = new ArrayList<>();
        List<Entry> entriesTemperature = new ArrayList<>();
        for (int i = 0; i < time.size(); i++) {
            try {
                //format date display on chart axis
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                Date date = inputFormat.parse(time.get(i));
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd");
                //date formatted for x axis
                String displayedDate = displayFormat.format(date);
                dateList.add(displayedDate);
                //date formatted for display in info window
                String dateTime = outputFormat.format(date);
                dateTimeList.add(dateTime);

                entriesCap50.add(new Entry(i, Float.parseFloat(cap50List.get(i))));
                entriesCap100.add(new Entry(i, Float.parseFloat(cap100List.get(i))));
                entriesCap150.add(new Entry(i, Float.parseFloat(cap150List.get(i))));
                entriesTemperature.add(new Entry(i, Float.parseFloat(temperatureList.get(i))));

            } catch (Exception e) {
                Log.e("parse date format error", e.getMessage());
            }
        }

        LineDataSet dataSetCap50 = new LineDataSet(entriesCap50, "Cap_50");
        LineDataSet dataSetCap100 = new LineDataSet(entriesCap100, "Cap_100");
        LineDataSet dataSetCap150 = new LineDataSet(entriesCap150, "Cap_150");
        LineDataSet dataSetTemperature = new LineDataSet(entriesTemperature, "Temperature");


        dataSetCap50.setLineWidth(2f);
        dataSetCap100.setLineWidth(2f);
        dataSetCap150.setLineWidth(2f);
        dataSetTemperature.setLineWidth(2f);

        dataSetCap50.setValueTextSize(8f);
        dataSetCap100.setValueTextSize(8f);
        dataSetCap150.setValueTextSize(8f);
        dataSetTemperature.setValueTextSize(8f);

        dataSetCap50.setColor(Color.RED);
        dataSetCap100.setColor(Color.BLUE);
        dataSetCap150.setColor(Color.GREEN);
        dataSetTemperature.setColor(Color.GRAY);

        LineData lineData = new LineData(dataSetCap50, dataSetCap100, dataSetCap150, dataSetTemperature);
        mosChart.setData(lineData);

        XAxis xAxis = mosChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateList));

        YAxis leftAxis = mosChart.getAxisLeft();
        leftAxis.setValueFormatter(new PercentFormatter());

        YAxis rightAxis = mosChart.getAxisRight();
        rightAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                String formattedValue = decimalFormat.format(value);
                return formattedValue + "℃";
            }
        });


        Legend legend = mosChart.getLegend();
        legend.setEnabled(true);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        mosChart.invalidate();


        ChartInfoWindow window = new ChartInfoWindow(this.getContext(), R.layout.chart_detail_window,
                "moisture", dateTimeList, mosChart.getLineData());
        mosChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                window.setChartView(mosChart);
                mosChart.setMarker(window);
            }

            @Override
            public void onNothingSelected() {
                mosChart.setMarker(null);
            }
        });

        if (entriesCap50.isEmpty() && entriesCap100.isEmpty() && entriesCap150.isEmpty() && entriesTemperature.isEmpty()) {
            binding.loadingMoistureText.setText("No moisture and temperature data available currently");
        } else {
            binding.loadingMoisture.setVisibility(View.GONE);
        }

    }

    private void setupLineChart(LineChart chart, String chartName) {
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.getDescription().setEnabled(false);

    }

    private void setupBarChart(BarChart chart, String chartName) {
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.getDescription().setEnabled(false);
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}