package com.example.one.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.one.databinding.ItemMessageIncomingBinding
import com.example.one.databinding.ItemMessageOutgoingBinding
import com.example.one.databinding.ItemDateHeaderBinding
import com.yourpackage.diaryschool.network.MessageResponse
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed class ChatItem {
    data class MessageItem(val message: MessageResponse) : ChatItem()
    data class DateHeaderItem(val date: String) : ChatItem()
}

class MessageAdapter(private val currentUserId: Int) :
    ListAdapter<ChatItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val VIEW_TYPE_INCOMING = 0
        private const val VIEW_TYPE_OUTGOING = 1
        private const val VIEW_TYPE_DATE_HEADER = 2

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatItem>() {
            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is ChatItem.MessageItem -> {
                if (item.message.sender_id == currentUserId) VIEW_TYPE_OUTGOING else VIEW_TYPE_INCOMING
            }
            is ChatItem.DateHeaderItem -> VIEW_TYPE_DATE_HEADER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_OUTGOING -> OutgoingMessageViewHolder(
                ItemMessageOutgoingBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_INCOMING -> IncomingMessageViewHolder(
                ItemMessageIncomingBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_DATE_HEADER -> DateHeaderViewHolder(
                ItemDateHeaderBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ChatItem.MessageItem -> {
                val time = item.message.created_at.substring(11, 16)
                when (holder) {
                    is IncomingMessageViewHolder -> holder.bind(item.message.content, time)
                    is OutgoingMessageViewHolder -> holder.bind(item.message.content, time)
                }
            }
            is ChatItem.DateHeaderItem -> {
                (holder as DateHeaderViewHolder).bind(item.date)
            }
        }
    }

    inner class IncomingMessageViewHolder(private val binding: ItemMessageIncomingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(content: String, time: String) {
            binding.textMessage.text = content
            binding.textTime.text = time
        }
    }

    inner class OutgoingMessageViewHolder(private val binding: ItemMessageOutgoingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(content: String, time: String) {
            binding.textMessage.text = content
            binding.textTime.text = time
        }
    }

    inner class DateHeaderViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.textDate.text = date
        }
    }

    fun submitMessages(messages: List<MessageResponse>) {
        val result = mutableListOf<ChatItem>()
        var lastDate: String? = null

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (message in messages) {
            val dateStr = try {
                val date = inputFormat.parse(message.created_at)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                null
            }

            if (dateStr != null && dateStr != lastDate) {
                result.add(ChatItem.DateHeaderItem(dateStr))
                lastDate = dateStr
            }

            result.add(ChatItem.MessageItem(message))
        }

        submitList(result)
    }

}