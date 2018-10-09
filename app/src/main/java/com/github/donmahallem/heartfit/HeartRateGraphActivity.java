package com.github.donmahallem.heartfit;

import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

public class HeartRateGraphActivity extends AppCompatActivity {

    private LineChart mLineChart;
    private LineData mLineData;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(R.layout.activity_heartrate_graph);
        this.mLineChart = this.findViewById(R.id.linechart);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.loadData();
    }

    private void loadData() {
        if (this.mLineData == null) {
            this.mLineData = new LineData();
            this.mLineChart.setData(this.mLineData);
        }
        List<Entry> entries = new ArrayList<Entry>();

        for (int i = 0; i < 100; i++) {

            // turn your data into Entry objects
            entries.add(new Entry((float) Math.random() * 100f, (float) Math.random()));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        dataSet.setColor(0xFF0000);
        this.mLineData.addDataSet(dataSet);

        this.mLineChart.invalidate();
        this.loadFitHeartRateData();
    }

    public void loadFitHeartRateData() {
        final Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        final long endTime = cal.getTimeInMillis();
        cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();
        final DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .bucketByTime(10, TimeUnit.MINUTES)
                //.read(DataType.TYPE_HEART_RATE_BPM)
                .aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        Timber.d("loadFitHearRateData");
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(dataReadRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Timber.d("Success with data %s %s", dataReadResponse.getBuckets().size(), dataReadResponse.getDataSets().size());
                        List<Entry> a = new ArrayList<>();/*
                        for(Bucket bucket:dataReadResponse.getBuckets()){
                            for(DataSet dataS:bucket.getDataSets()){
                                for(DataPoint dataPoint:dataS.getDataPoints()){
                                    a.add(new Entry(dataPoint.getTimestamp(TimeUnit.SECONDS),dataPoint.getValue(Field.FIELD_BPM).asFloat()));
                                }
                            }
                        }*/
                        for (DataSet dataS : dataReadResponse.getDataSets()) {
                            for (DataPoint dataPoint : dataS.getDataPoints()) {
                                a.add(new Entry(dataPoint.getTimestamp(TimeUnit.SECONDS), dataPoint.getValue(Field.FIELD_BPM).asFloat()));
                            }
                        }
                        Timber.d("Data entries: %s", a.size());
                        LineDataSet dataSet = new LineDataSet(a, "D");
                        dataSet.setColor(Color.RED);
                        mLineData.addDataSet(dataSet);
                        mLineChart.invalidate();
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Timber.d("Failure with data");
                                Timber.e(e);
                            }
                        })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(Task<DataReadResponse> task) {
                        Timber.i("Update Task finished");
                    }
                });
    }
}
