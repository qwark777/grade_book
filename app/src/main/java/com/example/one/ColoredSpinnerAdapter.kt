package com.example.one

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class ColoredSpinnerAdapter(
    context: Context,
    resource: Int,
    items: List<String>,
    private val dropdownTextColor: Int
) : ArrayAdapter<String>(context, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Обычное отображение (не выпадающий список)
        val view = super.getView(position, convertView, parent)
        (view as TextView).setTextColor(ContextCompat.getColor(context, R.color.orange))
        view.setTypeface(null, Typeface.BOLD)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Выпадающее представление
        val view = super.getDropDownView(position, convertView, parent)
        (view as TextView).apply {
            setTextColor(ContextCompat.getColor(context, dropdownTextColor))
            setBackgroundColor(ContextCompat.getColor(context, R.color.orange))
            setTypeface(null, Typeface.BOLD)
        }
        return view
    }
}