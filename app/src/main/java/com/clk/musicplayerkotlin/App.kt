package com.clk.musicplayerkotlin

import android.app.Application

class App : Application() {
    lateinit var storage: Storage
    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        storage = Storage()
    }
}