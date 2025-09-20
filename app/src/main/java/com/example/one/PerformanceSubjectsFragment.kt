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
    private lateinit var adapter: SubjectsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPerformanceSubjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SubjectsAdapter { subject ->
            openSubjectDetailsSafely(subject.name)
        }

        binding.rvSubjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubjects.adapter = adapter

        adapter.submitList(
            listOf(
                SubjectAvgUI("Математика", 4.5, 28),
                SubjectAvgUI("Русский", 4.2, 26),
                SubjectAvgUI("История", 3.7, 20),
                SubjectAvgUI("Информатика", 4.9, 18),
            )
        )
    }

    private fun openSubjectDetailsSafely(subjectName: String) {
        val activityContainer = requireActivity().findViewById<View?>(R.id.fragment_container)
        when {
            // 1) Есть контейнер в Activity → вставляем туда (на весь экран)
            activityContainer != null -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SubjectDetailsFragment.newInstance(subjectName))
                    .addToBackStack("subject_$subjectName")
                    .commit()
            }

            // 2) Нет контейнера в Activity → используем внутренний контейнер фрагмента
            view?.findViewById<View?>(R.id.subject_details_container) != null -> {
                childFragmentManager.beginTransaction()
                    .replace(R.id.subject_details_container, SubjectDetailsFragment.newInstance(subjectName))
                    .addToBackStack("subject_$subjectName")
                    .commit()
            }


        }
    }

}
