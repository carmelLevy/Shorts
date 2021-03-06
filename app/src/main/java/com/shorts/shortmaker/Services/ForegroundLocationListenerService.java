package com.shorts.shortmaker.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.shorts.shortmaker.ActionFactory;
import com.shorts.shortmaker.Actions.Action;
import com.shorts.shortmaker.AppData;
import com.shorts.shortmaker.BroadcastReceivers.NotificationLocationReceiver;
import com.shorts.shortmaker.DataClasses.ActionData;
import com.shorts.shortmaker.DataClasses.LocationData;
import com.shorts.shortmaker.DataClasses.Shortcut;
import com.shorts.shortmaker.FireBaseHandlers.FireStoreHandler;
import com.shorts.shortmaker.R;

import java.util.List;
import java.util.Objects;


public class ForegroundLocationListenerService extends Service {
    static final int LOCATION_SERVICE_ID = 175;
    public static final String START_LOCATION_SERVICE = "startLocationService";
    public static final String STOP_LOCATION_SERVICE = "stopLocationService";
    static final String CHANNEL_ID = "shorts.locationChannel";
    public static final String CLOSE_LOCATION_FOREGROUND = "CLOSE_LOCATION_FOREGROUND";
    public static final String SHORTCUT_ID_EXTRA = "SHORTCUT_ID_EXTRA";

    private Context context;
    private Shortcut currentShortcut;


    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                LocationData locationData = currentShortcut.getLocationData();
                double latitude = locationResult.getLastLocation().getLatitude();
                double longtitude = locationResult.getLastLocation().getLongitude();

                float[] results = new float[1];
                Location.distanceBetween(locationData.getLatitude()
                        , locationData.getLongitude(),
                        latitude,
                        longtitude,
                        results);
                if (results[0] <= locationData.getRadius()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (ActionData actionData : currentShortcut.getActionDataList()) {
                                if (actionData.getCondition() == ActionFactory.Conditions.ON_AT_LOCATION
                                        && actionData.getIsActivated()) {
                                    Action action = ActionFactory.getAction(actionData.getTitle());
                                    if (action != null) {
                                        action.setData(actionData.getData());
                                        action.activate(getApplication(),
                                                getApplicationContext());
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }).start();
                    stopLocationService();
                }


                Log.v("location service", latitude + ", " + longtitude);
                //TODO check radios and activate actions
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void startLocationService() {
        Intent broadcastIntent = new Intent(this, NotificationLocationReceiver.class);
        broadcastIntent.putExtra(NotificationLocationReceiver.EXTRA, CLOSE_LOCATION_FOREGROUND);
        PendingIntent actionIntent = PendingIntent.getBroadcast(this,
                0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = CHANNEL_ID;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, channelId);
        builder.setSmallIcon(R.mipmap.teddy_bear);
        builder.setContentTitle("Will activate when you are at "
                + currentShortcut.getLocationData().getLocationName() + "...");
        builder.addAction(R.mipmap.delete_icon, "Stop service", actionIntent);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");

        if (Build.VERSION.SDK_INT > -Build.VERSION_CODES.O) {
            if (notificationManager != null
                    && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId,
                        "Location Service",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Dexter.withContext(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @SuppressLint("MissingPermission")// checked with dexter
                    @Override
                    public void
                    onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (manager != null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            LocationServices.getFusedLocationProviderClient(context)
                                    .requestLocationUpdates(locationRequest,
                                            locationCallback,
                                            Looper.getMainLooper());
                            startForeground(LOCATION_SERVICE_ID, builder.build());
                        } else {
                            Intent openLocationIntent =
                                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            openLocationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(openLocationIntent);
                            Toast.makeText(context,
                                    "Enable location and try again",
                                    Toast.LENGTH_LONG).show();
                            stopLocationService();
                        }
                    }

                    @Override
                    public void
                    onPermissionRationaleShouldBeShown(List<PermissionRequest> list,
                                                       PermissionToken permissionToken) {
                        //todo dialog?
                    }
                }).check();
    }

    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopSelf();
    }

    private BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), CLOSE_LOCATION_FOREGROUND)) {
                stopLocationService();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String shortcutId = Objects.requireNonNull(intent.getExtras())
                    .getString(SHORTCUT_ID_EXTRA);
            if (action != null && shortcutId != null) {
                AppData appData = (AppData) getApplicationContext();
                appData.fireStoreHandler.getShortcut(shortcutId,
                        new FireStoreHandler.SingleShortcutCallback() {
                            @Override
                            public void onAddedShortcut(String id,
                                                        Shortcut shortcut,
                                                        Boolean success) {
                                if (success) {
                                    IntentFilter filter = new IntentFilter();
                                    filter.addAction(CLOSE_LOCATION_FOREGROUND);
                                    context.registerReceiver(closeReceiver, filter);
                                    currentShortcut = shortcut;
                                    startLocationService();
                                }
                            }
                        });
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(closeReceiver);
    }
}
