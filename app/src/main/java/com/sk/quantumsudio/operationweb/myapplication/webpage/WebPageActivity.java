package com.sk.quantumsudio.operationweb.myapplication.webpage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sk.quantumsudio.operationweb.myapplication.R;
import com.sk.quantumsudio.operationweb.myapplication.homepage.HomeActivity;
import com.sk.quantumsudio.operationweb.myapplication.utils.Function;
import com.sk.quantumsudio.operationweb.myapplication.utils.KeyboardUtils;
import com.sk.quantumsudio.operationweb.myapplication.utils.SearchEngineUtil;

import java.util.ArrayList;
import java.util.Objects;

public class WebPageActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "WebPageActivity";
    public static String urlFromWebview="",inputValue="";
    static int[] searchEngineImage = {    //search engine images
            R.drawable.google,R.drawable.contextual,R.drawable.duckduckgo
    };

    WebView webView;
    SwipeRefreshLayout swipe;
    EditText et_searchField;
    Toolbar toolbar;
    FrameLayout frame_loadingbar;
    ProgressBar loadingbar;
    ImageView iv_action,iv_back,searchEngine;
    int status,activityId;
    String URL = "";  //final url that to be loaded
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: onCreate of"+TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webpage);

        webView = (WebView) findViewById(R.id.webView);  //widgets initialization
        iv_action = findViewById(R.id.iv_refresh);
        frame_loadingbar = findViewById(R.id.frame_loadingbar);
        loadingbar = findViewById(R.id.loadingbar);
        toolbar = findViewById(R.id.toolbar_webpage);
        iv_back = findViewById(R.id.iv_button_back);
        searchEngine = findViewById(R.id.iv_searchEngine);
        swipe = findViewById(R.id.swipe_refresh);
        swipe.setOnRefreshListener(this);   //connects OnRefreshListener with the swipeLayout

        Intent intent = getIntent();
        activityId = intent.getIntExtra("activityId",3);

        if (activityId==1) {
            URL = intent.getStringExtra("url");  //receiving the url from homepage search field
        }else if(activityId==2){
            URL = intent.getStringExtra("ItemUrl");  //receiving the url from shortcut cards
        }else{
            Toast.makeText(getApplicationContext(),"Unexpected error occured...",
                    Toast.LENGTH_SHORT).show();
        }//sets the saved default search engine image
        searchEngine.setImageResource(searchEngineImage[SearchEngineUtil.searchEngineId]);

        urlWebLoadingProcess(); // web url loading process
        allEditTextEvents(); //all the editText events
        setSearchEngine();

        iv_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(status){
                    case 0: // cross image
                        break;
                    case 1: // refresh image
                        swipe.setRefreshing(true); //refresh UI
                        URL = urlFromWebview;
                        urlWebLoadingProcess(); //reloads the url
                        break;
                }
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //goes back to the homeactivity
                Intent i = new Intent(getApplicationContext(),HomeActivity.class);
                startActivity(i);
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //handling system back button functions
        Log.d(TAG, "onKeyDown: SystembackButtonHandler of"+TAG);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRefresh() {  //swipe to refresh function implementation
        if(!swipe.canChildScrollUp()){
            swipe.setRefreshing(true); //refresh UI
            URL = urlFromWebview;
            urlWebLoadingProcess(); //reloads the url
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (Objects.equals(Uri.parse(URL).getHost(), URL)) {
                //Open url contents in Webview
                frame_loadingbar.setVisibility(View.VISIBLE);
                return false;
            }
            return super.shouldOverrideUrlLoading(view,request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            urlFromWebview = url;
            et_searchField.setText(url);  //sets the url wherever the current page is...
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            swipe.setRefreshing(false);
            super.onPageFinished(view, url);
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    public void urlWebLoadingProcess(){
        webView.getSettings().setBuiltInZoomControls(true);   //setting up webView properties
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setJavaScriptEnabled(true);  //in case of any error
        webView.setWebViewClient(new MyWebViewClient());

        if(Function.isNetworkAvailable(getApplicationContext())){  //checks network state availability
            webView.loadUrl(URL);
        }else{
            Toast.makeText(getApplicationContext(),"No active internet connnection",
                    Toast.LENGTH_SHORT).show();
        }
        webView.setWebChromeClient(new WebChromeClient() {  //setting loading bar status
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                frame_loadingbar.setVisibility(View.VISIBLE);
                loadingbar.setProgress(newProgress);
                //sets the url color temporarily when loading
                et_searchField.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.violet));
                status = 0;  //when image is cross
                iv_action.setImageResource(R.drawable.ic_cross);  //sets the action button image to cross

                // disappears loading bar when finish loading
                if (newProgress == 100){
                    //recovers the url color after page is loaded
                    et_searchField.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.dark_grey));
                    frame_loadingbar.setVisibility(View.GONE);
                    status = 1;  //when image is refresh
                    iv_action.setImageResource(R.drawable.ic_refresh);  //changes the action button image to refresh
                }
                super.onProgressChanged(view,newProgress);
            }
        });
    }
    public void allEditTextEvents(){
        Log.d(TAG, "editTextEvents: accessing editText events functions");
        et_searchField = findViewById(R.id.input_search);
        et_searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    inputValue = et_searchField.getText().toString(); //gets the value from the edittext

                    if(URLUtil.isNetworkUrl(inputValue)){  //validates if inputValue is url or not
                        URL = inputValue; //sets the value to the URL
                        urlWebLoadingProcess(); //calls for starting web loading process
                    }else{
                        //pass the input value to the search engine API
                        inputValue = inputValue.replace(" ","+");
                        //returns the final search engine url
                        URL = SearchEngineUtil.setDefaultSearchEngine(inputValue,getApplicationContext());
                        urlWebLoadingProcess();
                    }
                    KeyboardUtils.hideKeyboard(getApplicationContext(),getCurrentFocus());
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
                    et_searchField.setText(urlFromWebview);
                    et_searchField.clearFocus();
                }
            }
        });
    }
    public void setSearchEngine(){
        Log.d(TAG, "setSearchEngine: Search engine settings");
        searchEngine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(WebPageActivity.this);
                builderSingle.setTitle("Default Search Engine");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(WebPageActivity.this, android.R.layout.simple_list_item_1);
                arrayAdapter.add("Google");             //search engine items
                arrayAdapter.add("ContextualWeb");
                arrayAdapter.add("Duckduckgo");

                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case 0:    //google
                                SearchEngineUtil.searchEngineId = 0;
                                searchEngine.setImageResource(R.drawable.google);
                                break;
                            case 1:   //contextual web
                                SearchEngineUtil.searchEngineId = 1;
                                searchEngine.setImageResource(R.drawable.contextual);
                                break;
                            case 2:   //duckduckgo
                                SearchEngineUtil.searchEngineId = 2;
                                searchEngine.setImageResource(R.drawable.duckduckgo);
                                break;
                        }
                    }
                });
                builderSingle.show();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        //saves the last searchEngineid data before going to paused state
        mPreferences = getSharedPreferences("FalconB",Context.MODE_PRIVATE);
        final SharedPreferences.Editor spEditor = mPreferences.edit();
        spEditor.putInt("SearchEngineId",SearchEngineUtil.searchEngineId);
        spEditor.commit();
    }
}
