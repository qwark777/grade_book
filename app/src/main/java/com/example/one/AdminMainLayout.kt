package com.example.one
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.one.ui.student.StudentsFragment
import com.example.one.ui.teacher.TeachersFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourpackage.diaryschool.network.ApiManager


class AdminMainLayout: AppCompatActivity() {

    private lateinit var apiManager: ApiManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.admin_main_layout)

        val mockTeachers = listOf(
            TeacherData(
                fullName = "Иван Петров",
                workPlace = "Школа №5",
                location = "Москва",
                subject = "Математика",
                classes = "10А, 11Б",
                username = "ivan.petrov",
                password = "12345"
            ),
            TeacherData(
                fullName = "Ольга Смирнова",
                workPlace = "Лицей №2",
                location = "Санкт-Петербург",
                subject = "Химия",
                classes = "9А, 9Б",
                username = "olga.smirnova",
                password = "54321"
            )
        )



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
                        .replace(R.id.fragment_container, ClassFragment())
                        .commit()
                    true
                }

                R.id.nav_rating -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, RatingFragment())
                        .commit()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .commit()
                    true
                }
                R.id.nav_teachers -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, TeachersFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }

    }
}