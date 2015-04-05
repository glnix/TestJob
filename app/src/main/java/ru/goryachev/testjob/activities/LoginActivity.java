package ru.goryachev.testjob.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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
    //Лого оператора
    private ImageView logoImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent i = getIntent();
//      Находиим все элементы экрана
        phoneEditText = (EditText) findViewById(R.id.input_phone);
        regButton = (Button) findViewById(R.id.button_login);
        logoImage = (ImageView) findViewById(R.id.image_operator_logo);
        regButton.setOnClickListener(this);

//        Установим логотип оператора
        Drawable operatorLogo = getOperatorLogo();
        if (operatorLogo != null) logoImage.setImageDrawable(operatorLogo);

        if (i.getBooleanExtra("First_start", false)) {
            showHelloDialog();
        }
        phoneEditText.requestFocus();


    }

    // Возвращает картинку с логотипом оператора
    private Drawable getOperatorLogo() {
        TelephonyManager tel = (TelephonyManager) getSystemService(activity.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();
        int mnc = 0;
        if (networkOperator != null) {
            try {
                mnc = Integer.parseInt(networkOperator);
            } catch (NumberFormatException e) {
                Log.d(getPackageName(), "Exception MNC convert: NumberFormatException: Invalid int: \"\" ");
            }
        }
        Drawable logoDrawable = null;
        switch (mnc) {
            case 25001:
                logoDrawable = activity.getResources().getDrawable(R.drawable.ic_mts);
                break;

            case 25002:
                logoDrawable = activity.getResources().getDrawable(R.drawable.ic_megafon);
                break;

            case 25039:
            case 25003:
                logoDrawable = activity.getResources().getDrawable(R.drawable.ic_rostelecom);
                break;

            case 25035:
                logoDrawable = activity.getResources().getDrawable(R.drawable.ic_motiv);
                break;

            case 25099:
            case 43701:
                logoDrawable = activity.getResources().getDrawable(R.drawable.ic_beeline);
                break;
        }
        return logoDrawable;
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
                    sendRegRequest(phoneNumber);
                } else {
                    Crouton.cancelAllCroutons();
                    Crouton.makeText(activity, "Неверно введен номер телефона", Style.ALERT).show();
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    YoYo.with(Techniques.Shake).duration(500).playOn(phoneEditText);
                    vibrator.vibrate(500);
                    phoneEditText.requestFocus();
                }
                break;
        }
    }

    //    Отправка запроса к серверу
    private void sendRegRequest(String phoneNumber) {
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
                        Crouton.cancelAllCroutons();
                        Crouton.makeText(activity, "Проверьте соединение с интернетом", Style.ALERT).show();
                        phoneEditText.requestFocus();
                        break;
                    case 400:
                        Crouton.cancelAllCroutons();
                        Crouton.makeText(activity, "Неверно введен номер телефона", Style.ALERT).show();
                        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        YoYo.with(Techniques.Shake).duration(500).playOn(phoneEditText);
                        vibrator.vibrate(500);
                        phoneEditText.requestFocus();
                        break;
                }
            }
        });
    }

    //    Проверка корректности номера телефона
    private boolean checkPhoneNumber(String phoneNumber) {
        return phoneNumber.length() == 10;
    }

    @Override
    protected void onDestroy() {
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }
}
