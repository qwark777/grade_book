package com.example.one

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.one.databinding.ItemSubjectAvgBinding

data class SubjectAvgUI(val subject: String, val avg: Double, val count: Int)

class SubjectsAdapter :
    ListAdapter<SubjectAvgUI, SubjectsAdapter.VH>(DIFF) {

    object DIFF : DiffUtil.ItemCallback<SubjectAvgUI>() {
        override fun areItemsTheSame(o: SubjectAvgUI, n: SubjectAvgUI) = o.subject == n.subject
        override fun areContentsTheSame(o: SubjectAvgUI, n: SubjectAvgUI) = o == n
    }

    inner class VH(val b: ItemSubjectAvgBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemSubjectAvgBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.tvSubject.text = item.subject
        holder.b.tvAvg.text = String.format("%.2f", item.avg)
        holder.b.tvCount.text = "${item.count} оценок"
    }
}
