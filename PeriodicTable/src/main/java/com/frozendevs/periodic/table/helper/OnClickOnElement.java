package com.frozendevs.periodic.table.helper;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.frozendevs.periodic.table.activity.DetailsActivity;

public class OnClickOnElement implements View.OnClickListener {

    private Context context;
    private int atomicNumber;

    public OnClickOnElement(Context context, int atomicNumber) {
        this.context = context;
        this.atomicNumber = atomicNumber;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra("atomicNumber", atomicNumber);
        context.startActivity(intent);
    }
}
