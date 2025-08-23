package com.example.one

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.one.databinding.FragmentPerformanceBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.example.one.PerformanceOverviewFragment
import com.example.one.PerformanceSubjectsFragment
import com.example.one.PerformanceTrendsFragment
import com.example.one.PerformanceAttendanceFragment

class PerformanceFragment : Fragment() {

    private lateinit var binding: FragmentPerformanceBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        val pages = listOf(
            "Обзор" to { PerformanceOverviewFragment() },
            "Предметы" to { PerformanceSubjectsFragment() },
            "Динамика" to { PerformanceTrendsFragment() },
            "Посещаемость" to { PerformanceAttendanceFragment() }
        )

        binding.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = pages.size
            override fun createFragment(position: Int) = pages[position].second()
        }

        TabLayoutMediator(binding.tabs, binding.pager) { tab, pos ->
            tab.text = pages[pos].first
        }.attach()
    }
}
