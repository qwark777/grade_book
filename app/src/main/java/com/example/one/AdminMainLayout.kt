package com.example.one
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourpackage.diaryschool.network.ApiManager


class AdminMainLayout: AppCompatActivity() {

    private lateinit var apiManager: ApiManager


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

                else -> false
            }
        }

    }
}