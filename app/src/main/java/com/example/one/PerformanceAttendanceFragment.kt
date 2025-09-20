package com.example.one

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.one.databinding.FragmentPerformanceAttendanceBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlin.math.roundToInt

class PerformanceAttendanceFragment : Fragment() {

    private lateinit var binding: FragmentPerformanceAttendanceBinding

    // Подставь реальные данные:
    private val totalLessons = 50 // всего уроков в периоде (для абсолютов в маркере)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPerformanceAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupAttendancePie()
        setupHeatmapDemo()
    }

    /** Круговая: посещаемость (%) + MarkerView показывает абсолют из totalLessons */
    private fun setupAttendancePie() {
        // демо-расклад (в процентах, суммарно 100)
        val entries = listOf(
            PieEntry(92f, "Присутств."),
            PieEntry(6f,  "Опоздал"),
            PieEntry(2f,  "Отсутств.")
        )

        val set = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#4CAF50"), // присутств.
                Color.parseColor("#FFC107"), // опоздал
                Color.parseColor("#F44336")  // отсутств.
            )
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            sliceSpace = 2f

            // выносные подписи с «усиками»
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1OffsetPercentage = 75f
            valueLinePart1Length = 0.42f
            valueLinePart2Length = 0.34f
            valueLineWidth = 2f
            valueLineColor = Color.DKGRAY
            selectionShift = 6f
        }

        binding.pieAttendance.apply {
            setExtraOffsets(8f, 12f, 8f, 16f)
            setMinOffset(8f)

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
            legend.yOffset = 8f

            setHoleColor(Color.WHITE)
            centerText = "Посещаемость"
            setCenterTextSize(13f)
            description.isEnabled = false

            marker = object : MarkerView(requireContext(), R.layout.marker_attendance) {
                private val title by lazy { findViewById<TextView>(R.id.tvTitle) }
                private val value by lazy { findViewById<TextView>(R.id.tvValue) }
                override fun refreshContent(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                    if (e is PieEntry) {
                        val percent = e.value
                        val absolute = (totalLessons * percent / 100f).roundToInt()
                        title.text = e.label
                        value.text = "$absolute из $totalLessons (${String.format("%.1f", percent)}%)"
                    }
                    super.refreshContent(e, h)
                }
                override fun getOffset(): MPPointF =
                    MPPointF(-(width / 2f), -height.toFloat() + dp(4f))
            }
            setDrawMarkers(true)

            animateY(700)
            invalidate()
        }
    }

    /** Простая тепловая карта: 5 недель × 7 дней (кружочки статусов) */
    private fun setupHeatmapDemo() {
        // Легенда под заголовком
        binding.tvHeatmapLegend.text = "Тепловая карта (5 недель × 7 дней)"

        // Палитра статусов
        val colPresent = Color.parseColor("#4CAF50")
        val colLate    = Color.parseColor("#FFC107")
        val colAbsent  = Color.parseColor("#F44336")
        val colNone    = Color.parseColor("#CFD8DC") // пусто/нет пары

        // Демка данных: 5 недель по 7 дней
        // 0 — нет пары, 1 — присутств., 2 — опоздал, 3 — отсутств.
        val weeks = 5
        val days = 7
        val demoData: List<List<Int>> = List(weeks) { w ->
            List(days) { d ->
                when {
                    d == 0 || d == 6 -> 0            // выходные пустые
                    (w + d) % 9 == 0 -> 3            // иногда не был
                    (w + d) % 5 == 0 -> 2            // иногда опоздал
                    else -> 1                        // чаще присутствовал
                }
            }
        }

        // Грид 7 (строк) × 5 (столбцов), чтобы визуально шли сверху-вниз по дням
        val grid = binding.heatmapGrid
        grid.columnCount = weeks
        grid.rowCount = days
        grid.removeAllViews()

        // заголовки дней слева — можно добавить, если есть место (пн..вс)
        val dayNames = arrayOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс")

        // Заполняем
        for (row in 0 until days) {
            for (col in 0 until weeks) {
                val status = demoData[col][row]
                val v = View(requireContext()).apply {
                    val size = dp(18f).toInt()
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = size
                        height = size
                        setMargins(dp(6f).toInt(), dp(6f).toInt(), dp(6f).toInt(), dp(6f).toInt())
                        columnSpec = GridLayout.spec(col)
                        rowSpec = GridLayout.spec(row)
                    }
                    background = requireContext().getDrawable(R.drawable.bg_heatmap_dot)
                    // Тинтим фон кружка нужным цветом
                    background?.setTint(
                        when (status) {
                            1 -> colPresent
                            2 -> colLate
                            3 -> colAbsent
                            else -> colNone
                        }
                    )
                }
                grid.addView(v)
            }
        }
    }

    private fun dp(v: Float): Float =
        v * resources.displayMetrics.density
}
