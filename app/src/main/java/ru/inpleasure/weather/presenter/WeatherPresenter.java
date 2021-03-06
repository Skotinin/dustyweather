package ru.inpleasure.weather.presenter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.TextView;

import ru.inpleasure.weather.Contract;
import ru.inpleasure.weather.Locator;
import ru.inpleasure.weather.api.WeatherApi;
import ru.inpleasure.weather.api.dto.WeatherDto;
import ru.inpleasure.weather.model.WeatherModel;
import ru.inpleasure.weather.model.dbo.Weather;
import ru.inpleasure.weather.draw.WeatherDrawer;

import java.util.List;


public class WeatherPresenter
    implements Contract.Presenter
{
    private Context context;
    private Contract.Model model;
    private Contract.View view;
    private Contract.Api api;
    private Contract.Drawer drawer;
    private Locator locator;
    private WeatherLoader weatherLoader;
    
    
    public WeatherPresenter(Contract.View view) {
        this.view = view;
        context = view.getContext();
        model = new WeatherModel(context);
        locator = new Locator(this);
        api = new WeatherApi(context);
        
    }
    
    @Override
    public Context getContext() {
        return context;
    }
    
    @Override
    public void onDestroy() {
        locator.stop();
        if (weatherLoader != null &&
            weatherLoader.getStatus() == AsyncTask.Status.RUNNING)
            weatherLoader.cancel(false);
        weatherLoader = null;
    }
    
    @Override
    public void onHistoryButtonClick() {
        
    }
    
    @Override
    public void onRefreshButtonClick() {
        if (weatherLoader == null) {
            weatherLoader = new WeatherLoader(view);
            weatherLoader.execute(locator.getLocation());
        }
        else if (weatherLoader.getStatus() != AsyncTask.Status.RUNNING)
            weatherLoader.execute(locator.getLocation());
    }
    
    @Override
    public void onLocationReceived(Location location) {
        weatherLoader = new WeatherLoader(view);
        weatherLoader.execute(location);
    }

    @Override
    public void initialize() {
        view.showWeather(model.getLastWeather());
        locator.start();
    }
    
    @Override
    public void onLocationError() {
        locator.stop();
    }

    @Override
    public void draw() {
        drawer = new WeatherDrawer(view);
        List<Weather> weatherList = model.getWeather();
        if (weatherList == null)
            return;
        double[] temperatures = new double[weatherList.size()];
        for (int i = 0; i < temperatures.length;i++)
            temperatures[i] = weatherList.get(i).getMainTemperature();
        drawer.drawDots(temperatures);
    }


    class WeatherLoader extends AsyncTask <Location, Void, Weather>
    {
        private Contract.View view;

        public WeatherLoader(final Contract.View view) {
            this.view = view;
        }

        @Override
        protected Weather doInBackground(Location... locations)
        {
            WeatherDto weatherDto = api.getWeather(locations[0]);
            if (weatherDto == null) return null;
            Weather weather = new Weather();
            weather.setLongitude(weatherDto.getCoord().getLon());
            weather.setLatitude(weatherDto.getCoord().getLat());
            weather.setCountry(weatherDto.getSys().getCountry());
            weather.setSunrise(weatherDto.getSys().getSunrise());
            weather.setSunset(weatherDto.getSys().getSunset());
            weather.setWeatherId(weatherDto.getWeather().get(0).getId());
            weather.setWeatherMain(weatherDto.getWeather().get(0).getMain());
            weather.setWeatherDescription(weatherDto.getWeather().get(0).getDescription());
            weather.setWeatherIcon(weatherDto.getWeather().get(0).getIcon());
            weather.setMainTemperature(weatherDto.getMain().getTemp());
            weather.setMainHumidity(weatherDto.getMain().getHumidity());
            weather.setMainPressure(weatherDto.getMain().getPressure());
            weather.setMainMinTemperature(weatherDto.getMain().getTempMin());
            weather.setMainMaxTemperature(weatherDto.getMain().getTempMax());
            weather.setMainSeaLevel(weatherDto.getMain().getSeaLevel());
            weather.setMainGroundLevel(weatherDto.getMain().getGrndLevel());
            weather.setWindSpeed(weatherDto.getWind().getSpeed());
            weather.setWindDegrees(weatherDto.getWind().getDeg());
            weather.setCloudsAll(weatherDto.getClouds().getAll());
            weather.setTimestamp(weatherDto.getDt());
            weather.setSystemId(weatherDto.getId());
            weather.setCityName(weatherDto.getName());
            weather.setSystemCode(weatherDto.getCod());
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather)
        {
            if (weather == null || view == null) return;
            model.addWeather(weather);
            view.showWeather(weather);
        }
    }
}
