package com.example.one.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.one.StudentData
import com.example.one.databinding.FragmentStudentsBinding

class StudentsFragment : Fragment() {

    private var _binding: FragmentStudentsBinding? = null
    private val binding get() = _binding!!

    private val studentAdapter = StudentAdapter()

    private val mockStudents = mutableListOf(
        StudentData(
            fullName = "Алексей Кузнецов",
            className = "10А",
            school = "Школа №3"
        ),
        StudentData(
            fullName = "Мария Иванова",
            className = "9Б",
            school = "Гимназия №7"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.studentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.studentRecyclerView.adapter = studentAdapter
        studentAdapter.submitList(mockStudents.toList())

        binding.addStudentButton.setOnClickListener {
            // Пока без диалога — просто добавим фейкового студента
            mockStudents.add(
                StudentData("Новый Ученик", "7Г", "Школа Тест")
            )
            studentAdapter.submitList(mockStudents.toList())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
