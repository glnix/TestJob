package ru.goryachev.testjob.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import ru.goryachev.testjob.R;

public class WorkspaceActivity extends ActionBarActivity {
    Button cakeButton;
    Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_space);
        cakeButton = (Button) findViewById(R.id.button_cake);
        cakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = getLayoutInflater();
                View dialogView = layoutInflater.inflate(R.layout.cake_dialog, null);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder
                        .setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton("Я негодую!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialogBuilder.show();

            }
        });
    }


}
