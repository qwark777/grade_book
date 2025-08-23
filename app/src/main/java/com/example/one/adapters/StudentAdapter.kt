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
import com.example.one.StudentData

class StudentAdapter(
    private val onWriteClick: ((StudentData) -> Unit)? = null
) : ListAdapter<StudentData, StudentAdapter.StudentViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.studentName)
        private val classText: TextView = itemView.findViewById(R.id.studentClass)
        private val schoolText: TextView = itemView.findViewById(R.id.studentSchool)
        private val writeButton: Button? = itemView.findViewById(R.id.writeButton) // если есть в layout

        fun bind(student: StudentData) {
            nameText.text = student.fullName
            classText.text = student.className
            schoolText.text = student.school

            writeButton?.setOnClickListener { onWriteClick?.invoke(student) }
            // Если кнопки нет и нужно по клику по всей карточке:
            // itemView.setOnClickListener { onWriteClick?.invoke(student) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<StudentData>() {
        override fun areItemsTheSame(oldItem: StudentData, newItem: StudentData): Boolean {
            return oldItem.id == newItem.id     // сравниваем по стабильному идентификатору
        }
        override fun areContentsTheSame(oldItem: StudentData, newItem: StudentData): Boolean {
            return oldItem == newItem
        }
    }
}
