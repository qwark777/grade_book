package com.example.one

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.one.databinding.FragmentPerformanceAttendanceBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class PerformanceAttendanceFragment : Fragment() {

    private lateinit var binding: FragmentPerformanceAttendanceBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPerformanceAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // демо-данные
        val pieEntries = listOf(
            PieEntry(92f, "Присутств."),
            PieEntry(6f, "Опоздал"),
            PieEntry(2f, "Отсутств.")
        )
        val set = PieDataSet(pieEntries, "").apply { valueTextSize = 12f }
        binding.pieAttendance.data = PieData(set)
        binding.pieAttendance.setUsePercentValues(true)
        binding.pieAttendance.description.isEnabled = false
        binding.pieAttendance.centerText = "Посещаемость"
        binding.pieAttendance.invalidate()

        // простой мок тепловой карты — просто 7×5 сетка с кружочками-статусами
        binding.tvHeatmapLegend.text = "Тепловая карта (демо)"
    }
}
