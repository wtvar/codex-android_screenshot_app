package com.example.screenshots.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.screenshots.databinding.ItemDayHeaderBinding
import com.example.screenshots.databinding.ItemScreenshotBinding
import com.example.screenshots.model.DayGroup
import com.example.screenshots.model.ScreenshotItem
import java.time.format.DateTimeFormatter

class ScreenshotAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val rows = mutableListOf<RowItem>()
    private val headerFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    sealed class RowItem {
        data class Header(val title: String) : RowItem()
        data class Screenshot(val item: ScreenshotItem) : RowItem()
    }

    fun submitGroups(groups: List<DayGroup>) {
        rows.clear()
        groups.forEach { group ->
            rows += RowItem.Header(group.date.format(headerFormatter))
            group.items.forEach { item ->
                rows += RowItem.Screenshot(item)
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (rows[position]) {
            is RowItem.Header -> VIEW_TYPE_HEADER
            is RowItem.Screenshot -> VIEW_TYPE_SCREENSHOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemDayHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemScreenshotBinding.inflate(inflater, parent, false)
                ScreenshotViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is RowItem.Header -> (holder as HeaderViewHolder).bind(row.title)
            is RowItem.Screenshot -> (holder as ScreenshotViewHolder).bind(row.item)
        }
    }

    override fun getItemCount(): Int = rows.size

    class HeaderViewHolder(private val binding: ItemDayHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.headerText.text = title
        }
    }

    class ScreenshotViewHolder(private val binding: ItemScreenshotBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ScreenshotItem) {
            binding.titleText.text = item.displayName
            Glide.with(binding.thumbnail)
                .load(item.uri)
                .centerCrop()
                .into(binding.thumbnail)
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_SCREENSHOT = 1
    }
}
