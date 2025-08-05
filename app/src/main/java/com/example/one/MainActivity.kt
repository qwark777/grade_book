package com.example.one

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.one.databinding.ActivityMainBinding
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var apiManager: ApiManager

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }


    fun typewriterWithDelay(textView: TextView, text: String, startDelay: Long = 1500, charDelay: Long = 10) {
        textView.text = ""

        Handler(Looper.getMainLooper()).postDelayed({
            var i = 0
            val printingRunnable = object : Runnable {
                override fun run() {
                    if (i < text.length) {
                        textView.text = text.substring(0, ++i)
                        Handler(Looper.getMainLooper()).postDelayed(this, charDelay)
                    }
                }
            }
            Handler(Looper.getMainLooper()).post(printingRunnable)
        }, startDelay) // Задержка перед стартом
    }

    private fun initUI() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val button_teacher = findViewById<Button>(R.id.button_teacher)
        val button_student = findViewById<Button>(R.id.button_student)

        button_teacher.visibility = View.INVISIBLE
        button_student.visibility = View.INVISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            button_teacher.visibility = View.VISIBLE
            button_student.visibility = View.VISIBLE
        }, 2000)

        val imageView = findViewById<ImageView>(R.id.imageView2)
        imageView.setImageResource(R.drawable.logo)

        typewriterWithDelay(binding.textView, getString(R.string.start_mes))

        button_teacher.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }

        button_student.setOnClickListener {
            startActivity(Intent(this, StudentMainLayout::class.java))
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen инициализируем сразу
        val splashScreen = installSplashScreen()

        // Перехватываем splash и держим, пока не проверим токен
        var keepSplashVisible = true
        splashScreen.setKeepOnScreenCondition { keepSplashVisible }

        super.onCreate(savedInstanceState)

        // Инициализируем ApiManager
        apiManager = ApiManager(applicationContext)

        // Проверка токена — в lifecycleScope
        lifecycleScope.launch {
            val tokenIsValid = apiManager.isTokenValid()

            if (tokenIsValid) {
                // Переход к нужной активности (например, Admin)
                startActivity(Intent(this@MainActivity, AdminMainLayout::class.java))
//                TODO("Проверка админ или студент")
                finish() // Закрываем MainActivity
            } else {
                // Токен недействителен — продолжаем загрузку MainActivity
                keepSplashVisible = false // Скрываем splash
                apiManager.clearData()
                // Теперь можно грузить UI
                initUI()
            }
        }
    }

}