package com.example.shortmaker.Actions;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.DialogFragment;

import com.example.shortmaker.ActionDialogs.ActionDialog;
import com.example.shortmaker.ActionDialogs.AlarmClockDialog;
import com.example.shortmaker.ActionDialogs.TextMessageDialog;
import com.example.shortmaker.R;

import java.util.List;

import ir.mirrajabi.searchdialog.core.Searchable;

public class ActionSendTextMessage implements Action, Searchable {

    public static final String WHATSAPP_PACKAGE_NAME = "com.whatsapp";

    private Context context;
    private TextMessageDialog dialog;
    private boolean sendThroughWhatsapp;

    public ActionSendTextMessage(Context context) {
        this.context = context;
        this.dialog = new TextMessageDialog();
    }


    @Override
    public void activate() {
        Intent sendIntent = new Intent();
        if (sendThroughWhatsapp) {
            sendIntent.setPackage(WHATSAPP_PACKAGE_NAME);
        }
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send."); //TODO - change to a costumized user text
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    @Override
    public ActionDialog getDialog() {
        return dialog;
    }


    @Override
    public void setData(List<String> data) {

    }

    @Override
    public String getTitle() {
        return "Send text message action";
    }

    @Override
    public int getImageResource() {
        return R.drawable.text_message_icon;
    }
}
