package com.example.termuxrunnerkt

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import com.example.termuxrunnerkt.databinding.DialogFreeScrollBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tencent.mmkv.MMKV
import androidx.core.net.toUri

val app = App.getInstance()

val mmkv = MMKV.defaultMMKV()


fun Any?.debug(tag: String="aaa"){
	Log.d(tag, "$this")
}

fun Any?.toast(){
	Toast.makeText(app, "$this", Toast.LENGTH_SHORT).show()
}

fun Any.alert(context: Context){
	val b = DialogFreeScrollBinding.inflate(LayoutInflater.from(context),null,false)
	b.msg.text = "$this"
	MaterialAlertDialogBuilder(context).setView(b.root).show()
}

fun Context.launchApp(packageName: String) {
	val pm = packageManager
	val launchIntent = pm.getLaunchIntentForPackage(packageName)
	if (launchIntent != null) {
		startActivity(launchIntent)
	} else {
		// App 未安装，可以跳转到应用商店
		"App 未安装".toast()
	}
}