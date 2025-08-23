package com.example.one

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.one.adapters.RatingAdapter
import com.example.one.adapters.RatingItem
import com.example.one.adapters.SubjectChip
import com.example.one.adapters.SubjectChipAdapter
import com.example.one.databinding.RatingFragmentBinding
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RatingFragment : Fragment() {

    private var _binding: RatingFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var ratingAdapter: RatingAdapter
    private lateinit var subjectAdapter: SubjectChipAdapter
    private lateinit var apiManager: ApiManager

    // Группы предметов (заполни по своей школе)
    private val groups: Map<String, List<String>> = mapOf(
        "Все предметы" to listOf("Математика","Информатика","Русский","История","Английский","Физика","Химия","Биология","География","Литература"),
        "Естественные" to listOf("Биология","Химия","Физика","География"),
        "Точные" to listOf("Математика","Информатика","Физика"),
        "Гуманитарные" to listOf("История","Литература","Обществознание","Русский"),
        "Языки" to listOf("Английский","Немецкий","Французский","Русский")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = RatingFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        apiManager = ApiManager(requireContext())

        // Вертикальный список рейтинга
        ratingAdapter = RatingAdapter()
        binding.ratingRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.ratingRecycler.adapter = ratingAdapter

        // Горизонтальный список предметов
        subjectAdapter = SubjectChipAdapter { chip ->
            loadSubjectRating(chip.title)
        }
        binding.subjectRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = subjectAdapter
        }

        // Меню-группа
        val menuOptions = groups.keys.toList()
        val dropAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, menuOptions)
        binding.ratingDropdown.setAdapter(dropAdapter)

        binding.ratingDropdown.setOnItemClickListener { _, _, position, _ ->
            val groupName = menuOptions[position]
            showSubjectsForGroup(groupName)
        }

        // По умолчанию — первый пункт меню
        binding.ratingDropdown.setText(menuOptions.first(), false)
        showSubjectsForGroup(menuOptions.first())
    }

    /** Подставляет список предметов снизу и сразу грузит первый */
    private fun showSubjectsForGroup(groupName: String) {
        val subjects = groups[groupName].orEmpty()
        val chips = subjects.map { SubjectChip(it) }
        subjectAdapter.submitList(chips)
        subjectAdapter.selectFirstIfAny()
        // автозагрузка рейтинга по первому предмету
        subjects.firstOrNull()?.let { loadSubjectRating(it) }
    }

    /** Грузим рейтинг по конкретному предмету */
    private fun loadSubjectRating(subjectName: String) {
        lifecycleScope.launch {
            val list: List<StudentSubjectScore> = withContext(Dispatchers.IO) {
                apiManager.getScoresBySubject(subjectName)
            }
            // Переводим в твою модель RatingItem
            val ranked = list
                .map { s ->
                    val avg = (s.average_score ?: 0.0)
                    RatingItem(
                        place = 0,
                        name = s.student_name,
                        average = avg.toInt()  // если нужно именно среднее с округлением: avg.roundToInt()
                    )
                }
                .sortedByDescending { it.average }
                .mapIndexed { idx, it -> it.copy(place = idx + 1) }

            ratingAdapter.submitList(ranked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
