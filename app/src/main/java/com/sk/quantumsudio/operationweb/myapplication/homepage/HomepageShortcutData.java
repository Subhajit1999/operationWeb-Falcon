package com.sk.quantumsudio.operationweb.myapplication.homepage;

 class HomepageShortcutData {
    private static final String TAG = "HomepageShortcutData";

    private String shortcutName;
    private int shortcutImage;

     HomepageShortcutData(String shortcutName,int shortcutImage){
        this.shortcutName = shortcutName;
        this.shortcutImage = shortcutImage;
    }
     String getShortcutName(){
        return shortcutName;
    }
     int getShortcutImage(){
        return shortcutImage;
    }
}
