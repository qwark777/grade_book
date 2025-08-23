package com.example.one.ui.timetable

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.one.R
import com.example.one.databinding.ItemDayEmptyBinding
import com.example.one.databinding.ItemDayHeaderBinding
import com.example.one.databinding.ItemLessonBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------- МОДЕЛИ UI ----------
data class DaySchedule(
    val date: String,            // "yyyy-MM-dd"
    val week_code: String,       // "A" | "B"
    val holiday: Boolean,
    val lessons: List<LessonItem>,
    val version: Long,
    val holidayReason: String? = null
)

data class LessonItem(
    val lesson_idx: Int?,        // может быть null для insert
    val starts_at: String,       // "HH:mm"
    val ends_at: String,         // "HH:mm"
    val subject: String,
    val teacher: SimpleRef?,
    val room: SimpleRef?,
    val change: ChangeBadge?     // null если без изменений
)

data class SimpleRef(val id: Int, val name: String)

data class ChangeBadge(
    val type: String,            // cancel|replace|insert|move|room|teacher
    val note: String? = null
)

// ---------- ЭЛЕМЕНТЫ СПИСКА ----------
sealed class TimetableItem {
    data class Header(
        val dateStr: String,         // "yyyy-MM-dd"
        val weekCode: String,
        val holiday: Boolean,
        val holidayReason: String?
    ) : TimetableItem()

    data class Lesson(
        val dateStr: String,
        val lesson: LessonItem
    ) : TimetableItem()

    data class EmptyDay(
        val dateStr: String,
        val reason: String?
    ) : TimetableItem()
}

// ---------- АДАПТЕР ----------
class TimetableAdapter : ListAdapter<TimetableItem, RecyclerView.ViewHolder>(Diff) {

    companion object {
        private const val VT_HEADER = 0
        private const val VT_LESSON = 1
        private const val VT_EMPTY = 2

        private val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val outFmt = SimpleDateFormat("EEE, dd MMM", Locale("ru"))

        private object Diff : DiffUtil.ItemCallback<TimetableItem>() {
            override fun areItemsTheSame(old: TimetableItem, new: TimetableItem): Boolean {
                return when {
                    old is TimetableItem.Header && new is TimetableItem.Header ->
                        old.dateStr == new.dateStr
                    old is TimetableItem.EmptyDay && new is TimetableItem.EmptyDay ->
                        old.dateStr == new.dateStr
                    old is TimetableItem.Lesson && new is TimetableItem.Lesson ->
                        old.dateStr == new.dateStr &&
                                old.lesson.starts_at == new.lesson.starts_at &&
                                old.lesson.subject == new.lesson.subject
                    else -> false
                }
            }

            override fun areContentsTheSame(old: TimetableItem, new: TimetableItem): Boolean = old == new
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is TimetableItem.Header -> VT_HEADER
        is TimetableItem.Lesson -> VT_LESSON
        is TimetableItem.EmptyDay -> VT_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_HEADER -> HeaderVH(ItemDayHeaderBinding.inflate(inf, parent, false))
            VT_LESSON -> LessonVH(ItemLessonBinding.inflate(inf, parent, false))
            VT_EMPTY  -> EmptyVH(ItemDayEmptyBinding.inflate(inf, parent, false))
            else -> error("unknown viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TimetableItem.Header -> (holder as HeaderVH).bind(item)
            is TimetableItem.Lesson -> (holder as LessonVH).bind(item)
            is TimetableItem.EmptyDay -> (holder as EmptyVH).bind(item)
        }
    }

    // ---------- VH: Header ----------
    inner class HeaderVH(private val b: ItemDayHeaderBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(it: TimetableItem.Header) {
            b.textWeekCode.text = it.weekCode
            b.textDate.text = formatDate(it.dateStr)
                .replaceFirstChar { c -> c.titlecase(Locale("ru")) }
            if (it.holiday) {
                b.textHoliday.visibility = View.VISIBLE
                b.textHoliday.text = it.holidayReason ?: "Выходной"
            } else {
                b.textHoliday.visibility = View.GONE
            }
        }

        private fun formatDate(s: String): String {
            return try {
                val d: Date = inFmt.parse(s)!!
                outFmt.format(d)
            } catch (_: Exception) {
                s
            }
        }
    }

    // ---------- VH: Empty ----------
    inner class EmptyVH(private val b: ItemDayEmptyBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(it: TimetableItem.EmptyDay) {
            b.textEmpty.text = "Занятий нет"
            b.textReason.text = it.reason ?: ""
        }
    }

    // ---------- VH: Lesson ----------
    inner class LessonVH(private val b: ItemLessonBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(it: TimetableItem.Lesson) {
            val l = it.lesson

            b.textTime.text = "${l.starts_at}–${l.ends_at}"
            b.textIndex.text = l.lesson_idx?.let { idx -> "• $idx урок" } ?: "• доп. занятие"

            b.textSubject.text = l.subject
            b.textTeacher.text = l.teacher?.name ?: ""
            if (l.room?.name.isNullOrBlank()) {
                b.textRoom.visibility = View.GONE
            } else {
                b.textRoom.visibility = View.VISIBLE
                b.textRoom.text = l.room!!.name
            }

            val isCanceled = l.change?.type == "cancel"
            b.textSubject.paintFlags =
                if (isCanceled) b.textSubject.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else b.textSubject.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            if (l.change == null) {
                b.changeContainer.visibility = View.GONE
            } else {
                b.changeContainer.visibility = View.VISIBLE
                b.textChangeType.text = changeLabel(l.change.type)
                b.textChangeNote.text = l.change.note ?: ""
                setChangeBadgeColor(b.textChangeType, l.change.type)
            }
        }

        private fun changeLabel(type: String): String = when (type) {
            "cancel"  -> "отменён"
            "replace" -> "замена"
            "insert"  -> "добавлен"
            "move"    -> "перенос"
            "room"    -> "кабинет"
            "teacher" -> "учитель"
            else      -> "изменение"
        }

        private fun setChangeBadgeColor(v: View, type: String) {
            @ColorInt val color = when (type) {
                "cancel"  -> ctxColor(v, android.R.color.holo_red_dark)
                "replace" -> ctxColor(v, android.R.color.holo_orange_dark)
                "insert"  -> ctxColor(v, android.R.color.holo_green_dark)
                "move"    -> ctxColor(v, android.R.color.holo_purple)
                "room",
                "teacher" -> ctxColor(v, android.R.color.holo_blue_dark)
                else      -> ctxColor(v, android.R.color.darker_gray)
            }
            (v.background)?.mutate()?.setTint(color)
        }

        private fun ctxColor(v: View, res: Int) = ContextCompat.getColor(v.context, res)
    }
}
