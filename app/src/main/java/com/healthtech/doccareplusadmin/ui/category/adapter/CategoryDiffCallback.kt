package com.healthtech.doccareplusadmin.ui.category.adapter

import androidx.recyclerview.widget.DiffUtil
import com.healthtech.doccareplusadmin.domain.model.Category

class CategoryDiffCallback(
    private val oldList: List<Category>,
    private val newList: List<Category>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem.name == newItem.name && oldItem.icon == newItem.icon
    }
}