package com.example.termuxrunnerkt

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.termux.shared.termux.TermuxConstants

class PluginResultsService : Service() {
	companion object {
		const val ACTION_RESULTS_RECEIVED = "com.example.termuxrunnerkt.RESULTS_RECEIVED"
		const val EXTRA_EXECUTION_ID = "execution_id"
		var EXECUTION_ID = 1000;
		const val PLUGIN_SERVICE_LABEL = "PluginResultsService"

		const val EXTRA_STDOUT = "stdout"
		const val EXTRA_STDERR = "stderr"
		const val EXTRA_EXIT_CODE = "exit_code"
		const val EXTRA_ERR_CODE = "err_code"
		const val EXTRA_ERR_MSG = "err_msg"


		@Synchronized
		fun getNextExecutionId(): Int {
			return EXECUTION_ID++
		}

	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		handleIntent(intent)
		stopSelf(startId)
		return START_STICKY
	}

	fun handleIntent(intent: Intent?) {
		intent ?: return

		val resultBundle = intent.getBundleExtra(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE) ?: return

		val executionId = intent.getIntExtra(EXTRA_EXECUTION_ID, 0)

		val stdout = resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT, "")
		val stdout_original_length = resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT_ORIGINAL_LENGTH)
		val stderr = resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR, "")
		val stderr_original_length = resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR_ORIGINAL_LENGTH)
		val exitCode = resultBundle.getInt(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE)
		val errCode = resultBundle.getInt(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_ERR)
		val errMsg = resultBundle.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_ERRMSG, "")

		sendBroadcast(Intent(ACTION_RESULTS_RECEIVED).apply {
			putExtra(EXTRA_STDOUT, stdout)
			putExtra(EXTRA_STDERR, stderr)
			putExtra(EXTRA_EXIT_CODE, exitCode)
			putExtra(EXTRA_ERR_CODE, errCode)
			putExtra(EXTRA_ERR_MSG, errMsg)
			setPackage(packageName)
		})

	}

	override fun onBind(p0: Intent?): IBinder? {
		return null
	}
}