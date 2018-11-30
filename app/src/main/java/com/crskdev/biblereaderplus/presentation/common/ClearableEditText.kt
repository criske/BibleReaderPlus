/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.common

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

/**
 * Created by Cristian Pela on 30.11.2018.
 */
class ClearableEditText : AppCompatEditText, View.OnTouchListener, View.OnFocusChangeListener,
    TextWatcher {

    private lateinit var clearDrawable: Drawable

    private var myFocusChangeListener: View.OnFocusChangeListener? = null

    private var myTouchListener: View.OnTouchListener? = null


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        viewInit(context)
    }


    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val x = event.x
        if (clearDrawable.isVisible && x > width - paddingRight - clearDrawable.intrinsicWidth) {
            if (event.action == MotionEvent.ACTION_UP) {
                error = null
                setText("")
            }
            return true
        }
        return myTouchListener?.onTouch(view, event) ?: false
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        setClearIconVisible(hasFocus && text?.isNotEmpty() == true)
        myFocusChangeListener?.onFocusChange(v, hasFocus);
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        if (isFocused) {
            setClearIconVisible(text.isNotEmpty());
        }
    }

    override fun setOnFocusChangeListener(onFocusChangeListener: View.OnFocusChangeListener) {
        myFocusChangeListener = onFocusChangeListener
    }

    override fun setOnTouchListener(onTouchListener: View.OnTouchListener) {
        myTouchListener = onTouchListener
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    private fun viewInit(context: Context) {
        clearDrawable = ContextCompat
            .getDrawable(context, android.R.drawable.ic_menu_close_clear_cancel)
                ?: throw Resources.NotFoundException("Resource not found")
        DrawableCompat.setTint(clearDrawable, currentHintTextColor)
        super.setOnFocusChangeListener(this)
        super.setOnTouchListener(this)
        addTextChangedListener(this)
    }

    private fun setClearIconVisible(visible: Boolean) {
        clearDrawable.setVisible(visible, false)
        compoundDrawables.apply {
            setCompoundDrawables(
                this[0],
                this[1],
                if (visible) clearDrawable else null,
                this[3]
            )
        }
    }
}