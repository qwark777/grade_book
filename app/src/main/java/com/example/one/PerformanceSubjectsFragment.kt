package com.example.one

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.one.databinding.FragmentPerformanceSubjectsBinding

class PerformanceSubjectsFragment : Fragment() {

    private lateinit var binding: FragmentPerformanceSubjectsBinding
    private val adapter = SubjectsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPerformanceSubjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvSubjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubjects.adapter = adapter

        // демо-данные
        adapter.submitList(
            listOf(
                SubjectAvgUI("Математика", 4.5, 28),
                SubjectAvgUI("Русский", 4.2, 26),
                SubjectAvgUI("История", 3.7, 20),
                SubjectAvgUI("Информатика", 4.9, 18),
            )
        )
    }
}
