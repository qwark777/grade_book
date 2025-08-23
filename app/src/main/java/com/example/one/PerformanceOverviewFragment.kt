package com.example.one

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.one.databinding.FragmentPerformanceOverviewBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF

class PerformanceOverviewFragment : Fragment() {

    private lateinit var binding: FragmentPerformanceOverviewBinding

    // TODO: подставь реальные значения из API
    private val totalLessons = 50  // для посещаемости

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPerformanceOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvGpa.text = "Средний балл: 4.42"

        setupGradesPie()
        setupAttendancePie()
    }

    /** Круговая: распределение оценок — на секторах проценты, по тапу Marker с абсолютом */
    private fun setupGradesPie() {
        // демо-данные (замени на реальные суммарные количества оценок)
        val count2 = 3f
        val count3 = 7f
        val count4 = 18f
        val count5 = 22f
        val totalGrades = (count2 + count3 + count4 + count5).toInt()

        val entries = listOf(
            PieEntry(count2, "Оценка 2"),
            PieEntry(count3, "Оценка 3"),
            PieEntry(count4, "Оценка 4"),
            PieEntry(count5, "Оценка 5")
        )

        val set = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#F44336"), // 2 — красный
                Color.parseColor("#FF9800"), // 3 — оранжевый
                Color.parseColor("#2196F3"), // 4 — синий
                Color.parseColor("#4CAF50")  // 5 — зелёный
            )
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            sliceSpace = 2f

            // выносные подписи (проценты)
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1OffsetPercentage = 80f
            valueLinePart1Length = 0.45f
            valueLinePart2Length = 0.38f
            valueLineWidth = 2f
            valueLineColor = Color.DKGRAY
            selectionShift = 6f
        }

        binding.chartGradesPie.apply {
            // показываем ПРОЦЕНТ на диаграмме
            data = PieData(set).also { pd ->
                setUsePercentValues(true)
                pd.setValueFormatter(PercentFormatter(this))
                pd.setValueTextSize(12f)
                pd.setValueTextColor(Color.BLACK)
            }

            setDrawEntryLabels(false)

            legend.isEnabled = true
            legend.isWordWrapEnabled = true
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.orientation = Legend.LegendOrientation.HORIZONTAL

            setHoleColor(Color.WHITE)
            centerText = "Оценки: $totalGrades"
            setCenterTextSize(13f)
            description.isEnabled = false

            setExtraOffsets(8f, 12f, 8f, 12f)
            setMinOffset(8f)

            // MarkerView: по нажатию показываем АБСОЛЮТ + процент
            marker = object : MarkerView(requireContext(), R.layout.marker_attendance) {
                private val title by lazy { findViewById<android.widget.TextView>(R.id.tvTitle) }
                private val value by lazy { findViewById<android.widget.TextView>(R.id.tvValue) }
                override fun refreshContent(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                    if (e is PieEntry) {
                        val absolute = e.value.toInt() // тут значения — именно количество оценок
                        // посчитаем процент от общего числа оценок
                        val percent = if (totalGrades > 0) (absolute * 100f / totalGrades) else 0f
                        title.text = e.label
                        value.text = "$absolute из $totalGrades (${String.format("%.1f", percent)}%)"
                    }
                    super.refreshContent(e, h)
                }
                override fun getOffset(): MPPointF =
                    MPPointF(-(width / 2f), -height.toFloat() - 10f)
            }
            setDrawMarkers(true)

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                    postDelayed({ highlightValues(null) }, 2000) // авто-скрытие маркера
                }
                override fun onNothingSelected() {}
            })

            animateY(700)
            invalidate()
        }
    }

    /** Круговая: посещаемость (%) + MarkerView показывает абсолют из totalLessons */
    private fun setupAttendancePie() {
        val entries = listOf(
            PieEntry(90f, "Посетил"),
            PieEntry(5f,  "Опоздал"),
            PieEntry(5f,  "Не посетил")
        )

        val set = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#F44336")
            )
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            sliceSpace = 2f

            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1OffsetPercentage = 70f
            valueLinePart1Length = 0.30f
            valueLinePart2Length = 0.22f
            valueLineWidth = 2f
            valueLineColor = Color.DKGRAY
            selectionShift = 6f
        }

        binding.chartAttendance.apply {
            setExtraOffsets(8f, 12f, 8f, 16f)
            setMinOffset(8f)

            data = PieData(set).also { pd ->
                setUsePercentValues(true) // проценты на диаграмме
                pd.setValueFormatter(PercentFormatter(this))
                pd.setValueTextSize(12f)
                pd.setValueTextColor(Color.BLACK)
            }

            setDrawEntryLabels(false)

            legend.isEnabled = true
            legend.isWordWrapEnabled = true
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.yOffset = 8f

            setHoleColor(Color.WHITE)
            centerText = "Посещаемость"
            setCenterTextSize(13f)
            description.isEnabled = false

            marker = object : MarkerView(requireContext(), R.layout.marker_attendance) {
                private val title by lazy { findViewById<android.widget.TextView>(R.id.tvTitle) }
                private val value by lazy { findViewById<android.widget.TextView>(R.id.tvValue) }
                override fun refreshContent(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                    if (e is PieEntry) {
                        val percent = e.value
                        val absolute = (totalLessons * percent / 100f).toInt()
                        title.text = e.label
                        value.text = "$absolute из $totalLessons (${String.format("%.1f", percent)}%)"
                    }
                    super.refreshContent(e, h)
                }
                override fun getOffset(): MPPointF =
                    MPPointF(-(width / 2f), -height.toFloat() - 10f)
            }
            setDrawMarkers(true)

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                    postDelayed({ highlightValues(null) }, 2000)
                }
                override fun onNothingSelected() {}
            })

            animateY(700)
            invalidate()
        }
    }
}
