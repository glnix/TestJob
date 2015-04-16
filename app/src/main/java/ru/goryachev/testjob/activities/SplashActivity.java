package ru.goryachev.testjob.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import ru.goryachev.testjob.R;

public class SplashActivity extends Activity {
    //Время показа активити включая вермя анимации
    private static final int SPLASH_SCREEN_SHOW_TIME = 2000;
    //Время анимации первого текста логотипа
    private static final int FIRST_ANIMATION_DURATION = 1500;
    public static final String TAG = "LOGTAG";
    //Контекст
    private Context context = this;
    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //Анимация
        YoYo.with(Techniques.FadeIn).duration(FIRST_ANIMATION_DURATION).playOn(findViewById(R.id.text_logo_1));

        //Задержка
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Здесь проверяем состояние приложения и переводим на соответсвующий экран
                SharedPreferences preferences = activity.getSharedPreferences("AppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                String statusPrefName = "Application_status";
                int status = preferences.getInt(statusPrefName, -1);
                Intent i;
                //DEBUG: заменить цифру пременной status
                switch (2) {
//                    Первый запуск
                    case -1:
                        i = new Intent(context, LoginActivity.class);
//                    Сообщаем, что стартуем первый раз
                        i.putExtra("First_start", true);
                        startActivity(i);
                        editor.putInt(statusPrefName, 0);
                        editor.apply();
                        break;
//                    Последующие запуски, запрос на регистрацию не отправлен
                    case 0:
                        i = new Intent(context, LoginActivity.class);
                        i.putExtra("First_start", false);
                        startActivity(i);
                        break;
//                    Запрос на регистрацию отправлен, ответ не получен
                    case 1:
                        i = new Intent(context, ValidationActivity.class);
                        startActivity(i);
                        break;
//                      Приложение зарегестрировано
                    case 2:
                        i = new Intent(context, SearchActivity.class);
                        startActivity(i);
                        break;
                }
                //Завершаем текущее активити
                finish();
            }
        }, SPLASH_SCREEN_SHOW_TIME);
    }
}