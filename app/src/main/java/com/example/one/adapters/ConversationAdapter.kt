// ConversationAdapter.kt
package com.example.one.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.one.databinding.ItemConversationBinding
import com.yourpackage.diaryschool.network.Student2

class ConversationAdapter(
    private val onClick: (Student2) -> Unit
) : ListAdapter<Student2, ConversationAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Student2>() {
            override fun areItemsTheSame(oldItem: Student2, newItem: Student2): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Student2, newItem: Student2): Boolean = oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemConversationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: Student2) {
            binding.textName.text = user.full_name
            binding.textClass.text = user.class_name ?: ""

            if (!user.photo_url.isNullOrBlank()) {
                Glide.with(binding.root.context)
                    .load("http://10.0.2.2:8001" + user.photo_url)
                    .into(binding.imageAvatar)
            } else {
                binding.imageAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
            }

            binding.root.setOnClickListener {
                onClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
