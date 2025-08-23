package com.example.one

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.one.databinding.FragmentPerformanceTrendsBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class PerformanceTrendsFragment : Fragment() {

    private lateinit var binding: FragmentPerformanceTrendsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPerformanceTrendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dates = listOf("01.09","08.09","15.09","22.09","29.09")
        val points = listOf(4.0f, 4.2f, 4.3f, 4.5f, 4.6f)

        val entries = points.mapIndexed { i, v -> Entry(i.toFloat(), v) }
        val set = LineDataSet(entries, "Средний балл").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircles(true)
            lineWidth = 2f
            valueTextSize = 10f
        }

        binding.chartTrends.data = LineData(set)
        binding.chartTrends.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
        binding.chartTrends.xAxis.granularity = 1f
        binding.chartTrends.axisLeft.axisMinimum = 0f
        binding.chartTrends.axisLeft.axisMaximum = 5f
        binding.chartTrends.axisRight.isEnabled = false
        binding.chartTrends.description.isEnabled = false
        binding.chartTrends.invalidate()
    }
}
