package com.shorts.shortmaker.ActionDialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.shorts.shortmaker.R;

import java.util.ArrayList;


public class ChangeOrientationDialog extends ActionDialog {

    private int mode;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.orientation_dialog, null);

        initializeDialogViews(builder, view);
        return builder.create();
    }

    protected void initializeDialogViews(AlertDialog.Builder builder, View view) {
        setModesSpinner(view);
        ImageView imageView = view.findViewById(R.id.imageView);
        setDialogImage(imageView, R.drawable.orientation_gif);
        buildDialog(builder, view);
    }

    protected void buildDialog(AlertDialog.Builder builder, View view) {
        builder.setView(view)
                .setTitle("Change orientation")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<String> results = new ArrayList<>();
                        results.add(String.valueOf(mode));
                        listener.applyUserInfo(results);
                    }
                });
    }

    protected void setModesSpinner(View view) {
        String[] modes = {"Landscape", "Portrait"};
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, modes);
        // set Adapter
        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setSpinner(spinner);


    }

    private void setSpinner(Spinner spinner) {
        //Register a callback to be invoked when an item in this AdapterView has been selected
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long id) {
                mode = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }

        });
    }
}
