package com.example.one.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.one.R
import com.example.one.databinding.ItemSubjectChipBinding

data class SubjectChip(val title: String)

class SubjectChipAdapter(
    private val onClick: (SubjectChip) -> Unit
) : ListAdapter<SubjectChip, SubjectChipAdapter.VH>(Diff) {

    private var selectedPos: Int = RecyclerView.NO_POSITION

    object Diff : DiffUtil.ItemCallback<SubjectChip>() {
        override fun areItemsTheSame(o: SubjectChip, n: SubjectChip) = o.title == n.title
        override fun areContentsTheSame(o: SubjectChip, n: SubjectChip) = o == n
    }

    inner class VH(val b: ItemSubjectChipBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: SubjectChip, position: Int) {
            b.textChip.text = item.title

            val selected = position == selectedPos
            val bg = if (selected) R.drawable.bg_room_chip else R.drawable.bg_change_badge
            val textColor = if (selected) R.color.white else R.color.black

            b.textChip.setBackgroundResource(bg)
            b.textChip.setTextColor(ContextCompat.getColor(b.root.context, textColor))

            b.root.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val old = selectedPos
                    selectedPos = pos
                    if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
                    notifyItemChanged(selectedPos)
                    onClick(item)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        return VH(ItemSubjectChipBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), position)
    }

    fun selectFirstIfAny() {
        if (currentList.isNotEmpty()) {
            selectedPos = 0
            notifyItemChanged(0)
        }
    }
}
