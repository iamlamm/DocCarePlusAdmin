package com.healthtech.doccareplusadmin.ui.notification.adapter

import androidx.recyclerview.widget.DiffUtil
import com.healthtech.doccareplusadmin.domain.model.Notification

class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
    override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem == newItem
    }
}