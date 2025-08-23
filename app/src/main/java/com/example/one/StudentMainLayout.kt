package com.example.one

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class StudentMainLayout :  AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.student_main_layout)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MainAdminFragment())
            .commit()

        // Обработка нижнего меню
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, RatingFragment())
                        .commit()
                    true
                }

//                R.id.nav_rating -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, RatingFragment())
//                        .commit()
//                    true
//                }
//
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, RatingFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }

    }
}