package com.example.one

import android.R
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
import com.example.one.databinding.RatingFragmentBinding
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RatingFragment : Fragment() {

    private var _binding: RatingFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var ratingAdapter: RatingAdapter
    private lateinit var apiManager: ApiManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RatingFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        apiManager = ApiManager(requireContext())

        ratingAdapter = RatingAdapter()
        binding.ratingRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.ratingRecycler.adapter = ratingAdapter

        // Показываем фильтр
        binding.ratingDropdown.visibility = View.VISIBLE

        // Пример: просто добавим один пункт "Общий рейтинг"
        val options = listOf("Общий рейтинг")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, options)
        binding.ratingDropdown.setAdapter(adapter)

        binding.ratingDropdown.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> loadGlobalRatings()
            }
        }

        // По умолчанию — сразу загружаем
        binding.ratingDropdown.setText("Общий рейтинг", false)
        loadGlobalRatings()
    }


    private fun loadGlobalRatings() {
        lifecycleScope.launch {
            val students = withContext(Dispatchers.IO) {
                apiManager.getAllStudentScores()
            }

            val result = students.mapNotNull { student ->
                val grades = student.grades
                if (grades.isEmpty()) return@mapNotNull null

                val total = grades.sumOf { it.value }
                RatingItem(
                    place = 0, // потом обновим
                    name = student.student_name,
                    average = total // здесь теперь сумма, а не среднее
                )
            }.sortedByDescending { it.average }

            val ranked = result.mapIndexed { index, item ->
                item.copy(place = index + 1)
            }

            ratingAdapter.submitList(ranked)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
