package com.healthtech.doccareplusadmin.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.ItemActivityBinding
import com.healthtech.doccareplusadmin.domain.model.Activity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import timber.log.Timber

class ActivityAdapter : ListAdapter<Activity, ActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

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
                ivActivityIcon.setImageResource(getIconForActivityType(activity.type))
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
                "user_registration" -> R.drawable.ic_user
                "doctor_approval" -> R.drawable.ic_doctor
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