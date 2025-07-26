package com.example.one

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var authViewModel: ApiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.admin_login_layout)

        // Инициализация ApiManager и AuthViewModel
        authViewModel = ApiManager(applicationContext)

        val button: Button = findViewById(R.id.button3)
        val loginEditText: EditText = findViewById(R.id.login)
        val passwordEditText: EditText = findViewById(R.id.password)

        button.setOnClickListener {
            val username = loginEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Запускаем корутину для выполнения сетевого запроса
            lifecycleScope.launch {
                try {
                    val isSuccess = authViewModel.login(username, password)
                    if (isSuccess) {
                        // Успешный вход
                        Toast.makeText(
                            this@AdminLoginActivity,
                            "Вход выполнен успешно",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@AdminLoginActivity, AdminMainLayout::class.java)
                        startActivity(intent)

                    } else {
                        Toast.makeText(
                            this@AdminLoginActivity,
                            "Неверный логин или пароль",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    // Обработка ошибок сети
                    Toast.makeText(
                        this@AdminLoginActivity,
                        "Ошибка подключения: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}