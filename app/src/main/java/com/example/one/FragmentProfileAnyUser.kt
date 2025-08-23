package com.example.one

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.one.adapters.AchievementsAdapter
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentProfileAnyUser : Fragment() {

    companion object {
        private const val ARG_USER_ID = "arg_user_id"

        fun newInstance(userId: Int?): FragmentProfileAnyUser =
            FragmentProfileAnyUser().apply {
                arguments = Bundle().apply {
                    if (userId != null) putInt(ARG_USER_ID, userId)
                }
            }
    }

    private var viewedUserId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewedUserId = if (arguments?.containsKey(ARG_USER_ID) == true)
            arguments?.getInt(ARG_USER_ID)
        else null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.teacher_profile_fragment, container, false)

    private fun replaceFragment(newFragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, newFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val api = ApiManager(requireContext())

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val editButton = view.findViewById<ImageButton>(R.id.edit_button)
        val progressBar = view.findViewById<ProgressBar>(R.id.loadingProgressBar)
        val contentContainer = view.findViewById<View>(R.id.contentContainer)
        val logout = view.findViewById<Button>(R.id.logout)

        val fullNameText = view.findViewById<TextView>(R.id.name)
        val workText = view.findViewById<TextView>(R.id.work)
        val locationText = view.findViewById<TextView>(R.id.place)
        val bioText = view.findViewById<TextView>(R.id.bio)

        editButton.setOnClickListener { replaceFragment(StaffEditProfileFragment()) }
        logout.setOnClickListener {
            api.clearData()
            startActivity(Intent(activity, MainActivity::class.java))
        }

        lifecycleScope.launch {
            val isValid = api.isTokenValid()
            if (!isValid) {
                Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val currentUserId = api.getUserId()
            val isOwnProfile = viewedUserId == null || viewedUserId == currentUserId

            // UI: скрываем управление на чужом профиле
            editButton.isVisible = isOwnProfile
            logout.isVisible = isOwnProfile

            progressBar.isVisible = true
            contentContainer.isGone = true

            if (isOwnProfile) {
                // ----- Свой профиль (как было)
                val bitmap: Bitmap? = api.getProfilePhoto()
                if (bitmap != null) imageView.setImageBitmap(bitmap)
                else Toast.makeText(requireContext(), "Не удалось загрузить фото", Toast.LENGTH_SHORT).show()

                val profile = withContext(Dispatchers.IO) { api.getProfileData() }
                profile?.let {
                    fullNameText.text = it.full_name
                    workText.text = it.work_place
                    locationText.text = it.location
                    bioText.text = it.bio
                }
            } else {
                // ----- Чужой профиль (публичные данные)
                val otherId = viewedUserId!!
                val user = withContext(Dispatchers.IO) { api.getUserPublicInfo(otherId) }
                if (user == null) {
                    Toast.makeText(requireContext(), "Профиль недоступен", Toast.LENGTH_SHORT).show()
                } else {
                    fullNameText.text = user.full_name
                    workText.text = user.class_name ?: user.work_place ?: "Не указано"
                    locationText.text = user.work_place ?: ""
                    bioText.text = user.role ?: ""

                    // Фото по ID (как в MessageFragment)
                    Glide.with(requireContext())
                        .load("http://10.0.2.2:8001/profile_photo/${user.id}")
                        .circleCrop()
                        .into(imageView)
                }


            }

            progressBar.isGone = true
            contentContainer.isVisible = true
        }

        // ---- Ачивки: оставляем как в оригинале (для чужого профиля — без перетаскивания)
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
                v: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) { outRect.set(spacing, spacing, spacing, spacing) }
        })

        // Включаем drag’n’drop только для своего профиля
        lifecycleScope.launch {
            val isOwn = try { (viewedUserId == null) || (ApiManager(requireContext()).getUserId() == viewedUserId) }
            catch (_: Exception) { false }

            val callback = object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    if (!isOwn) return false
                    adapter.swapItems(viewHolder.adapterPosition, target.adapterPosition)
                    val updated = adapter.getCurrentList().mapNotNull { getDrawableName(it) }
                    saveAchievementsOrder(updated)
                    return true
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
                override fun isLongPressDragEnabled(): Boolean = isOwn
            }
            ItemTouchHelper(callback).attachToRecyclerView(achievementsRecycler)
        }
    }

    // ==== вспомогательные методы (оставлены из исходника) ====

    private fun saveAchievementsOrder(order: List<String>) {
        val prefs = requireContext().getSharedPreferences("achievements_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("achievements_order", order.joinToString(","))
            .apply()
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

    private fun getDrawableName(resId: Int): String? =
        try { requireContext().resources.getResourceEntryName(resId) } catch (_: Exception) { null }
}
