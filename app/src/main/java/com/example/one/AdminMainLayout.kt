package com.example.one
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.one.ui.messages.ConversationsFragment
import com.example.one.ui.teacher.UsersFragment
import com.example.one.ui.timetable.TimetableFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourpackage.diaryschool.network.ApiManager


class AdminMainLayout: AppCompatActivity() {

    private lateinit var apiManager: ApiManager
    fun openChat(userId: Int) {
        // переключаем нижнее меню
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_messages

        // сразу открываем чат
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, com.example.one.ui.messages.MessageFragment.newInstance(userId))
            .addToBackStack("chat_$userId")
            .commit()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.admin_main_layout)




        apiManager = ApiManager(this)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ClassFragment())
            .commit()

        // Обработка нижнего меню
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, TimetableFragment(1))
//                        TODO("получение id класса в котором учится ")
                        .commit()
                    true
                }

//                R.id.nav_rating -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, RatingFragment())
//                        .commit()
//                    true
//                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .commit()
                    true
                }
                R.id.nav_teachers -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, UsersFragment())
                        .commit()
                    true
                }
                R.id.nav_messages -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ConversationsFragment())
                        .commit()
                    true
                }
                R.id.nav_performance ->{
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PerformanceFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

    }
}