package com.adiBlum.adiblum.myapplication;

import android.app.Activity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StatisticsActivity extends Activity {

    private Map<String, Double> datesToHours = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);
        Serializable serializableExtra = getIntent().getSerializableExtra("MyClass");
        datesToHours = (Map<String, Double>) serializableExtra;
        showGragh();
    }

    public void showGragh() {
        GraphView graph = (GraphView) findViewById(R.id.graph);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
//                new DataPoint(1, 5),
//                new DataPoint(2, 3),
//                new DataPoint(3, 2),
//                new DataPoint(4, 6)
        });

        graph.addSeries(series);
    }

    public void setDatesToHours(Map<String, Double> datesToHours) {
        this.datesToHours = datesToHours;
    }
}
