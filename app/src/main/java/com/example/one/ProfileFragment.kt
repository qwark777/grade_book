package com.example.one

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.teacher_profile_fragment, container, false)
    }

    private fun replaceFragment(newFragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, newFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun saveAchievementsOrder(order: List<String>) {
        val prefs = requireContext().getSharedPreferences("achievements_prefs", 0)
        prefs.edit { putString("achievements_order", order.joinToString(",")) }
    }

    private fun loadAchievementsOrder(): MutableList<Int> {
        val prefs = requireContext().getSharedPreferences("achievements_prefs", 0)
        val stored = prefs.getString("achievements_order", null)
        return if (stored != null) {
            stored.split(",").mapNotNull { name ->
                val resId = resources.getIdentifier(name, "drawable", requireContext().packageName)
                if (resId != 0) resId else null
            }.toMutableList()
        } else {
            mutableListOf(
                R.drawable.class_president,
                R.drawable.olimpiad_coach,
            )
        }
    }

    private fun getDrawableName(resId: Int): String? {
        return try {
            requireContext().resources.getResourceEntryName(resId)
        } catch (e: Exception) {
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val editButton = view.findViewById<ImageButton>(R.id.edit_button)
        val progressBar = view.findViewById<ProgressBar>(R.id.loadingProgressBar)
        val contentContainer = view.findViewById<View>(R.id.contentContainer)
        val logout = view.findViewById<Button>(R.id.logout)

        val fullNameText = view.findViewById<TextView>(R.id.name)
        val workText = view.findViewById<TextView>(R.id.work)
        val locationText = view.findViewById<TextView>(R.id.place)
        val bioText = view.findViewById<TextView>(R.id.bio)

        val prefs = requireContext().getSharedPreferences("profile_cache", Context.MODE_PRIVATE)

        // Сначала загрузим из кэша
        fullNameText.text = prefs.getString("full_name", "Имя пользователя")
        workText.text = prefs.getString("work_place", "Место работы")
        locationText.text = prefs.getString("location", "Местоположение")
        bioText.text = prefs.getString("bio", "Описание")

        editButton.setOnClickListener {
            replaceFragment(StaffEditProfileFragment())
        }

        logout.setOnClickListener {
            ApiManager(requireContext()).clearToken()
            startActivity(Intent(activity, MainActivity::class.java))
        }

        lifecycleScope.launch {
            val bitmap: Bitmap? = ApiManager(requireContext()).getProfilePhoto()
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                Toast.makeText(requireContext(), "Не удалось загрузить фото", Toast.LENGTH_SHORT).show()
            }

            // Затем подгружаем данные с сервера
            val profile = withContext(Dispatchers.IO) {
                ApiManager(requireContext()).getProfileData()
            }
            println(profile)
            if (profile != null) {
                fullNameText.text = profile.full_name
                workText.text = profile.work_place
                locationText.text = profile.location
                bioText.text = profile.bio

                prefs.edit {
                    putString("full_name", profile.full_name)
                    putString("work_place", profile.work_place)
                    putString("location", profile.location)
                    putString("bio", profile.bio)
                }
            }

            progressBar.visibility = View.GONE
            contentContainer.visibility = View.VISIBLE
        }

        val achievementsRecycler = view.findViewById<RecyclerView>(R.id.achievementsRecyclerView)
        val achievementList = loadAchievementsOrder()
        val adapter = AchievementsAdapter(achievementList)

        achievementsRecycler.adapter = adapter
        achievementsRecycler.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.FLEX_START
        }

        val spacing = 8
        achievementsRecycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.set(spacing, spacing, spacing, spacing)
            }
        })

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                adapter.swapItems(viewHolder.adapterPosition, target.adapterPosition)
                val updated = adapter.getCurrentList().mapNotNull { getDrawableName(it) }
                saveAchievementsOrder(updated)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            override fun isLongPressDragEnabled(): Boolean = true
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(achievementsRecycler)
    }
}
