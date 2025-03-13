package com.healthtech.doccareplusadmin.ui.user.adapter

import androidx.recyclerview.widget.DiffUtil
import com.healthtech.doccareplusadmin.domain.model.User
import timber.log.Timber

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        val result = oldItem.id == newItem.id
        if (!result) {
            Timber.d("Items are different: old=${oldItem.id}, new=${newItem.id}")
        }
        return result
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        val result = oldItem.id == newItem.id && 
                    oldItem.name == newItem.name && 
                    oldItem.email == newItem.email &&
                    oldItem.phoneNumber == newItem.phoneNumber &&
                    oldItem.role == newItem.role &&
                    oldItem.createdAt == newItem.createdAt
        
        if (!result) {
            Timber.d("Content changed for user ${oldItem.id}")
        }
        return result
    }
}