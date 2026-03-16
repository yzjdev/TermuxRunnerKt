package com.example.termuxrunnerkt.ext

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView

/**
 * TextView Spannable 构建扩展
 */
fun TextView.setSpannableText(block: SpannableStringBuilder.() -> Unit) {

	val builder = SpannableStringBuilder()
	builder.block()

	text = builder
	movementMethod = LinkMovementMethod.getInstance()
	highlightColor = Color.TRANSPARENT
}

/**
 * 添加普通文本
 */
fun SpannableStringBuilder.appendText(text: String) {
	append(text)
}

/**
 * 添加可点击文本
 */
fun SpannableStringBuilder.appendClickable(
	text: String,
	color: Int = Color.parseColor("#3F51B5"),
	underline: Boolean = true,
	click: () -> Unit
) {

	val start = length
	append(text)
	val end = length

	setSpan(object : ClickableSpan() {

		override fun onClick(widget: View) {
			click()
		}

		override fun updateDrawState(ds: TextPaint) {
			ds.color = color
			ds.isUnderlineText = underline
		}

	}, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}

/**
 * 添加粗体文本
 */
fun SpannableStringBuilder.appendBold(text: String) {

	val start = length
	append(text)
	val end = length

	setSpan(
		StyleSpan(Typeface.BOLD),
		start,
		end,
		Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
	)
}