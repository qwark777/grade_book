package com.example.one.ui.messages

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.one.UserBase
import com.example.one.databinding.FragmentMessagesBinding
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.launch
import com.yourpackage.diaryschool.network.MessageResponse

class MessageFragment(private val otherUserId: Int) : Fragment() {

    private lateinit var binding: FragmentMessagesBinding
    private lateinit var apiManager: ApiManager
    private lateinit var adapter: MessageAdapter
    private var currentUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiManager = ApiManager(requireContext())

        lifecycleScope.launch {
            val isValid = apiManager.isTokenValid()
            if (!isValid) {
                Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
                return@launch
            }

            currentUserId = apiManager.getUserId()

            adapter = MessageAdapter(currentUserId = currentUserId)
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = adapter

            binding.sendButton.setOnClickListener {
                val content = binding.messageEditText.text.toString()
                if (content.isNotBlank()) {
                    sendMessage(content)
                    binding.messageEditText.setText("")
                }
            }

            loadUserInfo()
            loadMessages()
        }
    }

    private fun loadUserInfo() {
        lifecycleScope.launch {
            val user = apiManager.getUserPublicInfo(otherUserId)
            user?.let {
                binding.userName.text = it.full_name
                binding.userPosition.text = it.class_name ?: it.work_place ?: "Не указано"
                Glide.with(requireContext())
                    .load("http://10.0.2.2:8001/profile_photo/${user.id}")
                    .circleCrop()
                    .into(binding.userPhoto)
            }
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            val messages: List<MessageResponse> = apiManager.getMessagesWithUser(otherUserId)
            adapter.submitMessages(messages)
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun sendMessage(content: String) {
        lifecycleScope.launch {
            val success = apiManager.sendMessage(otherUserId, content)
            if (success) {
                loadMessages()
            } else {
                Toast.makeText(requireContext(), "Ошибка отправки", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
