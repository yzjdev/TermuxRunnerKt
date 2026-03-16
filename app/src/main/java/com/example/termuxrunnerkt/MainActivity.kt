package com.example.termuxrunnerkt

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.example.termuxrunnerkt.databinding.ActivityMainBinding
import com.example.termuxrunnerkt.ext.appendClickable
import com.example.termuxrunnerkt.ext.appendText
import com.example.termuxrunnerkt.ext.setSpannableText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.termux.shared.termux.TermuxConstants
import com.termux.shared.termux.TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding
	val TAG = "aaa"

	private var backgroundCheckedId = R.id.backgroundButton1
	private var sessionActionCheckedId = R.id.button1

	private var loadingDialog: AlertDialog? = null


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}


		backgroundCheckedId = mmkv.getInt("background_group_checked_id", R.id.backgroundButton1)
		sessionActionCheckedId = mmkv.getInt("session_action_group_checked_id", R.id.button1)

		binding.backgroundGroup.apply {
			if (checkedButtonId != backgroundCheckedId) check(backgroundCheckedId)
			addOnButtonCheckedListener { group, checkedId, isChecked ->

				if (isChecked) {
					backgroundCheckedId = checkedId
					mmkv.putInt("background_group_checked_id", checkedId)
				}
			}
		}

		binding.sessionActionGroup.apply {
			if (checkedButtonId != sessionActionCheckedId) check(sessionActionCheckedId)
			addOnButtonCheckedListener { group, checkedId, isChecked ->
				if (isChecked) {
					sessionActionCheckedId = checkedId
					mmkv.putInt("session_action_group_checked_id", checkedId)
				}
			}

		}

		binding.exec.setOnClickListener {
			val permission = "com.termux.permission.RUN_COMMAND"
			val has = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
			if (!has) {
				requestPermissions(arrayOf(permission), 1001)
				return@setOnClickListener
			}
			exec()
		}

		binding.backgroundHelp.setSpannableText {

			appendText("Background:  ")

			appendClickable("Help") {
				"后台运行: app-shell\n前台运行: terminal-session".alert(this@MainActivity)
			}
		}

		binding.sessionActionHelp.setSpannableText {
			appendText("Session Action: ")
			appendClickable("Help") {
				"app-shell时不会启动TermuxActivity\n\n0: 启动TermuxActivity,新会话设为当前\n1: 启动TermuxActivity,保留当前会话,添加新会话\n2: 不启动TermuxActivity,新会话设为当前\n3: 不启动TermuxActivity,保留当前会话\n\n不推荐1 和 3".alert(this@MainActivity)
			}
		}

		binding.cmdPath.editText?.apply {
			setText(mmkv.getString("cmd_path", text.toString()))
			setSelection(text.length)
			doAfterTextChanged {
				if (!TextUtils.isEmpty(text))mmkv.putString("cmd_path", text.toString())
			}
		}
		binding.argsString.editText?.apply {
			setText(mmkv.getString("args_string", text.toString()))
			setSelection(text.length)
			doAfterTextChanged {
				if (!TextUtils.isEmpty(text))mmkv.putString("args_string", text.toString())
			}
		}
		binding.workdir.editText?.apply {
			setText(mmkv.getString("workdir", text.toString()))
			setSelection(text.length)
			doAfterTextChanged {
				if (!TextUtils.isEmpty(text))mmkv.putString("workdir", text.toString())
			}
		}


	}


	fun showLoading() {
		if (loadingDialog == null) {
			val builder = MaterialAlertDialogBuilder(this, R.style.LoadingDialogTheme)
				.setView(R.layout.dialog_loading)
				.setCancelable(false)
			loadingDialog = builder.create()
		}

		loadingDialog?.show()

	}

	fun hideLoading() {
		loadingDialog?.dismiss()

	}


	val resultsReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {

			hideLoading()
			if (intent == null) return

			val stdout = intent.getStringExtra(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT)
			val stderr = intent.getStringExtra(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR)
			val exitCode = intent.getIntExtra(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE, 0)
			val errCode = intent.getIntExtra(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_ERR, 0)
			val errMsg = intent.getStringExtra(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_ERRMSG)


			val builder = SpannableStringBuilder()

			// Exit Code
			builder.append("exit code: $exitCode\n\n")

			// stdout
			stdout?.takeIf { it.isNotEmpty() }?.let {
				builder.append("$it\n\n")
			}

			// stderr 高亮红色
			stderr?.takeIf { it.isNotEmpty() }?.let {
				builder.append("$it\n\n")
			}

			// errmsg
			errMsg?.takeIf { it.isNotEmpty() }?.let {
				builder.append("$it\n")
			}

			builder.trim().alert(this@MainActivity)

		}
	}

	fun registerResultsReceiver() {
		val filter = IntentFilter(PluginResultsService.ACTION_RESULTS_RECEIVED)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			registerReceiver(resultsReceiver, filter, RECEIVER_NOT_EXPORTED)
		} else {
			registerReceiver(resultsReceiver, filter)
		}
	}

	fun unregisterResultsReceiver() {
		runCatching {
			unregisterReceiver(resultsReceiver)
		}
	}

	private var isVisible = false

	override fun onStart() {
		super.onStart()
		isVisible = true
		registerResultsReceiver()
	}



	override fun onStop() {
		super.onStop()
		isVisible = false // Activity 不可见
	}

	fun isActivityVisible(): Boolean {
		return isVisible
	}


	override fun onDestroy() {
		super.onDestroy()
		unregisterResultsReceiver()
		hideLoading()
		loadingDialog = null
	}

	fun exec() {
		showLoading()
		val commandPath = binding.cmdPath.editText?.text.toString()
		val args = binding.argsString.editText?.text.toString().split(",")
		val workdir = binding.workdir.editText?.text.toString()
		val label = "Label"
		val desc = "Desc"

		val intent = Intent().apply {
			setClassName(TermuxConstants.TERMUX_PACKAGE_NAME, TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE_NAME)
			setAction(RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND)
			putExtra(RUN_COMMAND_SERVICE.EXTRA_COMMAND_PATH, commandPath)
			putExtra(RUN_COMMAND_SERVICE.EXTRA_ARGUMENTS, args.toTypedArray())
			putExtra(RUN_COMMAND_SERVICE.EXTRA_WORKDIR, workdir)

			// app-shell 后台运行
			// terminal-session 前台运行
			putExtra(RUN_COMMAND_SERVICE.EXTRA_RUNNER, findViewById<Button>(backgroundCheckedId).text.toString())

			// 0 新会话设为当前,启动TermuxActivity
			// 1 保留当前会话,添加新会话,启动TermuxActivity
			// 2 新会话设为当前,不启动TermuxActivity
			// 3 保留当前会话,不启动TermuxActivity
			putExtra(RUN_COMMAND_SERVICE.EXTRA_SESSION_ACTION, findViewById<Button>(sessionActionCheckedId).text.toString())
			putExtra(RUN_COMMAND_SERVICE.EXTRA_COMMAND_LABEL, label)
			putExtra(RUN_COMMAND_SERVICE.EXTRA_COMMAND_DESCRIPTION, desc)
		}

		val executionId = PluginResultsService.getNextExecutionId()

		val pluginResultsServiceIntent = Intent(this, PluginResultsService::class.java).apply {
			putExtra(PluginResultsService.EXTRA_EXECUTION_ID, executionId)
		}

		val flags = PendingIntent.FLAG_ONE_SHOT or
				(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
		val pendingIntent = PendingIntent.getService(this, executionId, pluginResultsServiceIntent, flags)
		intent.putExtra(RUN_COMMAND_SERVICE.EXTRA_PENDING_INTENT, pendingIntent)

		val componentName = startForegroundService(intent)

		if (componentName == null){
			hideLoading()
			"Termux服务启动失败".toast()
		}
	}
}