package com.github.donmahallem.heartfit.job;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class HeartRateJob extends JobService {
    private static final String TAG = "SyncService";

    @Override
    public boolean onStartJob(JobParameters params) {
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    public void writeData(float bpm) {
        final Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        final long endTime = cal.getTimeInMillis();

        DataSource dataSource =
                new DataSource.Builder()
                        .setAppPackageName(this)
                        .setDataType(DataType.TYPE_HEART_RATE_BPM)
                        .setStreamName("Test Heart Rate")
                        .setType(DataSource.TYPE_RAW)
                        .build();

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint =
                dataSet.createDataPoint()
                        .setTimestamp(endTime, TimeUnit.MILLISECONDS);

        dataPoint.getValue(Field.FIELD_BPM).setFloat(bpm);
        dataSet.add(dataPoint);
        Task<Void> response = Fitness
                .getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .insertData(dataSet);
        response.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Timber.e(e);
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void o) {
                        Timber.d("DATA SAVED");
                    }
                });
    }
}