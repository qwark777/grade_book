package com.example.one.adapters

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.one.R

class SubjectAdapter(
    private val subjects: List<String>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    private var selected = 0

    inner class SubjectViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val context = parent.context
        val textView = TextView(context).apply {
            setPadding(32, 16, 32, 16)
            background = ContextCompat.getDrawable(context, R.drawable.edit_button)
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
        return SubjectViewHolder(textView)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.view.text = subjects[position]
        holder.view.isSelected = position == selected
        holder.view.alpha = if (position == selected) 1f else 0.6f

        holder.view.setOnClickListener {
            val previous = selected
            selected = position
            notifyItemChanged(previous)
            notifyItemChanged(selected)
            onClick(position)
        }
    }

    override fun getItemCount() = subjects.size
}