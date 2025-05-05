package com.example.grnz

import android.app.Application
import com.example.grnz.data.network.ApiClient

class GrnzApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.initialize(this)
    }
}