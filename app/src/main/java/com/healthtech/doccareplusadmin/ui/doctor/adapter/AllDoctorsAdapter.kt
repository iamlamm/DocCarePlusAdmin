package  com.healthtech.doccareplusadmin.ui.doctor.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.ItemDoctorBinding
import com.healthtech.doccareplusadmin.domain.model.Doctor
import timber.log.Timber

class AllDoctorsAdapter : RecyclerView.Adapter<AllDoctorsAdapter.AllDoctorsViewHolder>() {
    private var doctors = listOf<Doctor>()
    private var onDoctorClickListener: ((Doctor) -> Unit)? = null
    private var onDoctorLongClickListener: ((Doctor) -> Unit)? = null
    private var onEditClickListener: ((Doctor) -> Unit)? = null
    private var onDeleteClickListener: ((Doctor) -> Unit)? = null

    fun setDoctors(newDoctors: List<Doctor>) {
        val diffCallback = DoctorDiffCallback(doctors, newDoctors)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        doctors = newDoctors
        diffResult.dispatchUpdatesTo(this)
    }

    // Click listener cho card bác sĩ
    fun setOnDoctorClickListener(listener: (Doctor) -> Unit) {
        onDoctorClickListener = listener
    }

    // Long click listener cho xóa bác sĩ
    fun setOnDoctorLongClickListener(listener: (Doctor) -> Unit) {
        onDoctorLongClickListener = listener
    }

    // Click listener cho nút edit
    fun setOnEditClickListener(listener: (Doctor) -> Unit) {
        onEditClickListener = listener
    }

    // Click listener cho nút delete
    fun setOnDeleteClickListener(listener: (Doctor) -> Unit) {
        onDeleteClickListener = listener
    }

    inner class AllDoctorsViewHolder(private val binding: ItemDoctorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(doctor: Doctor) {
            binding.apply {
                tvDoctorName.text = doctor.name
                tvDoctorSpecialty.text = doctor.specialty
                tvDoctorRate.text = doctor.rating.toString()
                tvDoctorReviewCount.text = "(${doctor.reviews})"
                tvDoctorFee.text = "$${doctor.fee}"

                // Tối ưu Glide với caching
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.doctor_avatar_1)

                Glide.with(root.context)
                    .load(doctor.avatar)
                    .apply(requestOptions)
                    .into(ivDoctorAvatar)

                // Click vào card bác sĩ
                root.setOnClickListener {
                    Timber.d("Doctor clicked: ${doctor.id}")
                    onDoctorClickListener?.invoke(doctor)
                }

                // Long click để xóa
                root.setOnLongClickListener {
                    Timber.d("Doctor long clicked: ${doctor.id}")
                    onDoctorLongClickListener?.invoke(doctor)
                    true
                }

                // Xử lý click vào nút edit (nếu có)
                if (root.findViewById<View>(R.id.btn_edit_doctor) != null) {
                    root.findViewById<View>(R.id.btn_edit_doctor).setOnClickListener {
                        Timber.d("Edit button clicked for doctor: ${doctor.id}")
                        onEditClickListener?.invoke(doctor)
                    }
                }

                // Xử lý click vào nút delete (nếu có)
                if (root.findViewById<View>(R.id.btn_delete_doctor) != null) {
                    root.findViewById<View>(R.id.btn_delete_doctor).setOnClickListener {
                        Timber.d("Delete button clicked for doctor: ${doctor.id}")
                        onDeleteClickListener?.invoke(doctor)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllDoctorsViewHolder {
        val binding = ItemDoctorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllDoctorsViewHolder(binding)
    }

    override fun getItemCount(): Int = doctors.size

    override fun onBindViewHolder(holder: AllDoctorsViewHolder, position: Int) {
        holder.bind(doctors[position])
    }
}