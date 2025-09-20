package com.example.one

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.example.one.databinding.FragmentSubjectDetailsBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*

class SubjectDetailsFragment : Fragment() {

    private lateinit var binding: FragmentSubjectDetailsBinding
    private val subjectName: String by lazy { requireArguments().getString(ARG_SUBJECT) ?: "Предмет" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSubjectDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTitle.text = subjectName

        setupTrendChart()
        setupGradesPie()
    }

    private fun setupTrendChart() {
        // демо-данные
        val dates = listOf("01.09","08.09","15.09","22.09","29.09")
        val points = listOf(4.0f, 4.2f, 4.3f, 4.6f, 4.8f)

        val target = points.mapIndexed { i, v -> Entry(i.toFloat(), v) }
        val anim = points.mapIndexed { i, _ -> Entry(i.toFloat(), 0f) }

        val set = LineDataSet(anim, null).apply {
            mode = LineDataSet.Mode.LINEAR
            color = Color.parseColor("#14B8A6")
            lineWidth = 3f
            setDrawValues(false)
            setDrawFilled(false)
            setDrawCircles(true)
            circleRadius = 4.5f
            setCircleColor(Color.parseColor("#14B8A6"))
            setDrawCircleHole(true)
            circleHoleRadius = 2.2f
            circleHoleColor = Color.WHITE
            isHighlightEnabled = false
        }

        binding.chartTrend.apply {
            data = LineData(set)
            setViewPortOffsets(56f, 24f, 24f, 40f)

            xAxis.apply {
                valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(dates)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textSize = 12f
                setDrawGridLines(false)
                axisLineColor = Color.parseColor("#D1D5DB")
            }
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 5f
                granularity = 1f
                textSize = 12f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E5E7EB")
                axisLineColor = Color.parseColor("#D1D5DB")
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            invalidate()
        }

        // двухфазная анимация: «плита» вверх, затем растяжка
        val baseY = points.minOrNull() ?: 0f
        val phase1 = 0.5f
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1400L
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                val t = (va.animatedValue as Float).coerceIn(0f, 1f)
                for (i in anim.indices) {
                    val targetY = target[i].y
                    val y = if (t <= phase1) {
                        baseY * (t / phase1)
                    } else {
                        val k = (t - phase1) / (1f - phase1)
                        baseY + (targetY - baseY) * k
                    }
                    anim[i].y = y
                }
                set.notifyDataSetChanged()
                binding.chartTrend.data.notifyDataChanged()
                binding.chartTrend.notifyDataSetChanged()
                binding.chartTrend.invalidate()
            }
            start()
        }
    }

    private fun setupGradesPie() {
        // демо-данные распределения оценок по предмету
        val e2 = 1f; val e3 = 6f; val e4 = 15f; val e5 = 20f
        val total = (e2 + e3 + e4 + e5).toInt()

        val entries = listOf(
            PieEntry(e2, "2"),
            PieEntry(e3, "3"),
            PieEntry(e4, "4"),
            PieEntry(e5, "5")
        )
        val set = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#F44336"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#4CAF50")
            )
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            sliceSpace = 2f
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1OffsetPercentage = 80f
            valueLinePart1Length = 0.45f
            valueLinePart2Length = 0.38f
            valueLineColor = Color.DKGRAY
            selectionShift = 6f
        }

        binding.chartGrades.apply {
            data = PieData(set).also { pd ->
                setUsePercentValues(true)
                pd.setValueFormatter(com.github.mikephil.charting.formatter.PercentFormatter(this))
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
            centerText = "Всего: $total"
            setCenterTextSize(13f)
            description.isEnabled = false
            setExtraOffsets(8f, 12f, 8f, 12f)
            setMinOffset(8f)
            animateY(700)
            invalidate()
        }
    }

    companion object {
        private const val ARG_SUBJECT = "arg_subject"
        fun newInstance(subjectName: String) = SubjectDetailsFragment().apply {
            arguments = Bundle().apply { putString(ARG_SUBJECT, subjectName) }
        }
    }
}
