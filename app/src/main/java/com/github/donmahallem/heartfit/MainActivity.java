package com.github.donmahallem.heartfit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.BleDevice;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.BleScanCallback;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 230;
    private final static int REQUEST_BLUETOOTH_ADMIN_PERMISSION = 2929;
    private OnDataPointListener mListener;
    private Switch mSendDataSwitch;
    private TextView mTxtCurrentBpm;
    private Button mBtnSubscribe, mBtnUnsubcribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.findViewById(R.id.btnSignin).setOnClickListener(this);
        this.findViewById(R.id.btnHeartData).setOnClickListener(this);
        this.mSendDataSwitch = this.findViewById(R.id.switch1);
        this.mTxtCurrentBpm = this.findViewById(R.id.txtCurrentBpm);
        this.mBtnSubscribe = this.findViewById(R.id.btnSubscribe);
        this.mBtnSubscribe.setOnClickListener(this);
        this.findViewById(R.id.btnUnsubscribe).setOnClickListener(this);
    }

    public void subscribeToData() {
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_HEART_RATE_BPM)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.i("Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.i("There was a problem subscribing.");
                    }
                });
    }

    public boolean sendData() {
        return this.mSendDataSwitch.isChecked();
    }

    public void claimDevice(BleDevice device) {

        Fitness.getBleClient(this,
                GoogleSignIn.getLastSignedInAccount(this))
                .claimBleDevice(device)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.d("Device claimed");
                        getDataSources();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Timber.e(e);
                    }
                });
    }

    private void getDataSources() {
        Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .findDataSources(
                        new DataSourcesRequest.Builder()
                                .setDataTypes(DataType.TYPE_HEART_RATE_BPM)
                                .setDataSourceTypes(DataSource.TYPE_RAW)
                                .build())
                .addOnSuccessListener(
                        new OnSuccessListener<List<DataSource>>() {
                            @Override
                            public void onSuccess(List<DataSource> dataSources) {
                                for (DataSource dataSource : dataSources) {
                                    Timber.i("Data source found: " + dataSource.toString());
                                    Timber.i("Data Source type: " + dataSource.getDataType().getName());

                                    // Let's register a listener to receive Activity data!
                                    if (dataSource.getDataType().equals(DataType.TYPE_HEART_RATE_BPM)
                                            && mListener == null) {
                                        Timber.i("Data source for LOCATION_SAMPLE found!  Registering.");
                                        registerFitnessDataListener(dataSource, DataType.TYPE_HEART_RATE_BPM);
                                    }
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Timber.e(e);
                            }
                        });
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        Timber.d("Got datasource %s - %s", dataSource.getDataType(), dataType);
        mListener =
                new OnDataPointListener() {
                    @Override
                    public void onDataPoint(DataPoint dataPoint) {
                        for (Field field : dataPoint.getDataType().getFields()) {
                            Value val = dataPoint.getValue(field);
                            Timber.i("Detected DataPoint field: " + field.getName());
                            Timber.i("Detected DataPoint value: " + val);
                            mTxtCurrentBpm.setText("" + val.asFloat() + " BPM");
                            if (MainActivity.this.sendData())
                                writeData(val.asFloat());
                        }
                    }
                };

        Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .add(
                        new SensorRequest.Builder()
                                .setDataSource(dataSource) // Optional but recommended for custom data sets.
                                .setDataType(dataType) // Can't be omitted.
                                .setSamplingRate(1, TimeUnit.SECONDS)
                                .build(),
                        mListener)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Timber.i("Listener registered!");
                                } else {
                                    Timber.e(task.getException());
                                }
                            }
                        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hasGooglePermissions()) {
            return;
        }
        queryPermission();
        /*
        Fitness.getBleClient(this,
                GoogleSignIn.getLastSignedInAccount(this))
                .listClaimedBleDevices()
                .addOnSuccessListener(new OnSuccessListener<List<BleDevice>>() {
                    @Override
                    public void onSuccess(List<BleDevice> bleDevices) {
                        Timber.d("%s Devices claimed", bleDevices.size());
                        getDataSources();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Timber.e(e);
                    }
                });
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .listSubscriptions(DataType.TYPE_ACTIVITY_SAMPLES)
                .addOnSuccessListener(new OnSuccessListener<List<Subscription>>() {
                    @Override
                    public void onSuccess(List<Subscription> subscriptions) {
                        for (Subscription sc : subscriptions) {
                            DataType dt = sc.getDataType();
                            Timber.d( "Active subscription for data type: " + dt.getName());
                        }
                    }
                });*/
    }

    public void writeData(float bpm) {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

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

    public void registerBleDevice() {
        BleScanCallback bleScanCallbacks = new BleScanCallback() {
            @Override
            public void onDeviceFound(BleDevice device) {
                Timber.d("Device found %s", device.getName());
                // A device that provides the requested data types is available
                claimDevice(device);
            }

            @Override
            public void onScanStopped() {
                Timber.d("no device found");
                // The scan timed out or was interrupted
            }
        };

        Task<Void> response = Fitness.getBleClient(this,
                GoogleSignIn.getLastSignedInAccount(this))
                .startBleScan(Arrays.asList(DataType.TYPE_HEART_RATE_BPM),
                        10, bleScanCallbacks);
    }

    private FitnessOptions createFitnessOptions() {
        return FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_READ)
                .build();

    }

    public boolean hasGooglePermissions() {
        return this.hasGooglePermissions(createFitnessOptions());
    }

    public boolean hasGooglePermissions(FitnessOptions fitnessOptions) {
        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions);
    }

    public void signinToGoogle() {
        final FitnessOptions fitnessOptions = createFitnessOptions();
        if (!hasGooglePermissions(fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            accessGoogleFit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("Result Codes %s %s", requestCode, resultCode);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                accessGoogleFit();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void accessGoogleFit() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.HOURS)
                .build();


        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Timber.d("onSuccess()");
                        for (Bucket b : dataReadResponse.getBuckets()) {
                            Timber.d("BUCKET========");
                            for (DataSet dataSet : b.getDataSets()) {
                                for (DataPoint dataPoint : dataSet.getDataPoints())
                                    Timber.d("Rate %s", dataPoint.getValue(Field.FIELD_BPM));
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.e(e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Timber.d("onComplete()");
                    }
                });
    }

    public void a() {
        Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .findDataSources(
                        new DataSourcesRequest.Builder()
                                .setDataTypes(DataType.TYPE_LOCATION_SAMPLE)
                                .setDataSourceTypes(DataSource.TYPE_RAW)
                                .build())
                .addOnSuccessListener(
                        new OnSuccessListener<List<DataSource>>() {
                            @Override
                            public void onSuccess(List<DataSource> dataSources) {
                                for (DataSource dataSource : dataSources) {
                                    Timber.i("Data source found: " + dataSource.toString());
                                    Timber.i("Data Source type: " + dataSource.getDataType().getName());

                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Timber.e(e);
                            }
                        });
    }

    public void queryPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BODY_SENSORS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BODY_SENSORS},
                        REQUEST_BLUETOOTH_ADMIN_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            registerBleDevice();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_BLUETOOTH_ADMIN_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    registerBleDevice();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignin:
                signinToGoogle();
                //registerBleDevice();
                break;
            case R.id.btnHeartData:
                //queryPermission();
                //registerBleDevice();

                break;
            case R.id.btnSubscribe:
                subscribeToData();
                break;
            case R.id.btnUnsubscribe:
                startActivity(new Intent(this, DeviceActivity.class));
                break;
        }
    }
}
