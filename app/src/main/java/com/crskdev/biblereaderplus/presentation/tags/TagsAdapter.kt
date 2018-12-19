/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.tags

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.forEach
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.presentation.util.system.getColorCompat
import com.crskdev.biblereaderplus.presentation.util.system.showSimpleInputDialog
import com.crskdev.biblereaderplus.presentation.util.view.BindableViewHolder
import com.crskdev.biblereaderplus.presentation.util.view.ColorUtilsExtra
import com.google.android.material.chip.Chip

/**
 * Created by Cristian Pela on 29.11.2018.
 */
class TagsAdapter(private val inflater: LayoutInflater,
                  private val tagBehaviour: TagBehaviour = TagBehaviour(),
                  private val action: (Tag, TagSelectAction) -> Unit) :

    ListAdapter<Tag, TagVH>(object : DiffUtil.ItemCallback<Tag>() {
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean = oldItem == newItem
    }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagVH =
        TagVH(
            inflater.inflate(
                R.layout.item_tag,
                parent,
                false
            ), tagBehaviour, action
        )

    override fun onBindViewHolder(holder: TagVH, position: Int) =
        holder.bind(getItem(position))

}

class TagBehaviour(val selectPolicy: SelectPolicy = SelectPolicy.TAP_DISABLED, val isClosable: Boolean = false) {

    enum class SelectPolicy {
        TAP_DISABLED,
        SELECT_ON_TAP,
        CONTEXT_MENU_ON_TAP,
        SELECT_ON_TAP_CONTENT_MENU_ON_LONG_TAP,
        CONTEXT_MENU_ON_TAP_SELECT_ON_LONG_TAP
    }
}

enum class TagSelectAction {
    CLOSE, CONTEXT_MENU_RENAME, CONTEXT_MENU_REMOVE, CONTEXT_MENU_CHANGE_COLOR, SELECT
}

class TagVH(v: View, private val tagBehaviour: TagBehaviour, private val action: (Tag, TagSelectAction) -> Unit) :
    BindableViewHolder<Tag>(v) {

    init {
        with(itemView.cast<Chip>()) {

            if (tagBehaviour.isClosable) {
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    model?.let {
                        action(
                            it,
                            TagSelectAction.CLOSE
                        )
                    }
                }
            }

            setOnCreateContextMenuListener { menu, view, info ->
                MenuInflater(context).inflate(R.menu.menu_tag_actions, menu)
                menu.forEach {
                    it.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.action_tag_select -> {
                                model?.let {
                                    action(
                                        it,
                                        TagSelectAction.SELECT
                                    )
                                }
                            }
                            R.id.action_tag_rename -> {
                                model?.let { tag ->
                                    val title = resources.getString(R.string.rename_title, tag.name)
                                    context.showSimpleInputDialog(title) {
                                        action(
                                            tag.copy(name = it.toString()),
                                            TagSelectAction.CONTEXT_MENU_RENAME
                                        )
                                    }
                                    Unit
                                }
                            }
                            R.id.action_tag_remove -> {
                                model?.let {
                                    TagOpsUI.showConfirmationDialogOnDelete(context, it) {
                                        action(
                                            it,
                                            TagSelectAction.CONTEXT_MENU_REMOVE
                                        )
                                    }
                                }
                            }
                            R.id.action_tag_color -> {
                                model?.let {
                                    TagOpsUI.showColorPicker(context, it) { t ->
                                        action(
                                            t,
                                            TagSelectAction.CONTEXT_MENU_CHANGE_COLOR
                                        )
                                    }

                                }
                            }
                        }
                        true
                    }
                }
            }

            when (tagBehaviour.selectPolicy) {
                TagBehaviour.SelectPolicy.SELECT_ON_TAP -> {
                    setOnClickListener {
                        model?.let {
                            action(
                                it,
                                TagSelectAction.SELECT
                            )
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    override fun onBind(model: Tag) {
        with(itemView.cast<Chip>()) {
            text = model.name
            setContrastingTextColor(Color.parseColor(model.color))
        }
    }


}

fun Chip.setContrastingTextColor(@ColorInt color: Int) {
    chipBackgroundColor = ColorStateList.valueOf(color)
    chipStrokeColor = ColorStateList.valueOf(ColorUtils.blendARGB(Color.WHITE, color, 0.5f))
    setTextColor(
        ColorUtilsExtra.contrastColor(color)(
            context.getColorCompat(R.color.secondaryTextColor),
            context.getColorCompat(R.color.primaryTextColor)
        )
    )
}