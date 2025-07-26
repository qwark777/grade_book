package com.example.one

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

data class RatingItem(
    val id: Int,
    val name: String,
    val score: Int
)

class RatingAdapter : ListAdapter<RatingItem, RatingAdapter.ViewHolder>(Diff()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.name)
        private val score: TextView = view.findViewById(R.id.score)
        private val place: TextView = view.findViewById(R.id.place)

        fun bind(item: RatingItem, position: Int) {
            name.text = item.name
            score.text = "${item.score}"
            place.text = when (position) {
                0 -> "🥇"
                1 -> "🥈"
                2 -> "🥉"
                else -> (position + 1).toString()
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<RatingItem>() {
        override fun areItemsTheSame(old: RatingItem, new: RatingItem) = old.id == new.id
        override fun areContentsTheSame(old: RatingItem, new: RatingItem) = old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rating, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
}
