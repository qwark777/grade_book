// StartNewChatFragment.kt
package com.example.one.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.one.R
import com.example.one.databinding.FragmentStartNewChatBinding
import com.yourpackage.diaryschool.network.ApiManager
import com.yourpackage.diaryschool.network.Student2
import kotlinx.coroutines.launch

class StartNewChatFragment : Fragment() {

    private lateinit var binding: FragmentStartNewChatBinding
    private lateinit var adapter: ConversationAdapter
    private lateinit var apiManager: ApiManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStartNewChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiManager = ApiManager(requireContext())

        adapter = ConversationAdapter { user ->
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    com.example.one.ui.messages.MessageFragment.newInstance(
                        // если id = Long, то добавь .toInt()
                        user.id
                    )
                )

                .addToBackStack(null)
                .commit()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        loadUsers()
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            val students = apiManager.getAllStudents()
            val teachers = apiManager.getAllTeachers()
            adapter.submitList((students + teachers).filterIsInstance<Student2>())
        }
    }
}
