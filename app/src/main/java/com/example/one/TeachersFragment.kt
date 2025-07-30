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
    private val studentAdapter = StudentAdapter() // создай аналог TeacherAdapter

    private lateinit var apiManager: ApiManager
    private val students = mutableListOf<StudentData>()
    private val mockTeachers = listOf(
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

    private val mockStudents = listOf(
        StudentData("Алексей Сидоров", "8Б", "Москва"),
        StudentData("Мария Иванова", "9А", "Санкт-Петербург")
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

        apiManager = ApiManager(requireContext())
        // изначально показываем преподавателей
        binding.teacherRecyclerView.adapter = teacherAdapter
        teacherAdapter.submitList(mockTeachers)

        binding.switchRoleGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.teachersRadioButton -> {
                    binding.teacherRecyclerView.adapter = teacherAdapter
                    teacherAdapter.submitList(mockTeachers)
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
                    // Обновить список преподавателей
                }.show(parentFragmentManager, "AddTeacherDialog")
            } else {
                // здесь вызов диалога для добавления ученика (нужно реализовать)
            }
        }
    }

    private fun loadStudents() {
        lifecycleScope.launch {
            try {
                // Показываем индикатор загрузки (если есть)

                val serverStudents = withContext(Dispatchers.IO) {
                    apiManager.getAllStudents()
                }

                students.clear()
                students.addAll(serverStudents.map { student ->
                    StudentData(
                        fullName = student.full_name ?: "Неизвестно",
                        className =  "Не указан",
                        school = "Школа не указана"
                    )
                })

                // Обновляем UI в главном потоке
                studentAdapter.submitList(students.toList())
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
