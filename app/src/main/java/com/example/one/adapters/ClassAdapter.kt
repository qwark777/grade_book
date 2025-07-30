package com.example.one.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.one.ClassItem
import com.example.one.R

/**
 * Адаптер для отображения списка классов в виде кнопок.
 *
 * @param onClick — функция, вызываемая при нажатии на кнопку класса.
 */


class ClassAdapter(
    private val onClick: (ClassItem) -> Unit
) : ListAdapter<ClassItem, ClassAdapter.ClassViewHolder>(DiffCallback()) {

    inner class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val classNameText: TextView = itemView.findViewById(R.id.classNameText)
        private val studentCountText: TextView = itemView.findViewById(R.id.studentCountText)

        fun bind(item: ClassItem) {
            classNameText.text = "Класс ${item.name}"
            studentCountText.text = "${item.studentCount} учеников"
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_class, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ClassItem>() {
        override fun areItemsTheSame(oldItem: ClassItem, newItem: ClassItem) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: ClassItem, newItem: ClassItem) =
            oldItem == newItem
    }
}