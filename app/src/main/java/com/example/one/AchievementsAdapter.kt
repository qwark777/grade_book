package com.example.one

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView

class AchievementsAdapter(
    private val achievements: MutableList<Int>
) : RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.achievementImage)
        val titleView: TextView = itemView.findViewById(R.id.achievementTitle)
        val dateView: TextView = itemView.findViewById(R.id.achievementDate)
        val glowAnimation: LottieAnimationView = itemView.findViewById(R.id.glowAnimation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resId = achievements[position]
        val context = holder.itemView.context

        // Устанавливаем изображение
        holder.imageView.setImageResource(resId)

        // Название из имени drawable
        val resName = try {
            context.resources.getResourceEntryName(resId)
        } catch (e: Exception) {
            "Achievement"
        }

        holder.titleView.text = resName.replace("_", " ").replaceFirstChar { it.uppercase() }

        // Пример даты
        holder.dateView.text = "12.07.2025"

        // Glow-анимация для редкости
        val animation = when {
            resName.contains("olimpiad_coach", ignoreCase = true) -> R.raw.achievement_glow_100x100
            resName.contains("heart_of_class", ignoreCase = true) -> R.raw.red_glow
            resName.contains("class_president", ignoreCase = true) -> R.raw.glow_loop_fade
            else -> null
        }

        if (animation != null) {
            holder.glowAnimation.setAnimation(animation)
            holder.glowAnimation.visibility = View.VISIBLE
            holder.glowAnimation.playAnimation()
        } else {
            holder.glowAnimation.visibility = View.GONE
            holder.glowAnimation.cancelAnimation()
        }
    }

    override fun getItemCount(): Int = achievements.size

    fun swapItems(from: Int, to: Int) {
        if (from in achievements.indices && to in achievements.indices) {
            val tmp = achievements[from]
            achievements[from] = achievements[to]
            achievements[to] = tmp
            notifyItemMoved(from, to)
        }
    }

    fun getCurrentList(): List<Int> = achievements
}
