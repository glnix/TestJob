package ru.goryachev.testjob.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import ru.goryachev.testjob.R;

public class SearchActivity extends ActionBarActivity {
    Activity activity = this;
    private final static String LOGTAG = "LOG:SearchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

    }

    public void click(View v) {
        Log.d(LOGTAG, "Button inside EditText click!");
    }
}
