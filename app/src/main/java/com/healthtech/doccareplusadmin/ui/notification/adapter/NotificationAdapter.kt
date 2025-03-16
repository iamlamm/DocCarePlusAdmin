package com.healthtech.doccareplusadmin.ui.notification.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.ItemNotificationBinding
import com.healthtech.doccareplusadmin.domain.model.Notification
import com.healthtech.doccareplusadmin.domain.model.NotificationType
import com.healthtech.doccareplusadmin.utils.getTimeAgo

class NotificationAdapter :
    ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.apply {
                root.setBackgroundResource(
                    if (notification.read) R.color.white
                    else R.color.unread_notification_bg
                )

                tvNotificationTitle.apply {
                    setTypeface(null, if (notification.read) Typeface.NORMAL else Typeface.BOLD)
                    text = notification.title
                }

                tvNotificationMessage.text = notification.message
                tvNotificationTime.text = notification.time.getTimeAgo()

                ivNotificationIcon.setImageResource(
                    when (notification.type) {
                        NotificationType.DOCTOR_REGISTRATION -> R.drawable.ic_doctor
                        NotificationType.USER_REGISTRATION -> R.drawable.ic_user
                        NotificationType.APPOINTMENT_BOOKED -> R.drawable.ic_calendar
                        NotificationType.SYSTEM -> R.drawable.ic_info
                        NotificationType.REPORT -> R.drawable.ic_report
                        NotificationType.ADMIN_NEW_APPOINTMENT -> R.drawable.ic_calendar
                    }
                )

                root.setOnClickListener {
                    if (!notification.read) {
                        onNotificationClick?.invoke(notification.id)
                    }
                }
            }
        }
    }

    private var onNotificationClick: ((String) -> Unit)? = null

    fun setOnNotificationClickListener(listener: (String) -> Unit) {
        onNotificationClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}