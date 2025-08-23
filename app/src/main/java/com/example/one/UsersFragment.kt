package com.example.one.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.one.AdminMainLayout
import com.example.one.R
import com.example.one.StudentData
import com.example.one.TeacherData
import com.example.one.adapters.StudentAdapter
import com.example.one.adapters.TeacherAdapter
import com.example.one.databinding.FragmentUsersBinding
import com.example.one.dialogs.AddTeacherDialogFragment
import com.example.one.ui.messages.MessageFragment
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    private lateinit var teacherAdapter: TeacherAdapter
    private lateinit var studentAdapter: StudentAdapter

    private lateinit var apiManager: ApiManager
    private val students = mutableListOf<StudentData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        apiManager = ApiManager(requireContext())

        // Адаптеры
        teacherAdapter = TeacherAdapter { teacher ->
            (requireActivity() as AdminMainLayout).openChat(teacher.id)
        }
        studentAdapter = StudentAdapter { student ->
            (requireActivity() as AdminMainLayout).openChat(student.id)
        }

        // Один RecyclerView на оба списка
        binding.teacherRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = teacherAdapter
        }

        // Изначально — преподаватели
        loadTeachers()

        // Переключение радиокнопок
        binding.switchRoleGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.teachersRadioButton -> {
                    binding.teacherRecyclerView.adapter = teacherAdapter
                    loadTeachers(binding.searchEditText.text.toString())
                    binding.addTeacherButton.text = "Добавить преподавателя"
                }
                R.id.studentsRadioButton -> {
                    binding.teacherRecyclerView.adapter = studentAdapter
                    loadStudents(binding.searchEditText.text.toString())
                    binding.addTeacherButton.text = "Добавить ученика"
                }
            }
        }

        // Поиск
        binding.searchButton.setOnClickListener {
            when (binding.switchRoleGroup.checkedRadioButtonId) {
                R.id.teachersRadioButton -> loadTeachers(binding.searchEditText.text.toString())
                R.id.studentsRadioButton -> loadStudents(binding.searchEditText.text.toString())
            }
        }

        // Добавить
        binding.addTeacherButton.setOnClickListener {
            val isTeacherSelected = binding.teachersRadioButton.isChecked
            if (isTeacherSelected) {
                AddTeacherDialogFragment.newInstance { loadTeachers(binding.searchEditText.text.toString()) }
                    .show(parentFragmentManager, "AddTeacherDialog")
            } else {
                Toast.makeText(requireContext(), "Добавление ученика пока не реализовано", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openChat(userId: Int) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, com.example.one.ui.messages.MessageFragment.newInstance(userId))
            .addToBackStack("chat_$userId")
            .commit()
    }

    private fun loadTeachers(include: String = "") {
        lifecycleScope.launch {
            try {
                val serverTeachers = withContext(Dispatchers.IO) {
                    apiManager.getAllTeachers(include_string = include)
                }
                val teachers = serverTeachers.map { t ->
                    TeacherData(
                        id = t.id,
                        fullName = t.full_name ?: "Неизвестно",
                        workPlace = t.work_place ?: "Не указано",
                        location = t.location ?: "Не указано",
                        subject = t.subject ?: "Предмет не указан",
                        classes = t.classes ?: "Классы не указаны",
                        username = t.username ?: "",
                        password = t.password ?: ""
                    )
                }
                teacherAdapter.submitList(teachers)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки преподавателей: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadStudents(include: String = "") {
        lifecycleScope.launch {
            try {
                val serverStudents = withContext(Dispatchers.IO) {
                    apiManager.getAllStudents(include_string = include)
                }
                students.clear()
                students.addAll(serverStudents.map { s ->
                    StudentData(
                        id = s.id,
                        fullName = s.full_name ?: "Неизвестно",
                        className = s.class_name ?: "Не указан",
                        school = "Школа не указана"
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
