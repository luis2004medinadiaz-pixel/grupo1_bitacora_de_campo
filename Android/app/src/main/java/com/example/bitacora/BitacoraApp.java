package com.example.bitacora;

import android.app.Application;
import com.example.bitacora.data.local.SessionManager;

public class BitacoraApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.init(this);
    }
}
