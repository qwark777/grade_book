package com.example.one

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.one.databinding.RatingFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class DropdownItem {
    data class Header(val title: String) : DropdownItem()
    data class Option(val label: String, val type: String) : DropdownItem()
}

class RatingFragment : Fragment() {

    private lateinit var subjectRecycler: RecyclerView
    private lateinit var ratingRecycler: RecyclerView

    private lateinit var subjectAdapter: SubjectAdapter
    private lateinit var ratingAdapter: RatingAdapter

    private val subjects = listOf("Математика", "Физика", "Русский", "Информатика", "Химия")
    private var currentSubjectIndex = 0

    private var _binding: RatingFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RatingFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subjectRecycler = binding.subjectRecycler
        ratingRecycler = binding.ratingRecycler

        subjectAdapter = SubjectAdapter(subjects) { index ->
            currentSubjectIndex = index
            loadRatings(subjects[index])
        }

        subjectRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        subjectRecycler.adapter = subjectAdapter

        ratingAdapter = RatingAdapter()
        ratingRecycler.layoutManager = LinearLayoutManager(requireContext())
        ratingRecycler.adapter = ratingAdapter

        // Загрузим рейтинг для первого предмета
        loadRatings(subjects[currentSubjectIndex])

        val dropdownOptions = listOf(
            "Математика", "Физика", "Русский", "Информатика", "Химия",
            "Кафедра Естественных наук", "Кафедра Гуманитарных",
            "Дисциплина", "Активность"
        )

        val dropdownAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, dropdownOptions)
        binding.ratingDropdown.setAdapter(dropdownAdapter)

        binding.ratingDropdown.setOnItemClickListener { _, _, position, _ ->
            val selected = dropdownOptions[position]
            loadRatings(selected)
        }
    }

    private fun loadRatings(subject: String) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                listOf(
                    RatingItem(1, "Иванов И.И.", 96),
                    RatingItem(2, "Петрова А.А.", 92),
                    RatingItem(3, "Смирнов Д.С.", 91),
                    RatingItem(4, "Сидоров Б.Б.", 87),
                    RatingItem(5, "Кузнецова Е.Ю.", 85)
                )
            }
            ratingAdapter.submitList(result)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
