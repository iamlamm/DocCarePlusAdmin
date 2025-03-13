package com.healthtech.doccareplusadmin.ui.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.ItemUserBinding
import com.healthtech.doccareplusadmin.domain.model.User
import timber.log.Timber

class AllUserAdapter : ListAdapter<User, AllUserAdapter.UserViewHolder>(UserDiffCallback()) {
    private var onUserClickListener: ((User) -> Unit)? = null
    private var onUserLongClickListener: ((User) -> Unit)? = null
    private var onEditClickListener: ((User) -> Unit)? = null
    private var onDeleteClickListener: ((User) -> Unit)? = null

    override fun submitList(list: List<User>?) {
        if (list == null) {
            super.submitList(null)
            return
        }

        val uniqueList = list.distinctBy { it.id }
        if (uniqueList.size != list.size) {
            Timber.w("Detected duplicates in adapter data: ${list.size} -> ${uniqueList.size}")
            val duplicateIds = list.groupBy { it.id }
                .filter { it.value.size > 1 }
                .keys
            Timber.w("Duplicate user IDs: $duplicateIds")
        }
        super.submitList(uniqueList)
    }

    fun setOnUserClickListener(listener: (User) -> Unit) {
        onUserClickListener = listener
    }

    fun setOnUserLongClickListener(listener: (User) -> Unit) {
        onUserLongClickListener = listener
    }

    fun setOnEditClickListener(listener: (User) -> Unit) {
        onEditClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (User) -> Unit) {
        onDeleteClickListener = listener
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isExpanded = false

        fun bind(user: User) {
            binding.apply {
                tvUserName.text = user.name
                tvUserEmail.text = user.email
                tvUserPhone.text = user.phoneNumber
//                tvUserRole.text = user.role.name

                // Load avatar with Glide
                Glide.with(root.context)
                    .load(user.avatar)
                    .apply(
                        RequestOptions()
                            .placeholder(R.mipmap.avatar_male_default)
                            .error(R.mipmap.avatar_male_default)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(ivUserAvatar)

                // Set age, gender, blood type if available
                tvUserAge.text = user.age?.toString() ?: "N/A"
                tvUserGender.text = user.gender?.name ?: "N/A"
                tvUserBloodType.text = user.bloodType ?: "N/A"

                // Set click listeners
                root.setOnClickListener {
                    onUserClickListener?.invoke(user)
                }

                root.setOnLongClickListener {
                    onUserLongClickListener?.invoke(user)
                    true
                }

                // Expand/collapse details
                btnExpandUser.setOnClickListener {
                    isExpanded = !isExpanded
                    layoutUserDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE
                    btnExpandUser.setImageResource(
                        if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
                    )
                }

                // Set action buttons
                btnEditUser.setOnClickListener {
                    onEditClickListener?.invoke(user)
                }

                btnDeleteUser.setOnClickListener {
                    onDeleteClickListener?.invoke(user)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        Timber.d("Binding user at position $position: ${getItem(position).id} - ${getItem(position).name}")
        holder.bind(getItem(position))
    }
}