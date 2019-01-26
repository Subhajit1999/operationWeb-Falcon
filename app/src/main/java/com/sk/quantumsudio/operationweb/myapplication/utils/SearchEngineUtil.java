package com.sk.quantumsudio.operationweb.myapplication.utils;

import android.content.Context;
import android.util.Log;

public class SearchEngineUtil {
    private static final String TAG = "SearchEngineUtil";

    public static int searchEngineId;
    //string array of search engine urls
    private static String[] url = new String[3];

    public static String setDefaultSearchEngine(String inputValue,Context context){
        Log.d(TAG, "setDefaultSearchEngine: setting default search engine");

        url[0] = "https://www.google.com/search?source=hp&ei=V643XLb2N5ioyAOipYmQDQ&q="+   //search engine urls array
                inputValue+"&btnK=Google+Search&oq="+inputValue+
                "&gs_l=psy-ab.3..0l8j0i131j0.13717.15912..19503...3.0..1.349.2533.0j1j9j1.." +
                "....0....1..gws-wiz.....6..35i39.DZuILZd7BR0";
        url[1] = "https://www.contextualwebsearch.com/search/"+inputValue;
        url[2] = "https://duckduckgo.com/?q="+inputValue+"&t=hp&ia=web";

        return url[searchEngineId];
    }
}
