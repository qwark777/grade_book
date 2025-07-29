package com.example.one.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

data class RatingItem(
    val place: Int,
    val name: String,
    val average: Int
)

class RatingAdapter : androidx.recyclerview.widget.ListAdapter<RatingItem, RatingAdapter.ViewHolder>(Diff()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(_root_ide_package_.com.example.one.R.id.name)
        private val scoreText: TextView = view.findViewById(_root_ide_package_.com.example.one.R.id.score)
        private val placeText: TextView = view.findViewById(_root_ide_package_.com.example.one.R.id.place)

        fun bind(item: RatingItem) {
            nameText.text = item.name
            scoreText.text = item.average.toString()

            placeText.text = when (item.place) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> item.place.toString()
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<RatingItem>() {
        override fun areItemsTheSame(oldItem: RatingItem, newItem: RatingItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: RatingItem, newItem: RatingItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(_root_ide_package_.com.example.one.R.layout.item_rating, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
