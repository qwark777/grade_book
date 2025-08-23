package com.example.one.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.one.R
import com.example.one.TeacherData

class TeacherAdapter(
    private val onWriteClick: ((TeacherData) -> Unit)? = null
) : ListAdapter<TeacherData, TeacherAdapter.TeacherViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teacher, parent, false)
        return TeacherViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TeacherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.teacherName)
        private val subjectClassesText: TextView = itemView.findViewById(R.id.teacherSubjectClasses)
        private val locationText: TextView = itemView.findViewById(R.id.teacherLocation)
        private val writeButton: Button = itemView.findViewById(R.id.writeButton)

        fun bind(teacher: TeacherData) {
            nameText.text = teacher.fullName
            subjectClassesText.text = "${teacher.subject} | ${teacher.classes}"
            locationText.text = teacher.workPlace

            // Кнопка "Написать" → колбэк наружу
            writeButton.setOnClickListener { onWriteClick?.invoke(teacher) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TeacherData>() {
        override fun areItemsTheSame(oldItem: TeacherData, newItem: TeacherData): Boolean {
            return oldItem.username == newItem.username
        }
        override fun areContentsTheSame(oldItem: TeacherData, newItem: TeacherData): Boolean {
            return oldItem == newItem
        }
    }
}
