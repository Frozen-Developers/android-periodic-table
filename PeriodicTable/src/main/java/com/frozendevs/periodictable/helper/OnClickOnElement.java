package com.frozendevs.periodictable.helper;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.frozendevs.periodictable.activity.PropertiesActivity;

public class OnClickOnElement implements View.OnClickListener {

    private Context context;
    private int atomicNumber;

    public OnClickOnElement(Context context, int atomicNumber) {
        this.context = context;
        this.atomicNumber = atomicNumber;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(context, PropertiesActivity.class);
        intent.putExtra("atomicNumber", atomicNumber);
        context.startActivity(intent);
    }
}
