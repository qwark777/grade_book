// SubjectsAdapter.kt
package com.example.one

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.one.R

data class SubjectAvgUI(val name: String, val avg: Double, val count: Int)

class SubjectsAdapter(
    private val onItemClick: (SubjectAvgUI) -> Unit
) : ListAdapter<SubjectAvgUI, SubjectsAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<SubjectAvgUI>() {
        override fun areItemsTheSame(o: SubjectAvgUI, n: SubjectAvgUI) = o.name == n.name
        override fun areContentsTheSame(o: SubjectAvgUI, n: SubjectAvgUI) = o == n
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_subject_avg, parent, false)
        return VH(v, onItemClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(itemView: View, private val onItemClick: (SubjectAvgUI) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val card: CardView? = itemView.findViewById(R.id.tvCard)
        private val tvName: TextView = itemView.findViewById(R.id.tvSubject)
        private val tvAvg: TextView = itemView.findViewById(R.id.tvAvg)
        private val tvCount: TextView = itemView.findViewById(R.id.tvCount)

        fun bind(item: SubjectAvgUI) {
            tvName.text = item.name
            tvAvg.text = "Средний: ${"%.2f".format(item.avg)}"
            tvCount.text = "Оценок: ${item.count}"

            (card ?: itemView).setOnClickListener { onItemClick(item) }
        }
    }
}
