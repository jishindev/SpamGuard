package dev.jishin.android.spamguard.custom.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

open class SimpleRvAdapter<M, H : RecyclerView.ViewHolder>(
    @LayoutRes private val layoutResId: Int,
    private val vhBuilder: (view: View) -> H,
    private var binder: (H, M) -> Unit,
    areContentsSame: (old: M, new: M) -> Boolean,
    diffCallback: DiffUtil.ItemCallback<M> = getDiffCallback(areContentsSame)
) : ListAdapter<M, H>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return vhBuilder(view)
    }

    override fun onBindViewHolder(holder: H, position: Int) {
        getItem(position)?.let { binder(holder, it) }
    }

    fun updateItems(itemList: List<M>) {
        Timber.i("updateItems() called with: itemList = [$itemList]")
        submitList(ArrayList(itemList.toMutableList()))
    }

    companion object {
        fun <T> getDiffCallback(areContentsSame: (old: T, new: T) -> Boolean) =
            object : DiffUtil.ItemCallback<T>() {
                override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem == newItem

                override fun areContentsTheSame(oldItem: T, newItem: T) =
                    areContentsSame(oldItem, newItem)
            }
    }
}