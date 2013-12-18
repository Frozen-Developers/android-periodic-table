package com.frozendevs.periodic.table.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.frozendevs.periodic.table.R;
import com.frozendevs.periodic.table.helper.Database;
import com.frozendevs.periodic.table.model.ElementDetails;

public class DetailsActivity extends ActionBarActivity {

    private ElementDetails elementDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        elementDetails = Database.getElementDetails(this, getIntent().getIntExtra("atomicNumber", 1));

        getSupportActionBar().setTitle(elementDetails.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wiki:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(elementDetails.getWikiLink())));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
