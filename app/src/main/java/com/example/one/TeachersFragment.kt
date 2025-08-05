package com.example.one.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.one.R
import com.example.one.StudentData
import com.example.one.TeacherData
import com.example.one.databinding.FragmentTeachersBinding
import com.example.one.dialogs.AddTeacherDialogFragment
import com.example.one.adapters.StudentAdapter
import com.example.one.adapters.TeacherAdapter
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeachersFragment : Fragment() {

    private var _binding: FragmentTeachersBinding? = null
    private val binding get() = _binding!!

    private val teacherAdapter = TeacherAdapter()
    private val studentAdapter = StudentAdapter()

    private lateinit var apiManager: ApiManager
    private val students = mutableListOf<StudentData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeachersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.teacherRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        apiManager = ApiManager(requireContext())

        // Изначально показываем преподавателей
        binding.teacherRecyclerView.adapter = teacherAdapter
        loadTeachers()

        binding.switchRoleGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.teachersRadioButton -> {
                    binding.teacherRecyclerView.adapter = teacherAdapter
                    loadTeachers()
                    binding.addTeacherButton.text = "Добавить преподавателя"
                }

                R.id.studentsRadioButton -> {
                    binding.teacherRecyclerView.adapter = studentAdapter
                    loadStudents()
                    binding.addTeacherButton.text = "Добавить ученика"
                }
            }
        }

        binding.addTeacherButton.setOnClickListener {
            val isTeacherSelected = binding.teachersRadioButton.isChecked
            if (isTeacherSelected) {
                AddTeacherDialogFragment.newInstance {
                    loadTeachers()
                }.show(parentFragmentManager, "AddTeacherDialog")
            } else {
                // TODO: реализуй диалог добавления ученика
                Toast.makeText(requireContext(), "Добавление ученика пока не реализовано", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTeachers() {
        lifecycleScope.launch {
            try {
                val serverTeachers = withContext(Dispatchers.IO) {
                    apiManager.getAllTeachers() // должен вернуть List<TeacherNetworkModel>
                }
                println(serverTeachers)
                val teachers = serverTeachers.map { teacher ->
                    TeacherData(
                        fullName = teacher.full_name ?: "Неизвестно",
                        workPlace = teacher.work_place ?: "Не указано",
                        location = teacher.location ?: "Не указано",
                        subject = teacher.subject ?: "Предмет не указан",
                        classes = teacher.classes ?: "Классы не указаны",
                        username = teacher.username ?: "",
                        password = teacher.password ?: ""
                    )
                }

                teacherAdapter.submitList(teachers)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки преподавателей: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadStudents() {
        lifecycleScope.launch {
            try {
                val serverStudents = withContext(Dispatchers.IO) {
                    apiManager.getAllStudents()
                }

                students.clear()
                students.addAll(serverStudents.map { student ->
                    StudentData(
                        fullName = student.full_name ?: "Неизвестно",
                        className = student.class_name ?: "Не указан",
                        school = "Школа не указана" // если появится в API — обновим
                    )
                })

                studentAdapter.submitList(students.toList())
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки учеников: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
