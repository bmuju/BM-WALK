package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.service.TrackingManager

class BmWalkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getDatabase(this)
        TrackingManager.getInstance(this)
    }
}
