/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */
package com.crskdev.biblereaderplus.presentation

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import com.crskdev.biblereaderplus.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : DaggerAppCompatActivity() {
    private val PEOPLE = arrayOf("John Smith", "Kate Eckhart", "Emily Sun", "Frodo Baggins")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line, PEOPLE
        )


        autoCompleteTextView.setAdapter<ArrayAdapter<String>>(adapter)
        autoCompleteTextView.setOnItemClickListener { parent, arg1, position, arg3 ->
            autoCompleteTextView.text = null
            val selected = parent.getItemAtPosition(position) as String
            addChipToGroup(selected, chipGroupSuggestion)
        }

        autoCompleteTextView.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SPACE) {
                autoCompleteTextView.text = null
                true
            } else {
                false
            }

        }
    }

    private fun addChipToGroup(person: String, chipGroup: ChipGroup) {
        val chip = Chip(this)
        chip.text = person
        chip.isCloseIconEnabled = true
        chip.setChipIconTintResource(R.color.secondaryColor)

        // necessary to get single selection working
        chip.isClickable = true
        chip.isCheckable = false
        chipGroup.addView(chip as View)
        chip.setOnCloseIconClickListener { chipGroup.removeView(chip as View) }
    }

}
