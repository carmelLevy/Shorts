package com.shorts.shortmaker.Actions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.shorts.shortmaker.ActionDialogs.ActionDialog;
import com.shorts.shortmaker.ActionDialogs.TextMessageDialog;

import java.util.ArrayList;
import java.util.List;


public class ActionSendTextMessage implements Action {

    private static final int REQUEST_CODE_PERMISSION_SMS = 1546;

    private TextMessageDialog dialog;
    private Activity activity;
    private String whoToSend;
    private String message;
    private List<String> recipientsList;

    public ActionSendTextMessage() {
        this.dialog = new TextMessageDialog();
    }


    @Override
    public void activate(Context context, final Activity activity) {
        this.activity = activity;

        Dexter.withContext(activity)
                .withPermission(Manifest.permission.SEND_SMS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        sendSms();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(activity,
                                "Please confirm permission",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        Toast.makeText(activity,
                                "Please confirm permission",
                                Toast.LENGTH_SHORT).show();
                        // TODO - maybe to a dialog here(can be added using the dexter library automatically!!!)
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void sendSms() {
        for (int i = 0; i < recipientsList.size(); i++) {
            SmsManager.getDefault().sendTextMessage(
                    recipientsList.get(i),
                    null,
                    message,
                    null,
                    null);
        }
    }


    @Override
    public ActionDialog getDialog() {
        return dialog;
    }


    @Override
    public void setData(List<String> data) {
        message = data.get(0);
        recipientsList = data.subList(1, data.size());
    }
}
