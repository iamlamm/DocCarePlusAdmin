package com.healthtech.doccareplusadmin.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.ItemActivityBinding
import com.healthtech.doccareplusadmin.domain.model.Activity
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityAdapter :
    ListAdapter<Activity, ActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = getItem(position)
        Timber.d("Binding activity at position $position: ${activity.id}")
        holder.bind(activity)
    }

    class ActivityViewHolder(private val binding: ItemActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: Activity) {
            binding.apply {
                tvActivityTitle.text = activity.title
                tvActivityDescription.text = activity.description
                tvActivityTime.text = formatTimestamp(activity.timestamp)

                // Set icon based on activity type
                val iconRes = getIconForActivityType(activity.type)
                ivActivityIcon.setImageResource(iconRes)
                
                // Force layout update
                root.requestLayout()
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("dd MMM, HH:mm", Locale("vi"))
            return format.format(date)
        }

        private fun getIconForActivityType(type: String): Int {
            return when (type.lowercase()) {
                "appointment" -> R.drawable.ic_appointment
                "appointment_cancelled" -> R.drawable.ic_appointment
                "user_registration" -> R.drawable.ic_user
                "user_added" -> R.mipmap.ic_user_boy_added
                "user_updated" -> R.mipmap.ic_user_updated
                "user_blocked" -> R.drawable.ic_user
                "user_deleted" -> R.mipmap.ic_user_deleted
                "doctor_added" -> R.mipmap.ic_user_boy_added
                "doctor_updated" -> R.mipmap.ic_user_updated
                "doctor_deleted" -> R.mipmap.ic_user_deleted
                "category_added" -> R.drawable.ic_category
                "category_updated" -> R.drawable.ic_category
                "category_deleted" -> R.drawable.ic_category
//                "admin_login" -> R.drawable.ic_admin
//                "admin_logout" -> R.drawable.ic_admin
                else -> R.mipmap.avatar_bear_default
            }
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<Activity>() {
        override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem == newItem
        }
    }
}