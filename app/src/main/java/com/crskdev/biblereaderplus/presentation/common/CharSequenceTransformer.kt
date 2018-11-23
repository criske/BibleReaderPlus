/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.common

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.LeadingMarginSpan
import android.text.style.StyleSpan
import androidx.annotation.DrawableRes
import com.crskdev.biblereaderplus.common.util.castOrNull
import com.crskdev.biblereaderplus.presentation.common.CharSequenceTransformer.Companion.ensureSpannable
import java.util.*


/**
 * Created by Cristian Pela on 23.11.2018.
 */
interface CharSequenceTransformer {

    fun transform(other: CharSequence, args: Args? = null): CharSequence

    interface Args

    companion object {
        internal fun ensureSpannable(other: CharSequence): SpannableStringBuilder =
            if (other !is StringBuilder) {
                SpannableStringBuilder(other)
            } else {
                other as SpannableStringBuilder
            }

        internal inline fun ensureSpannable(other: CharSequence, block: SpannableStringBuilder.() -> Unit): SpannableStringBuilder =
            ensureSpannable(other).apply(block)
    }
}

class CharSequenceTransformerFactory(

    private val transformers: EnumMap<CharSequenceTransformerFactory.Type, Lazy<CharSequenceTransformer>>) {

    fun transform(type: Type, other: CharSequence, args: CharSequenceTransformer.Args? = null): CharSequence =
        transformers[type]?.value?.transform(other, args)
            ?: throw ClassNotFoundException("Transformer Not Found!!")


    fun startChain(content: CharSequence): Chain =
        Chain(this, ensureSpannable(content))

    enum class Type {
        ICON_AT_END, LEAD_FIRST_LINE, HIGHLIGHT
    }

    class Chain internal constructor(
        private val factory: CharSequenceTransformerFactory,
        val content: CharSequence) {

        fun transform(type: Type, args: CharSequenceTransformer.Args? = null): Chain =
            Chain(factory, factory.transform(type, content, args))

    }

}

//TODO implement args for each transformer

class IconAtEndTransformer(private val context: Context, @DrawableRes private val res: Int) :
    CharSequenceTransformer {

    override fun transform(other: CharSequence, args: CharSequenceTransformer.Args?): CharSequence =
        ensureSpannable(other) {
            val start = length
            append(" ")
            setSpan(ImageSpan(context, res), start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
}

class LeadFirstLineTransformer : CharSequenceTransformer {

    override fun transform(other: CharSequence, args: CharSequenceTransformer.Args?): CharSequence =
        ensureSpannable(other) {
            setSpan(LeadingMarginSpan.Standard(40, 0), 0, length, 0)
        }

}

class HighLightContentTransformer : CharSequenceTransformer {

    class HighlightArg(val what: String, val ignoreCase: Boolean = false, val highlightAll: Boolean = true) :
        CharSequenceTransformer.Args

    override fun transform(other: CharSequence, args: CharSequenceTransformer.Args?): CharSequence =
        ensureSpannable(other) {
            args?.castOrNull<HighlightArg>()?.let {
                var start = other.indexOf(it.what, ignoreCase = it.ignoreCase)
                while (start != -1) {
                    val end = start + it.what.length
                    setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    if (!it.highlightAll) {
                        break
                    }
                    start = other.indexOf(it.what, end, ignoreCase = it.ignoreCase)
                }
            }
        }
}

