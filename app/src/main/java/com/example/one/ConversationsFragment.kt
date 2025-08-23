// ConversationsFragment.kt
package com.example.one.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.one.R
import com.example.one.databinding.FragmentConversationsBinding
import com.yourpackage.diaryschool.network.ApiManager
import com.yourpackage.diaryschool.network.Student2
import kotlinx.coroutines.launch

class ConversationsFragment : Fragment() {

    private lateinit var binding: FragmentConversationsBinding
    private lateinit var adapter: ConversationAdapter
    private lateinit var apiManager: ApiManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConversationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // ConversationsFragment.kt

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiManager = ApiManager(requireContext())

        adapter = ConversationAdapter { user ->
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    com.example.one.ui.messages.MessageFragment.newInstance(
                        // если user.id = Long, добавь .toInt()
                        user.id
                    )
                )
                .addToBackStack(null)
                .commit()
        }

        binding.conversationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.conversationsRecyclerView.adapter = adapter

        binding.fabNewMessage.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, StartNewChatFragment())
                .addToBackStack(null)
                .commit()
        }

        loadStartedChats()
    }


    private fun loadStartedChats() {
        lifecycleScope.launch {
            val startedChats = apiManager.getStartedConversations()
            adapter.submitList(startedChats.map {
                Student2(
                    id = it.user_id,
                    full_name = it.full_name,
                    photo_url = it.photo_url,
                    class_name = null
                )
            })
        }
    }
}
