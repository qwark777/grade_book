package com.example.one

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class AttendanceMarkerView(context: Context) : MarkerView(context, R.layout.marker_attendance) {

    private val tvTitle: TextView = findViewById(R.id.tvTitle)
    private val tvValue: TextView = findViewById(R.id.tvValue)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e is PieEntry) {
            tvTitle.text = e.label
            // e.y — значение сектора (проценты, если включён PercentFormatter в чарте)
            tvValue.text = "${"%.1f".format(e.value)}%"
        }
        super.refreshContent(e, highlight)
    }

    // смещение, чтобы карточка появлялась над точкой и по центру
    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat() - 8f)
    }
}
