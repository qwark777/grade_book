package com.example.one.ui.timetable

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.one.databinding.ItemDayPageBinding

/**
 * Горизонтальный адаптер-«пейджер»: одна страница = один день расписания.
 * Внутри страницы — вертикальный список из заголовка дня + уроков/пустого дня
 * на базе уже существующего [TimetableAdapter].
 */
class DayPagerAdapter : ListAdapter<DaySchedule, DayPagerAdapter.DayPageVH>(Diff) {

    // Ширина страницы (для «подгляда» соседних страниц). Если null — MATCH_PARENT.
    var pageWidthPx: Int? = null
    // Насколько следующая страница должна выглядывать (реализуется отрицательным отступом).
    var sidePeekPx: Int = 0
    // Фактический padding у RecyclerView по краям, нужен чтобы «съесть» лишний отступ у первого/последнего
    var edgePaddingPx: Int = 0
    // Желаемый внешний зазор у первого/последнего дня от края экрана
    var edgeGapPx: Int = 0

    private object Diff : DiffUtil.ItemCallback<DaySchedule>() {
        override fun areItemsTheSame(oldItem: DaySchedule, newItem: DaySchedule): Boolean =
            oldItem.date == newItem.date

        override fun areContentsTheSame(oldItem: DaySchedule, newItem: DaySchedule): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayPageVH {
        val binding = ItemDayPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayPageVH(binding)
    }

    override fun onBindViewHolder(holder: DayPageVH, position: Int) {
        pageWidthPx?.let { w ->
            val lp = holder.itemView.layoutParams
            if (lp != null) {
                lp.width = w
                holder.itemView.layoutParams = lp
            }
        }
        holder.bind(getItem(position))
        (holder.itemView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
            val isFirst = position == 0
            val isLast = position == itemCount - 1
            val innerGapPx = (holder.itemView.resources.displayMetrics.density * 2).toInt() // ~2dp между страницами

            // Внешние края: хотим оставить ровно edgeGapPx, поэтому съедаем padding
            val firstOuterStart = -(edgePaddingPx - edgeGapPx).coerceAtLeast(0)
            val lastOuterEnd = -(edgePaddingPx - edgeGapPx).coerceAtLeast(0)

            lp.marginStart = when {
                isFirst -> firstOuterStart
                else -> -sidePeekPx + innerGapPx
            }
            lp.marginEnd = when {
                isLast -> lastOuterEnd
                else -> -sidePeekPx + innerGapPx
            }
            holder.itemView.layoutParams = lp
        }
    }

    inner class DayPageVH(private val binding: ItemDayPageBinding) : RecyclerView.ViewHolder(binding.root) {
        private val innerAdapter = TimetableAdapter()

        init {
            binding.dayRecycler.layoutManager = LinearLayoutManager(binding.root.context)
            binding.dayRecycler.adapter = innerAdapter
            binding.dayRecycler.setHasFixedSize(false)
        }

        fun bind(day: DaySchedule) {
            innerAdapter.submitList(buildItemsForDay(day))
        }

        private fun buildItemsForDay(d: DaySchedule): List<TimetableItem> {
            val items = ArrayList<TimetableItem>(8)
            items += TimetableItem.Header(
                dateStr = d.date,
                weekCode = d.week_code,
                holiday = d.holiday,
                holidayReason = d.holidayReason
            )
            if (d.holiday) {
                items += TimetableItem.EmptyDay(d.date, d.holidayReason)
                return items
            }
            if (d.lessons.isEmpty()) {
                items += TimetableItem.EmptyDay(d.date, null)
                return items
            }
            d.lessons.forEach { l ->
                items += TimetableItem.Lesson(d.date, l)
            }
            return items
        }
    }
}


