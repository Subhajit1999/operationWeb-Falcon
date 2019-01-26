package com.sk.quantumsudio.operationweb.myapplication.homepage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.sk.quantumsudio.operationweb.myapplication.R;
import com.sk.quantumsudio.operationweb.myapplication.autoSuggestion.ApiCall;
import com.sk.quantumsudio.operationweb.myapplication.autoSuggestion.AutoSuggestAdapter;
import com.sk.quantumsudio.operationweb.myapplication.gps.AppLocationService;
import com.sk.quantumsudio.operationweb.myapplication.utils.SearchEngineUtil;
import com.sk.quantumsudio.operationweb.myapplication.webpage.WebPageActivity;
import com.sk.quantumsudio.operationweb.myapplication.utils.Function;
import com.sk.quantumsudio.operationweb.myapplication.utils.KeyboardUtils;
import com.sk.quantumsudio.operationweb.myapplication.weather.WeatherActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private static final int TRIGGER_AUTO_COMPLETE = 100;   //for AutoSuggestion
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;

    Toolbar home_toolbar;
    TextView tv_appname;
    AppCompatAutoCompleteTextView autoCompleteSearchInput;
    ImageView iv_enter;
    RecyclerView mRecyclerView;
    String input_url,finalUrl;
    //for weather layout part
    LinearLayout weatherLayout;
    Typeface weatherFont;
    TextView temp,location,date,weatherIcon,weather_details;
    Location gpsLocation;
    static Double latitude, longitude;
    SharedPreferences preferences;

    AppLocationService appLocationService;  //for accessing gps data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Inside homeActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //recovers the saved data immediately when HomeActivity is visible
        preferences = getSharedPreferences("FalconB",Context.MODE_PRIVATE);
        SearchEngineUtil.searchEngineId = preferences.getInt("SearchEngineId",0);
        appLocationService = new AppLocationService(getApplicationContext());

        //widgets initialization
        tv_appname = findViewById(R.id.tv_appname);
        autoCompleteSearchInput = findViewById(R.id.input_search);
        iv_enter = findViewById(R.id.iv_enter);
        home_toolbar = findViewById(R.id.toolbar_homepage);
        temp = findViewById(R.id.temperature);         //for weather layout
        location = findViewById(R.id.location);
        date = findViewById(R.id.time);
        weatherIcon = findViewById(R.id.weather_icon);
        weather_details = findViewById(R.id.weather_deatils);
        weatherLayout = findViewById(R.id.weather_layout);
        home_toolbar.inflateMenu(R.menu.homepage_menu); //setting up the menu with the toolbar

        weatherFont = Typeface.createFromAsset(getAssets(),"fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);//implementaing weather icons as font

        shortcutSetup(); //for clearing things up
        searchProcess(); //handles search events for keyboard and in-display enter button
        getLocation(); //For getting the latitude and longitude from system gps
        autoSuggestion(); //for setting up the autoSuggestion feature

        weatherLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //navigates to the detailed WeatherActivity
                Intent i = new Intent(getApplicationContext(),WeatherActivity.class);
                Bundle bundle = new Bundle();
                bundle.putDouble("lat",latitude);
                bundle.putDouble("long",longitude);
                i.putExtras(bundle);
                startActivity(i);
            }
        });
    }
    public void shortcutSetup(){

        mRecyclerView = findViewById(R.id.recyclerview);  //initializing and setting up recyclerView as GridView
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(HomeActivity.this, 4);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        List<HomepageShortcutData> homepageShortcutData = new ArrayList<>(); //list of items title and images
        homepageShortcutData.add(new HomepageShortcutData("Google",R.drawable.google));  //position 0
        homepageShortcutData.add(new HomepageShortcutData("Facebook",R.drawable.facebook));  //position 1
        homepageShortcutData.add(new HomepageShortcutData("YouTube",R.drawable.youtube));  //position 2
        homepageShortcutData.add(new HomepageShortcutData("Twitter",R.drawable.twitter));  //position 3
        homepageShortcutData.add(new HomepageShortcutData("Instagram",R.drawable.instagram));  //position 4
        homepageShortcutData.add(new HomepageShortcutData("Amazon",R.drawable.amazon));  //position 5
        homepageShortcutData.add(new HomepageShortcutData("Flipkart",R.drawable.flipkart));  //position 6
        homepageShortcutData.add(new HomepageShortcutData("ebay",R.drawable.ebay));  //position 7
        homepageShortcutData.add(new HomepageShortcutData("Snapdeal",R.drawable.snapdeal));  //position 8
        homepageShortcutData.add(new HomepageShortcutData("Cricbuzz",R.drawable.cricbuzz));  //position 9
        homepageShortcutData.add(new HomepageShortcutData("Zomato",R.drawable.zomato));  //position 10
        homepageShortcutData.add(new HomepageShortcutData("Uber",R.drawable.uber));  //position 11


        ShortcutAdapter adapter = new ShortcutAdapter(HomeActivity.this,homepageShortcutData);
        mRecyclerView.setAdapter(adapter); //setting everything wth adapter
    }

    public void searchProcess(){
        Log.d(TAG, "searchProcess: search process function");
        iv_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchIntent();  //calls function for passing url to webview
                KeyboardUtils.hideKeyboard(getApplicationContext(),getCurrentFocus());
            }
        });
        autoCompleteSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtils.hideKeyboard(getApplicationContext(),getCurrentFocus());

                    searchIntent();  //calls function for passing url to webview
                    handled = true;
                }
                return handled;
            }
        });
        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener()
        {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible)
            {   //will check if keyboard visible or not by accessing custom KeyboardUtils class
                if(!isVisible) {
                    //when keyboard is hidden
                    autoCompleteSearchInput.clearFocus();
                }
            }
        });
    }
    private void searchIntent(){
        Log.d(TAG, "searchIntent: searchIntent click action");
        input_url = autoCompleteSearchInput.getText().toString();
        autoCompleteSearchInput.setText("");  //makes it blank after copying the user input data
        if(input_url.matches("")){
            Toast.makeText(getApplicationContext(),"Please enter something to search",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getApplicationContext(),WebPageActivity.class);
        if(URLUtil.isNetworkUrl(input_url)){
            intent.putExtra("url",input_url);
        }else{
            // sends search value to the search engine
            input_url = input_url.replace(" ","+");
            finalUrl = SearchEngineUtil.setDefaultSearchEngine(input_url,getApplicationContext());
            intent.putExtra("url",finalUrl);
        }
        intent.putExtra("activityId",1);
        startActivity(intent);
    }
    void taskLoadUp(){
        Log.d(TAG, "taskLoadUp: Weather task loading");
        if(Function.isNetworkAvailable(getApplicationContext())){
            DownloadWeather task = new DownloadWeather();
            task.execute();
        }else{
            Toast.makeText(getApplicationContext(),"No active Internet Connection",Toast.LENGTH_SHORT).show();
        }
    }

    public class DownloadWeather extends AsyncTask< String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected String doInBackground(String...args) {
            String xml="";
            xml = Function.excuteGet("http://api.openweathermap.org/data/2.5/weather?" + "lat=" + latitude+ "&lon="
                    + longitude + "&appid=" +WeatherActivity.OPEN_WEATHER_MAP_API);
            return xml;
        }
        @Override
        protected void onPostExecute(String xml) {
            Log.d(TAG, "onPostExecute: getting weather data");

            try {
                JSONObject jsonObject = new JSONObject(xml);
                JSONObject details = jsonObject.getJSONArray("weather").getJSONObject(0);
                JSONObject main = jsonObject.getJSONObject("main");
                DateFormat df = DateFormat.getDateTimeInstance();

                String cityFieldText = jsonObject.getString("name").toUpperCase(Locale.US) + ", " + jsonObject.getJSONObject("sys").getString("country");
                String detailsText = details.getString("description").toUpperCase(Locale.US);
                String dateText = df.format(new Date(jsonObject.getLong("dt") * 1000));
                Spanned weather_icon = Html.fromHtml(Function.setWeatherIcon(details.getInt("id"),
                        jsonObject.getJSONObject("sys").getLong("sunrise") * 1000,
                        jsonObject.getJSONObject("sys").getLong("sunset") * 1000));

                NumberFormat numFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                double tempDouble = main.getDouble("temp")-273;   //converting temp from kelvin to celcius
                int tempFromKtoC = (int) tempDouble;
                String currenttempText = numFormat.format(tempFromKtoC) + "°c";

                location.setText(cityFieldText);
                weather_details.setText(detailsText);
                date.setText(dateText);
                weatherIcon.setText(weather_icon);
                temp.setText(currenttempText);

            } catch (JSONException e) {
                location.setText(getString(R.string.error_location));
            }
        }
    }
    public void showSettingsAlert(){
        Log.d(TAG, "showSettingsAlert:");
        //for showing location access settings dialog
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Location Service is turned off, please turn it on to get the weather " +
                "forecast in your location. Proceed to Android Settings menu?");

        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  // go to settings button
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getApplicationContext().startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {   //cancel button
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }
    public void getLocation(){
        Log.d(TAG, "getLocation: getting lat,long from gps");
        gpsLocation = appLocationService
                .getLocation(LocationManager.GPS_PROVIDER,this);
        if (gpsLocation != null) {
            if(Function.isNetworkAvailable(getApplicationContext())){
                latitude = gpsLocation.getLatitude();
                longitude = gpsLocation.getLongitude();
            }else{
                weatherLayout.setVisibility(View.GONE);
            }
            taskLoadUp();
        } else {
            showSettingsAlert();
        }
    }

    private void makeApiCall(String text) {   //actual API call function for AutoSuggestion
        Log.d(TAG, "makeApiCall: calling autosuggest api");
        ApiCall.make(this, text, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                List<String> stringList = new ArrayList<>();
                try {
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray array = responseObject.getJSONArray("results");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject row = array.getJSONObject(i);
                        stringList.add(row.getString("trackName"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //sets data here and notify
                autoSuggestAdapter.setData(stringList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }
    private void autoSuggestion(){
        Log.d(TAG, "autoSuggestion: autosuggestion behaviour handling");
        //setting up adapter for AutoSuggestion
        autoSuggestAdapter = new AutoSuggestAdapter(this,android.R.layout.simple_dropdown_item_1line);
        autoCompleteSearchInput.setThreshold(2);
        autoCompleteSearchInput.setAdapter(autoSuggestAdapter);
        autoCompleteSearchInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Item click functionality
                autoCompleteSearchInput.setText(autoSuggestAdapter.getObject(position));
            }
        });
        autoCompleteSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(autoCompleteSearchInput.getText())) {
                        makeApiCall(autoCompleteSearchInput.getText().toString());
                    }
                }
                return false;
            }
        });
    }
}
