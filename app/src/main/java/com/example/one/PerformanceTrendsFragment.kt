package com.example.one

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import com.example.one.databinding.FragmentPerformanceTrendsBinding
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.roundToInt
import androidx.core.graphics.toColorInt

class PerformanceTrendsFragment : Fragment() {

    private lateinit var binding: FragmentPerformanceTrendsBinding
    private var panAnimator: ValueAnimator? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPerformanceTrendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // демо-данные
        val dates  = listOf("01.09","08.09","15.09","22.09","29.09","06.10","13.10","20.10","27.10")
        val points = listOf(1.0f, 4.2f, 4.3f, 4.5f, 4.6f, 3.8f, 3.2f, 2.9f, 3.4f)
        val entries = points.mapIndexed { i, v -> Entry(i.toFloat(), v) }

        // стиль линии/точек
        val set = LineDataSet(entries, null).apply {
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            cubicIntensity = 0.25f
            lineWidth = 3.5f
            color = Color.TRANSPARENT          // саму линию рисуем градиентом в кастомном рендерере

            // точки
            setDrawCircles(true)
            circleRadius = 5f
            setCircleColor("#14B8A6".toColorInt())
            setDrawCircleHole(true)
            circleHoleRadius = 2.4f
            circleHoleColor = Color.WHITE

            // значения НАД точками отключаем
            setDrawValues(false)

            setDrawFilled(false)
            isHighlightEnabled = false
        }

        binding.chartTrends.apply {
            data = LineData(set)

            // градиентный рендерер со сглаживанием
            renderer = GradientBezierLineRenderer(this, animator, viewPortHandler)

            // жесты/скролл
            setScaleEnabled(true)
            setScaleXEnabled(true)
            setScaleYEnabled(false)
            isDragEnabled = true
            setPinchZoom(true)
            isDoubleTapToZoomEnabled = true
            setDragDecelerationEnabled(true)
            setDragDecelerationFrictionCoef(0.92f)
            setVisibleXRangeMaximum(4.3f)

            // отступы, чтобы не резались подписи и правая точка
            setViewPortOffsets(64f, 40f, 72f, 48f) // ← увеличили слева
            extraRightOffset = 36f

            // ось X — даты снизу
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dates)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textSize = 12f
                textColor = "#6B7280".toColorInt()
                setDrawGridLines(false)
                setDrawAxisLine(false)
            }

            // ось Y — 0..5, шаг 1, 6 меток
            axisLeft.apply {
                isEnabled = true
                axisMinimum = 0f
                axisMaximum = 5f

                setLabelCount(6, true)    // 0,1,2,3,4,5
                granularity = 1f
                isGranularityEnabled = true

                textSize = 12f
                textColor = "#6B7280".toColorInt()

                setDrawGridLines(true)
                gridColor = "#E5E7EB".toColorInt()
                gridLineWidth = 0.8f
                enableGridDashedLine(8f, 8f, 0f)

                // линию оси прячем, чтобы чище
                axisLineColor = Color.TRANSPARENT

                // показываем целые значения
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String = value.toInt().toString()
                }
            }
            axisRight.isEnabled = false

            legend.isEnabled = false
            description.isEnabled = false

            // края отдаем родителю (перелистывание вкладок), середина — скролл графика
            setOnTouchListener { v, event ->
                val w = v.width
                val edgePx = (w * 0.12f).roundToInt()
                val inEdge = event.x.toInt() <= edgePx || event.x.toInt() >= w - edgePx
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                        v.parent.requestDisallowInterceptTouchEvent(!inEdge)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                        v.parent.requestDisallowInterceptTouchEvent(false)
                }
                false
            }

            invalidate()
        }

        // показать правый край и анимировать появление
        binding.chartTrends.post {
            val endX = binding.chartTrends.data.xMax
            binding.chartTrends.moveViewToX(endX)
            binding.chartTrends.animateY(900, Easing.EaseOutCubic)
            binding.chartTrends.postDelayed({ runRightEdgePeek() }, 1000)
        }
    }

    /** Лёгкий «подсмотр» влево и возврат — хинт, что график скроллится */
    private fun runRightEdgePeek() {
        val chart = binding.chartTrends
        val startX = chart.lowestVisibleX.takeIf { it.isFinite() } ?: 0f
        val visible = (chart.highestVisibleX - chart.lowestVisibleX).takeIf { it.isFinite() && it > 0f } ?: 4f
        val leftX = (startX - (visible * 0.5f).coerceAtLeast(0.8f)).coerceAtLeast(0f)

        panAnimator?.cancel()

        val goLeft = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 260L
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                chart.moveViewToX(startX + (leftX - startX) * (va.animatedValue as Float))
                chart.invalidate()
            }
        }
        val goRight = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 260L
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                chart.moveViewToX(leftX + (startX - leftX) * (va.animatedValue as Float))
                chart.invalidate()
            }
        }
        goLeft.addListener(onEnd = { chart.postDelayed({ goRight.start() }, 160L) })
        panAnimator = goLeft
        goLeft.start()
    }

    override fun onDestroyView() {
        panAnimator?.cancel()
        super.onDestroyView()
    }
}

/** Кастомный рендерер: сглаженная линия (bezier) с вертикальным градиентом и поддержкой анимации phaseY */
class GradientBezierLineRenderer(
    chart: LineChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineChartRenderer(chart, animator, viewPortHandler) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isDither = true
    }

    override fun drawData(c: Canvas) {
        val ld = mChart.lineData ?: return
        for (i in 0 until ld.dataSetCount) {
            val ds = ld.getDataSetByIndex(i)
            if (ds.isVisible && ds is LineDataSet) drawLine(c, ds)
        }
    }

    private fun drawLine(c: Canvas, ds: LineDataSet) {
        val trans = mChart.getTransformer(ds.axisDependency)
        if (ds.entryCount < 2) return

        val path = Path()
        val p = FloatArray(2)
        val pp = FloatArray(2)

        val phaseY = mAnimator.phaseY

        for (i in 0 until ds.entryCount) {
            val e = ds.getEntryForIndex(i)
            p[0] = e.x
            p[1] = e.y * phaseY
            trans.pointValuesToPixel(p)

            if (i == 0) {
                path.moveTo(p[0], p[1])
            } else {
                val prev = ds.getEntryForIndex(i - 1)
                pp[0] = prev.x
                pp[1] = prev.y * phaseY
                trans.pointValuesToPixel(pp)

                val midX = (pp[0] + p[0]) / 2f
                // кубические сегменты для гладкости (как HORIZONTAL_BEZIER)
                path.cubicTo(midX, pp[1], midX, p[1], p[0], p[1])
            }
        }

        // вертикальный градиент: зелёный → жёлтый → красный
        val shader = LinearGradient(
            0f, mViewPortHandler.contentTop(),
            0f, mViewPortHandler.contentBottom(),
            intArrayOf(
                "#22C55E".toColorInt(),
                "#FACC15".toColorInt(),
                "#EF4444".toColorInt()
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        paint.strokeWidth = ds.lineWidth
        c.drawPath(path, paint)
    }
}
