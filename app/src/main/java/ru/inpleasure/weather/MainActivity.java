package ru.inpleasure.weather;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.Color;

import java.lang.reflect.Field;

import ru.inpleasure.weather.api.dto.WeatherDto;
import ru.inpleasure.weather.draw.WeatherDrawer;
import ru.inpleasure.weather.model.DbField;
import ru.inpleasure.weather.model.dbo.Weather;
import ru.inpleasure.weather.presenter.WeatherPresenter;

public class MainActivity extends Activity
    implements Contract.View
{
    public static final String PREFERENCE_FILE = "weather_prefs";
    public static final String PREFERENCE_KEY_TOKEN = "KEY_TOKEN";
    public static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private ImageView weatherIcon;
    private TextView cityName;
    private TextView weatherMain;
    private TextView weatherDescription;
    private TextView weatherTempMin;
    private TextView weatherTemp;
    private TextView weatherTempMax;
    private ImageView drawView;
    private Typeface poiretOneTypeface;
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
        presenter = new WeatherPresenter(this);
        poiretOneTypeface = Typeface.createFromAsset(getAssets(), "fonts/poiret_one.ttf");
        weatherIcon = (ImageView)findViewById(R.id.main_weather_icon);
        weatherIcon.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                presenter.onRefreshButtonClick();
            }
        });
        cityName = (TextView)findViewById(R.id.main_city_name);
        cityName.setTypeface(poiretOneTypeface);
        weatherMain = (TextView)findViewById(R.id.main_weather_main);
        weatherMain.setTypeface(poiretOneTypeface);
        weatherDescription = (TextView)findViewById(R.id.main_weather_description);
        weatherDescription.setTypeface(poiretOneTypeface);
        weatherTempMin = (TextView)findViewById(R.id.main_weather_temp_min);
        weatherTemp = (TextView)findViewById(R.id.main_weather_temp);
        weatherTempMax = (TextView)findViewById(R.id.main_weather_temp_max);
        drawView = (ImageView) findViewById(R.id.main_draw_view);
        preferences = getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        if (preferences.getString(PREFERENCE_KEY_TOKEN, null) == null) {
            TokenDialog dialog = new TokenDialog();
            dialog.setPresenter(presenter);
            dialog.show(getFragmentManager(), "TOKEN_DIALOG");
        }
        else
            presenter.initialize();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

    }
    
    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Canvas getCanvas() {
        Bitmap bitmap = Bitmap.createBitmap(
                100, 100, Bitmap.Config.ARGB_8888);
        drawView.setImageBitmap(bitmap);
        return new Canvas(bitmap);
    }

    @Override
    public void showWeather(Weather weather)
    {
        weatherIcon.setImageResource(WeatherDto.WEATHER_ICONS.get(weather.getWeatherIcon()));
        cityName.setText(weather.getCityName());
        weatherMain.setText(weather.getWeatherMain());
        weatherDescription.setText(weather.getWeatherDescription());
        weatherTempMin.setText(String.valueOf(weather.getMainMinTemperature()));
        weatherTemp.setText(String.valueOf(weather.getMainTemperature()));
        weatherTempMax.setText(String.valueOf(weather.getMainMaxTemperature()));
    }

    @Override
    public void draw() {
        drawView.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
