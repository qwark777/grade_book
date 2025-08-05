package com.example.one.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.one.databinding.ItemMessageIncomingBinding
import com.example.one.databinding.ItemMessageOutgoingBinding
import com.yourpackage.diaryschool.network.MessageResponse

class MessageAdapter(private val currentUserId: Int) :
    ListAdapter<MessageResponse, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val VIEW_TYPE_INCOMING = 0
        private const val VIEW_TYPE_OUTGOING = 1

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MessageResponse>() {
            override fun areItemsTheSame(oldItem: MessageResponse, newItem: MessageResponse): Boolean {
                return oldItem.created_at == newItem.created_at && oldItem.sender_id == newItem.sender_id
            }

            override fun areContentsTheSame(oldItem: MessageResponse, newItem: MessageResponse): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.sender_id == currentUserId) VIEW_TYPE_OUTGOING else VIEW_TYPE_INCOMING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_OUTGOING) {
            val binding = ItemMessageOutgoingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            OutgoingMessageViewHolder(binding)
        } else {
            val binding = ItemMessageIncomingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            IncomingMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is OutgoingMessageViewHolder -> holder.bind(message)
            is IncomingMessageViewHolder -> holder.bind(message)
        }
    }

    inner class IncomingMessageViewHolder(private val binding: ItemMessageIncomingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: MessageResponse) {
            binding.textMessage.text = message.content
            binding.textTime.text = message.created_at.substring(11, 16)
        }
    }

    inner class OutgoingMessageViewHolder(private val binding: ItemMessageOutgoingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: MessageResponse) {
            binding.textMessage.text = message.content
            binding.textTime.text = message.created_at.substring(11, 16)
        }
    }
}
