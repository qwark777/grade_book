// MessageFragment.kt
package com.example.one.ui.messages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.one.databinding.FragmentMessagesBinding
import com.yourpackage.diaryschool.network.ApiManager
import com.yourpackage.diaryschool.network.MessageResponse
import kotlinx.coroutines.launch

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
        currentUserId = apiManager.getUserId()

        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            return
        }

        adapter = MessageAdapter(currentUserId = currentUserId)
        Log.d("DEBUG", "currentUserId = $currentUserId")

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.sendButton.setOnClickListener {
            val content = binding.messageEditText.text.toString()
            if (content.isNotBlank()) {
                sendMessage(content)
                binding.messageEditText.setText("")
            }
        }

        loadMessages()
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            val messages = apiManager.getMessagesWithUser(otherUserId)
            adapter.submitList(messages)
            binding.recyclerView.scrollToPosition(messages.size - 1)
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
