package com.example.one.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.one.TeacherData
import com.example.one.databinding.FragmentTeachersBinding
import com.example.one.dialogs.AddTeacherDialogFragment

class TeachersFragment : Fragment() {

    private var _binding: FragmentTeachersBinding? = null
    private val binding get() = _binding!!

    private val teacherAdapter = TeacherAdapter()

    private val mockTeachers = mutableListOf(
        TeacherData(
            fullName = "Иван Петров",
            workPlace = "Школа №5",
            location = "Москва",
            subject = "Математика",
            classes = "10А, 11Б",
            username = "ivan.petrov",
            password = "12345"
        ),
        TeacherData(
            fullName = "Ольга Смирнова",
            workPlace = "Лицей №2",
            location = "Санкт-Петербург",
            subject = "Химия",
            classes = "9А, 9Б",
            username = "olga.smirnova",
            password = "54321"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeachersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.teacherRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.teacherRecyclerView.adapter = teacherAdapter
        teacherAdapter.submitList(mockTeachers.toList())

        binding.addTeacherButton.setOnClickListener {
            AddTeacherDialogFragment.newInstance {
                // Обновить список преподавателей
            }.show(parentFragmentManager, "AddTeacherDialog")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
