package ru.goryachev.testjob.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.util.HashMap;
import java.util.Map;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import ru.goryachev.testjob.R;
import ru.goryachev.testjob.serviceClass.HTTPClient;

public class ValidationActivity extends ActionBarActivity implements View.OnClickListener {
    Activity activity = this;
    //   Элементы экрана
    private EditText validationCodeEditText;
    private Button okButton;
    private TextView forgetText;

    // Для доступа к SharedPreferences
    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;

    SMSBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation);
//        Найдем элементы экрана
        validationCodeEditText = (EditText) findViewById(R.id.code_input);
        okButton = (Button) findViewById(R.id.button_ok);
        forgetText = (TextView) findViewById(R.id.text_forget);

        //Делаем слушателем нажатия кнопки текущую активити
        okButton.setOnClickListener(this);
        forgetText.setOnClickListener(this);

        //Получаем доступ к SharedPreferences
        preferences = activity.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        preferencesEditor = preferences.edit();

        if (getIntent().getBooleanExtra("Start_SMS_Listner", false)) {
            receiver = new SMSBroadcastReceiver();
            registerReceiver(receiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_ok:
                String code = validationCodeEditText.getText().toString();
                sendRegRequest(code);
                break;

            case R.id.text_forget:
                preferencesEditor.putInt("Application_status", 0);
                preferencesEditor.apply();
                startActivity(new Intent(activity, LoginActivity.class));
                finish();
                break;
        }
    }

    //    Проверка кода подтверждения на валидность
    private boolean codeCheck(String code) {
        return code.length() == 4;
    }

    //     Регистрация на сервере
    private void sendRegRequest(String code) {
        if (codeCheck(code)) {
            final ProgressDialog progressDialog = new ProgressDialog(activity, ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Регистрация...");
            progressDialog.show();
            HTTPClient httpClient = HTTPClient.getInstance();
//                    Параметры запроса
            Map<String, String> params = new HashMap<>();
            params.put("CheckerId", preferences.getString("CheckerId", ""));
            params.put("SmsCode", code);
            httpClient.post("http://phasorweb.com/sendcode/", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    if (statusCode == 200) {
                        preferencesEditor.putInt("Application_status", 2);
                        preferencesEditor.apply();
                        Intent i = new Intent(activity, WorkspaceActivity.class);

                        progressDialog.dismiss();
                        startActivity(i);
                        finish();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    progressDialog.dismiss();
                    switch (statusCode) {
                        case 0:
                            Crouton.cancelAllCroutons();
                            Crouton.makeText(activity, "Проверьте соединение с интернетом", Style.ALERT).show();
                            validationCodeEditText.requestFocus();
                            break;
                        case 400:
                            Crouton.cancelAllCroutons();
                            Crouton.makeText(activity, "Неверно введен проверочный код", Style.ALERT).show();
                            validationCodeEditText.requestFocus();
                            break;
                    }
                }
            });
        } else {
            Crouton.cancelAllCroutons();
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            Crouton.makeText(activity, "Неверно введен проверочный код", Style.ALERT).show();
            YoYo.with(Techniques.Shake).duration(500).playOn(validationCodeEditText);
            vibrator.vibrate(500);
            validationCodeEditText.requestFocus();
        }
    }

    //Ресивер для перехвата входящих SMS
    public class SMSBroadcastReceiver extends BroadcastReceiver {

        private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(SMS_RECEIVED)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    final SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    if (messages.length > -1) {
                        String msgBody = messages[0].getDisplayMessageBody();
                        if (msgBody.contains("Checker code:")) {
                            String code = msgBody.substring(msgBody.length() - 4); // Если код всегда будет 4 значный
                            sendRegRequest(code);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
        if (receiver != null)
            unregisterReceiver(receiver);
    }
}
