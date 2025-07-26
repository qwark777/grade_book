package com.example.one

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.launch

class ClassFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClassAdapter
    private lateinit var apiManager: ApiManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_class_list, container, false)
        recyclerView = view.findViewById(R.id.classRecyclerView)

        recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = ClassAdapter { selectedClass ->
            // обработка нажатия на класс
            Toast.makeText(context, "Класс: $selectedClass", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        loadClassesFromDB()

        return view
    }

    private fun loadClassesFromDB() {
        lifecycleScope.launch {
            apiManager = ApiManager(requireContext())
            val classList = apiManager.getClasses()
                .map { ClassItem(it.name, 23) } // если класс API отличается
            adapter.submitList(classList)
        }
    }
}
