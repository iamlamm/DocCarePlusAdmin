package  com.healthtech.doccareplusadmin.ui.category.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.healthtech.doccareplusadmin.R
import com.healthtech.doccareplusadmin.databinding.ItemCategoryBinding
import com.healthtech.doccareplusadmin.domain.model.Category
import androidx.recyclerview.widget.DiffUtil

class AllCategoriesAdapter : RecyclerView.Adapter<AllCategoriesAdapter.AllCategoriesViewHolder>() {
    private var categories = listOf<Category>()
    private var onCategoryClickListener: ((Category) -> Unit)? = null
    private var onCategoryLongClickListener: ((Category) -> Unit)? = null

    fun setCategories(newCategories: List<Category>) {
        val diffCallback = CategoryDiffCallback(categories, newCategories)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        categories = newCategories
        diffResult.dispatchUpdatesTo(this)
    }

    // Thêm click listener
    fun setOnCategoryClickListener(listener: (Category) -> Unit) {
        onCategoryClickListener = listener
    }

    fun setOnCategoryLongClickListener(listener: (Category) -> Unit) {
        onCategoryLongClickListener = listener
    }

    inner class AllCategoriesViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.apply {
                tvCategoryName.text = category.name

                // Tối ưu Glide với caching
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.cardiology)

                Glide.with(root.context)
                    .load(category.icon)
                    .apply(requestOptions)
                    .into(ivCategory)

                // Thêm click listener
                root.setOnClickListener {
                    onCategoryClickListener?.invoke(category)
                }

                root.setOnLongClickListener {
                    onCategoryLongClickListener?.invoke(category)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllCategoriesViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllCategoriesViewHolder(binding)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: AllCategoriesViewHolder, position: Int) {
        holder.bind(categories[position])
    }
}