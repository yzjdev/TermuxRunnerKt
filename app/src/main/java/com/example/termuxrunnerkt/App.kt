package com.example.termuxrunnerkt

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.tencent.mmkv.MMKV

class App  : Application(){
	companion object {
		private lateinit var instance: App

		fun getInstance(): App {
			return instance
		}
	}

	override fun onCreate() {
		super.onCreate()
		instance = this
		DynamicColors.applyToActivitiesIfAvailable(this)
		MMKV.initialize(this)
	}



}