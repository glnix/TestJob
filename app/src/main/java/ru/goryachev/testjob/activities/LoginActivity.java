package ru.goryachev.testjob.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {
    private Activity activity = this;
    //  Поле для ввода номера телефона
    private EditText phoneEditText;
    //  Кнопка отправки запроса
    private Button regButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent i = getIntent();
//      Находиим все элементы экрана
        phoneEditText = (EditText) findViewById(R.id.input_phone);
        regButton = (Button) findViewById(R.id.button_login);
        regButton.setOnClickListener(this);
        if (i.getBooleanExtra("First_start", true)) {
            showHelloDialog();
        }
        phoneEditText.requestFocus();
    }

    //    Диалог рассказывающй о сервисе
    private void showHelloDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle("Новости")
                .setMessage("Данное приложение позволяет зарегистрироваться на очень крутом" +
                        " сервисе, используя SMS аутентификацию.\nЕсли зарегистрируетесь," +
                        " то получите тортик.")
                .setCancelable(false)
                .setPositiveButton("Я хочу тортик!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialogBuilder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:
                final String phoneNumber = phoneEditText.getText().toString();
                if (checkPhoneNumber(phoneNumber)) {
                    HTTPClient httpClient = HTTPClient.getInstance();
//                    Параметры запроса
                    Map<String, String> params = new HashMap<>();
                    params.put("Phone", "+7" + phoneNumber);
//                    DEBUG!!!
                    params.put("dosms", "1");

                    final ProgressDialog progressDialog = new ProgressDialog(activity, ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage("Регистрация...");
                    progressDialog.show();

                    httpClient.post("http://phasorweb.com/register/", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            boolean hasId = false;
                            SharedPreferences prefs = activity.getSharedPreferences("AppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();

                            if (statusCode == 200) {
                                for (Header h : headers) {
                                    if (h.getName().equals("CheckerId")) {
                                        String id = h.getValue();
                                        if (!id.equals("")) {
                                            editor.putString("CheckerId", id);
                                            editor.apply();
                                            hasId = true;
                                            break;
                                        }
                                    }
                                }

                                if (hasId) {
                                    Intent i = new Intent(activity, ValidationActivity.class);
                                    i.putExtra("Start_SMS_Listner", true);

                                    editor.putInt("Application_status", 1);
                                    editor.apply();

                                    startActivity(i);
                                    finish();
                                }
                            }
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            progressDialog.dismiss();
                            switch (statusCode) {
                                case 0:
                                    Crouton.makeText(activity, "Проверьте соединение с интернетом", Style.ALERT).show();
                                    phoneEditText.requestFocus();
                                    break;
                                case 400:
                                    Crouton.makeText(activity, "Не верно введен номер телефона", Style.ALERT).show();
                                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                    YoYo.with(Techniques.Shake).duration(500).playOn(phoneEditText);
                                    vibrator.vibrate(500);
                                    phoneEditText.requestFocus();
                                    break;
                            }
                        }
                    });
                } else {
                    Crouton.makeText(activity, "Не верно введен номер телефона", Style.ALERT).show();
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    YoYo.with(Techniques.Shake).duration(500).playOn(phoneEditText);
                    vibrator.vibrate(500);
                    phoneEditText.requestFocus();
                }
                break;
        }
    }

    private boolean checkPhoneNumber(String phoneNumber) {
        return phoneNumber.length() == 10;
    }


}
