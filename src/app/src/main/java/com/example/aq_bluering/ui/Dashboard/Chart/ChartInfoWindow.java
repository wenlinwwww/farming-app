package com.example.aq_bluering.ui.Dashboard.Chart;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.aq_bluering.R;
import com.example.aq_bluering.databinding.ChartDetailWindowBinding;
import com.example.aq_bluering.databinding.FragmentDashboardChartBinding;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;

public class ChartInfoWindow extends MarkerView {

    private String chartName = null;
    private ArrayList<String> dateTimeList;
    private TextView header;
    private TextView info1;
    private TextView info2;
    private TextView info3;
    private TextView info4;
    private LineData lineData;
    private ChartDetailWindowBinding binding;


    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public ChartInfoWindow(Context context, int layoutResource, String chartName, ArrayList<String> dateTimeList, LineData lineData) {
        super(context, layoutResource);
        this.dateTimeList = dateTimeList;
        this.header = findViewById(R.id.window_header);
        this.info1 = findViewById(R.id.window_info1);
        this.info2 = findViewById(R.id.window_info2);
        this.info3 = findViewById(R.id.window_info3);
        this.info4 = findViewById(R.id.window_info4);
        this.chartName = chartName;
        this.lineData = lineData;


    }


    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        if (lineData != null && lineData.getDataSetCount() > 0) {
            if (chartName.equals("battery")) {
                float x_coordinate = e.getX();
                String headerString = dateTimeList.get((int) x_coordinate);
                header.setText(headerString);
                header.setVisibility(View.VISIBLE);
                ILineDataSet dataSet = lineData.getDataSetByLabel("Battery_Voltage",
                        true);
                if (dataSet != null && dataSet.getEntryCount() > 0) {
                    Entry entry = dataSet.getEntryForXValue(x_coordinate, Float.NaN);
                    String value = String.valueOf(entry.getY());
                    String infoString = "Voltage: " + value+" V";
                    info1.setText(infoString);
                    info1.setVisibility(View.VISIBLE);
                }

            } else if (chartName.equals("moisture")) {

                float x_coordinate = e.getX();
                String headerString = dateTimeList.get((int) x_coordinate);
                header.setText(headerString);
                header.setVisibility(View.VISIBLE);
                ILineDataSet dataSet_temperature = lineData.getDataSetByLabel("Temperature",
                        true);
                if (dataSet_temperature != null && dataSet_temperature.getEntryCount() > 0) {
                    Entry entry = dataSet_temperature.getEntryForXValue(x_coordinate, Float.NaN);
                    String value = String.valueOf(entry.getY());
                    String infoString = "Temperature: " + value+"%";
                    info1.setText(infoString);
                    info1.setVisibility(View.VISIBLE);
                }

                ILineDataSet dataSet_cap50 = lineData.getDataSetByLabel("Cap_50",
                        true);
                if (dataSet_cap50 != null && dataSet_cap50.getEntryCount() > 0) {
                    Entry entry = dataSet_cap50.getEntryForXValue(x_coordinate, Float.NaN);
                    String value = String.valueOf(entry.getY());
                    String infoString = "Moisture 30-50cm underground: " + value+"%";
                    info2.setText(infoString);
                    info2.setVisibility(View.VISIBLE);
                }

                ILineDataSet dataSet_cap100 = lineData.getDataSetByLabel("Cap_100",
                        true);
                if (dataSet_cap100 != null && dataSet_cap100.getEntryCount() > 0) {
                    Entry entry = dataSet_cap100.getEntryForXValue(x_coordinate, Float.NaN);
                    String value = String.valueOf(entry.getY());
                    String infoString = "Moisture 100cm underground: " + value+"%";
                    info3.setText(infoString);
                    info3.setVisibility(View.VISIBLE);
                }

                ILineDataSet dataSet_cap150 = lineData.getDataSetByLabel("Cap_150",
                        true);
                if (dataSet_cap150 != null && dataSet_cap150.getEntryCount() > 0) {
                    Entry entry = dataSet_cap150.getEntryForXValue(x_coordinate, Float.NaN);
                    String value = String.valueOf(entry.getY());
                    String infoString = "Moisture 150cm underground: " + value+"%";
                    info4.setText(infoString);
                    info4.setVisibility(View.VISIBLE);
                }

            } else if (chartName.equals("evaporation")){
                float x_coordinate = e.getX();
                String headerString = dateTimeList.get((int) x_coordinate);
                header.setText(headerString);
                header.setVisibility(View.VISIBLE);
                ILineDataSet dataSet = lineData.getDataSetByLabel("Evaporation",
                        true);
                if (dataSet != null && dataSet.getEntryCount() > 0) {
                    Entry entry = dataSet.getEntryForXValue(x_coordinate, Float.NaN);
                    String value = String.valueOf(entry.getY());
                    String infoString = "Evaporation: " + value +" mm";
                    info1.setText(infoString);
                    info1.setVisibility(View.VISIBLE);
                }
            }
        }
        // This will force the marker-view to update its size
        super.refreshContent(e, highlight);

    }

    @Override
    public MPPointF getOffset() {
        // Adjust the marker offset if needed
        return new MPPointF(-getWidth() / 2f, -getHeight());
    }
}
