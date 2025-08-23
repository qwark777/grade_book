package com.example.one.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.one.FragmentProfileAnyUser
import com.example.one.R
import com.example.one.databinding.FragmentMessagesBinding
import com.yourpackage.diaryschool.network.ApiManager
import com.yourpackage.diaryschool.network.MessageResponse
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MessageFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding
    private lateinit var apiManager: ApiManager
    private lateinit var adapter: MessageAdapter
    private var currentUserId: Int = -1

    private var otherUserId: Int = -1

    // Список сообщений
    private val messagesList = mutableListOf<MessageResponse>()

    // WebSocket
    private var ws: WebSocket? = null

    companion object {
        private const val ARG_OTHER_USER_ID = "arg_other_user_id"

        fun newInstance(otherUserId: Int): MessageFragment {
            val fragment = MessageFragment()
            val args = Bundle().apply {
                putInt(ARG_OTHER_USER_ID, otherUserId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        otherUserId = requireArguments().getInt(ARG_OTHER_USER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            binding.recyclerView.adapter = adapter

            binding.sendButton.setOnClickListener {
                val content = binding.messageEditText.text.toString().trim()
                if (content.isNotEmpty()) {
                    sendMessageOptimistic(content)
                    binding.messageEditText.setText("")
                }
            }

            // Переход в профиль собеседника
            binding.chatHeader.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, FragmentProfileAnyUser.newInstance(otherUserId))
                    .addToBackStack(null)
                    .commit()
            }

            loadUserInfo()
            loadMessages()
        }
    }

    override fun onStart() {
        super.onStart()
        // Подключаемся к WebSocket
        ws = apiManager.openChatSocket(object : ApiManager.ChatListener {
            override fun onMessage(msg: MessageResponse) {
                val toThisChat =
                    (msg.sender_id == otherUserId && msg.recipient_id == currentUserId) ||
                            (msg.sender_id == currentUserId && msg.recipient_id == otherUserId)

                if (toThisChat) {
                    requireActivity().runOnUiThread {
                        messagesList.add(msg)
                        adapter.submitMessages(messagesList.toList())
                        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }

            override fun onFailure(t: Throwable) {
                // Можно залогировать ошибку, но без всплывашек
            }
        })
    }

    override fun onStop() {
        super.onStop()
        ws?.close(1000, null)
        ws = null
    }

    private fun loadUserInfo() {
        lifecycleScope.launch {
            val user = apiManager.getUserPublicInfo(otherUserId)
            user?.let {
                binding.userName.text = it.full_name
                binding.userPosition.text = it.class_name ?: it.work_place ?: "Не указано"
                Glide.with(requireContext())
                    .load("http://10.0.2.2:8001/profile_photo/${it.id}")
                    .circleCrop()
                    .into(binding.userPhoto)
            }
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            val msgs: List<MessageResponse> = apiManager.getMessagesWithUser(otherUserId)
            messagesList.clear()
            messagesList.addAll(msgs)
            adapter.submitMessages(messagesList.toList())
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }

    /** Оптимистическая отправка: сразу добавляем локально, потом шлём на сервер */
    private fun sendMessageOptimistic(content: String) {
        val temp = MessageResponse(
            id = null,
            sender_id = currentUserId,
            recipient_id = otherUserId,
            content = content,
            created_at = nowIso()
        )
        messagesList.add(temp)
        adapter.submitMessages(messagesList.toList())
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)

        lifecycleScope.launch {
            val created = apiManager.sendMessage(otherUserId, content)
            if (created == null) {
                Toast.makeText(requireContext(), "Ошибка отправки", Toast.LENGTH_SHORT).show()
                return@launch
            }
            loadMessages()
        }
    }

    private fun nowIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(System.currentTimeMillis())
    }
}
