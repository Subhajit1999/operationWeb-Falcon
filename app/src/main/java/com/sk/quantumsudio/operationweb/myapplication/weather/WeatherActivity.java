package com.sk.quantumsudio.operationweb.myapplication.weather;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.quantumsudio.operationweb.myapplication.R;
import com.sk.quantumsudio.operationweb.myapplication.utils.Function;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";
    public static final String OPEN_WEATHER_MAP_API = "8840c1890ba5ea455d036c972ea8d2a2";  //API KEY here

     TextView selectCity, cityField, detailsField, currentTemperatureField, humidity_field,
            pressure_field, weatherIcon, updatedField;
     ProgressBar loader;
    Typeface weatherFont;
    static Double Latitude,Longitude;
    Double tempFromKtoC;
    String city = "",country = "";
    int id = 0;  //checks if the activity is loading for first time or not

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

         loader = (ProgressBar) findViewById(R.id.loader);     //widgets initialization
         selectCity = (TextView) findViewById(R.id.selectCity);
         cityField = (TextView) findViewById(R.id.city_field);
         updatedField = (TextView) findViewById(R.id.updated_field);
         detailsField = (TextView) findViewById(R.id.details_field);
         currentTemperatureField = (TextView) findViewById(R.id.current_temperature_field);
         humidity_field = (TextView) findViewById(R.id.humidity_field);
         pressure_field = (TextView) findViewById(R.id.pressure_field);
         weatherIcon = (TextView) findViewById(R.id.weather_icon);

         weatherFont = Typeface.createFromAsset(getAssets(),"fonts/weathericons-regular-webfont.ttf");
         weatherIcon.setTypeface(weatherFont);

         Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Latitude = bundle.getDouble("lat");  //latitute
            Longitude = bundle.getDouble("long"); //longitute from HomeActivity
        }
        taskLoadUp(city+", "+country);
        selectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = 1;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(WeatherActivity.this);
                alertDialog.setTitle("Change Location");
                LinearLayout layout = new LinearLayout(getApplicationContext());  //alertDialog viewgroup
                layout.setOrientation(LinearLayout.VERTICAL);
                final EditText input_city = new EditText(WeatherActivity.this);  //edittext city
                input_city.setText(city);
                input_city.setHint("City");
                layout.addView(input_city);

                final EditText input_country = new EditText(WeatherActivity.this);  //edittext country
                input_country.setText(country);
                input_country.setHint("Country (E.g. IN for India)");
                layout.addView(input_country);

                alertDialog.setView(layout);

                alertDialog.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        city = input_city.getText().toString();
                        country = input_country.getText().toString();
                        String location = city+", "+country;
                        taskLoadUp(location);
                    }
                });
                alertDialog.setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        });
    }
     void taskLoadUp(String query){
        if(Function.isNetworkAvailable(getApplicationContext())){
            DownloadWeather task = new DownloadWeather();
            if(id==0){   // for default loading (from lat and long)
                task.execute();
            }else if(id==1){ //for normal loading (in case the location changed manually)
                task.execute(query);
            }
        }else{
            Toast.makeText(getApplicationContext(),"No active Internet Connection",Toast.LENGTH_SHORT).show();
        }
    }
    public class DownloadWeather extends AsyncTask< String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loader.setVisibility(View.VISIBLE);

        }
        protected String doInBackground(String...args) {
            String xml="";
            if(id==0){
                xml = Function.excuteGet("http://api.openweathermap.org/data/2.5/weather?" + "lat=" + Latitude+ "&lon="
                        + Longitude + "&appid=" +OPEN_WEATHER_MAP_API);
            }else if(id==1){
                xml = Function.excuteGet("http://api.openweathermap.org/data/2.5/weather?q=" + args[0] +
                        "&units=metric&appid=" + OPEN_WEATHER_MAP_API);
            }
            return xml;
        }
        @Override
        protected void onPostExecute(String xml) {

            try {
                JSONObject jsonObject = new JSONObject(xml);
                JSONObject details = jsonObject.getJSONArray("weather").getJSONObject(0);
                JSONObject main = jsonObject.getJSONObject("main");
                DateFormat df = DateFormat.getDateTimeInstance();

                city = jsonObject.getString("name").toUpperCase(Locale.US);  //getting the city and country and storing them in different variables
                country = jsonObject.getJSONObject("sys").getString("country");
                String cityFieldText = city + ", " + country;
                String humidityText = "Humidity: " + main.getString("humidity") + "%";
                String pressureText = "Pressure: " + main.getString("pressure") + " hPa";
                String detailsText = details.getString("description").toUpperCase(Locale.US);
                String dateText = df.format(new Date(jsonObject.getLong("dt") * 1000));
                Spanned weather_icon = Html.fromHtml(Function.setWeatherIcon(details.getInt("id"),
                        jsonObject.getJSONObject("sys").getLong("sunrise") * 1000,
                        jsonObject.getJSONObject("sys").getLong("sunset") * 1000));

                NumberFormat doubleFormat = NumberFormat.getNumberInstance(Locale.getDefault());

                if(id==0){   // api calling by lat, long returns temp in kelvin
                    tempFromKtoC = main.getDouble("temp")-273;
                }else if(id==1){ //else normally in celcius
                    tempFromKtoC = main.getDouble("temp");
                }
                String currenttempText = doubleFormat.format(tempFromKtoC) + "°c";

                cityField.setText(cityFieldText);
                humidity_field.setText(humidityText);
                pressure_field.setText(pressureText);
                detailsField.setText(detailsText);
                updatedField.setText(dateText);
                weatherIcon.setText(weather_icon);
                currentTemperatureField.setText(currenttempText);
                loader.setVisibility(View.GONE);


            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
