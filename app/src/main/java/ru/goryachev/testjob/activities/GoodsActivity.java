package ru.goryachev.testjob.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import ru.goryachev.testjob.R;

/**
 * Created by Aleksey Goryachev on 16.04.2015.
 */
public class GoodsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods);
    }
}
