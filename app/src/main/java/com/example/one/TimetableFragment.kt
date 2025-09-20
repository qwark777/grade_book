package com.example.one.ui.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.one.databinding.FragmentTimetableBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class TimetableFragment(
    private val classId: Int,
    private val initialDateMillis: Long = System.currentTimeMillis()
) : Fragment() {

    private var _binding: FragmentTimetableBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: DayPagerAdapter

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val DAY_MS = 24L * 3600 * 1000

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTimetableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка ViewPager2 для peek-эффекта
        val vp = binding.viewPager
        vp.offscreenPageLimit = 1
        vp.clipToPadding = false
        vp.clipChildren = false

        val sidePeek = dp(14)           // сколько «видно» соседней страницы
        val pageMargin = dp(12)

        // Добавим внутренние паддинги, чтобы были видны края соседей
        vp.setPadding(sidePeek, 0, sidePeek, 0)

        // Смещение страниц (задаёт промежуток между ними)
        vp.setPageTransformer { page, position ->
            page.translationX = position * (-pageMargin).toFloat()
        }

        // Верхние чипы
        binding.chipToday?.setOnClickListener { loadWeek(System.currentTimeMillis()) }
        binding.chipWeekA?.setOnClickListener { loadWeek(weekStartMillis(System.currentTimeMillis())) }
        binding.chipWeekB?.setOnClickListener { loadWeek(weekStartMillis(System.currentTimeMillis()) + 7L * DAY_MS) }

        loadWeek(initialDateMillis)
    }

    private fun loadWeek(anyDateMillis: Long) {
        lifecycleScope.launch {
            try {
                binding.viewPager.isVisible = false

                // TODO: заменить на реальный вызов API
                val days = withContext(Dispatchers.IO) { mockWeek(anyDateMillis) }

                pagerAdapter = DayPagerAdapter(days)
                binding.viewPager.adapter = pagerAdapter

                // Прокрутка к сегодняшнему дню
                val todayStr = dateFmt.format(System.currentTimeMillis())
                val idx = days.indexOfFirst { it.date == todayStr }
                if (idx >= 0) binding.viewPager.setCurrentItem(idx, false)

                binding.viewPager.isVisible = true

            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Не удалось загрузить расписание", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Pager: каждая страница — один день, внутри вертикальный список TimetableAdapter */
    private inner class DayPagerAdapter(
        private val days: List<DaySchedule>
    ) : RecyclerView.Adapter<DayPagerAdapter.DayVH>() {

        inner class DayVH(val recycler: RecyclerView) : RecyclerView.ViewHolder(recycler)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayVH {
            val rv = RecyclerView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                clipToPadding = false
                setPadding(dp(8), dp(8), dp(8), dp(12))
                layoutManager = LinearLayoutManager(parent.context)
            }
            return DayVH(rv)
        }

        override fun getItemCount(): Int = days.size

        override fun onBindViewHolder(holder: DayVH, position: Int) {
            val day = days[position]
            val items = buildItems(listOf(day))
            val adapter = TimetableAdapter()
            holder.recycler.adapter = adapter
            adapter.submitList(items)
        }
    }

    private fun buildItems(days: List<DaySchedule>): List<TimetableItem> {
        val out = ArrayList<TimetableItem>(days.size * 4)
        for (d in days) {
            out += TimetableItem.Header(
                dateStr = d.date,
                weekCode = d.week_code,
                holiday = d.holiday,
                holidayReason = d.holidayReason
            )
            if (d.holiday) {
                out += TimetableItem.EmptyDay(d.date, d.holidayReason)
                continue
            }
            if (d.lessons.isEmpty()) {
                out += TimetableItem.EmptyDay(d.date, null)
                continue
            }
            d.lessons.forEach { l -> out += TimetableItem.Lesson(d.date, l) }
        }
        return out
    }

    // ---------- ВСПОМОГАТЕЛЬНОЕ ----------
    private fun dp(v: Int): Int =
        (v * resources.displayMetrics.density).roundToInt()

    private fun weekStartMillis(anyMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.timeInMillis = anyMillis
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val shift = when (dow) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> -1
            Calendar.WEDNESDAY -> -2
            Calendar.THURSDAY -> -3
            Calendar.FRIDAY -> -4
            Calendar.SATURDAY -> -5
            Calendar.SUNDAY -> -6
            else -> 0
        }
        cal.add(Calendar.DAY_OF_MONTH, shift)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** Заглушка: генерим учебную неделю с заменами и праздником */
    private fun mockWeek(anyMillis: Long): List<DaySchedule> {
        val startMs = weekStartMillis(anyMillis)
        val list = mutableListOf<DaySchedule>()
        val subjects = listOf("Математика","Информатика","Русский","История","Английский")
        val teacher = SimpleRef(1, "Иванова И.И.")
        val room301 = SimpleRef(301, "каб. 301")
        val room302 = SimpleRef(302, "каб. 302")

        val weekCode = if (((startMs / DAY_MS) / 7) % 2L == 0L) "A" else "B"

        for (i in 0 until 5) {
            val dayMs = startMs + i * DAY_MS
            val dateStr = dateFmt.format(dayMs)
            val holiday = (i == 2) // среда — праздник

            if (holiday) {
                list += DaySchedule(
                    date = dateStr,
                    week_code = weekCode,
                    holiday = true,
                    lessons = emptyList(),
                    version = 1L,
                    holidayReason = "Праздник 🎉"
                )
            } else {
                val lessons = (1..5).map { idx ->
                    val change =
                        if (i == 1 && idx == 2) ChangeBadge("replace", "Ведёт Петров") else
                            if (i == 3 && idx == 4) ChangeBadge("cancel", "Учитель на совещании") else
                                null

                    LessonItem(
                        lesson_idx = idx,
                        starts_at = "%02d:%02d".format(8 + (idx - 1), if (idx % 2 == 0) 30 else 0),
                        ends_at   = "%02d:%02d".format(8 + (idx - 1), if (idx % 2 == 0) 55 else 45),
                        subject = subjects[(idx + i) % subjects.size],
                        teacher = teacher,
                        room = if (idx % 2 == 0) room302 else room301,
                        change = change
                    )
                }
                list += DaySchedule(
                    date = dateStr,
                    week_code = weekCode,
                    holiday = false,
                    lessons = lessons,
                    version = 1L
                )
            }
        }
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
