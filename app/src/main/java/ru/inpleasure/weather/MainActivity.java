package ru.inpleasure.weather;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.view.Window;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.Color;

import ru.inpleasure.weather.model.WeatherDbHelper;
import ru.inpleasure.weather.presenter.*;

public class MainActivity extends Activity
    implements Contract.View
{
    public static final String PREFERENCE_FILE = "weather_prefs";
    public static final String PREFERENCE_KEY_TOKEN = "KEY_TOKEN";
    public static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private TextView textView;
    private Button button;
    private Contract.Presenter presenter;
    private SharedPreferences preferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.rgb(64, 61, 56));
        }
        setContentView(R.layout.main);
        textView = (TextView)findViewById(R.id.activity_main_tv);
        button = (Button)findViewById(R.id.test_button);
        presenter = new WeatherPresenter(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                presenter.onRefreshButtonClick();
            }
        });
        preferences = getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        if (preferences.getString(PREFERENCE_KEY_TOKEN, null) == null) {
            new TokenDialog().show(getFragmentManager(), "TOKEN_DIALOG");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("one", textView.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        textView.setText(state.getString("one"));
    }
    
    @Override
    public Context getContext() {
        return this;
    }
    
    @Override
    public void showWeatherIcon() {
        
    }
    
    @Override
    public void showText(String text) {
        textView.setText(text);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
