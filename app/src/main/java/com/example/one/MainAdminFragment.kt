package com.example.one

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.launch

class MainAdminFragment : Fragment() {
    private lateinit var authViewModel: ApiManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_admin_fragment, container, false)
    }



    private fun generatePassword(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#\$%^&*()-_=+"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
    private fun showCustomDialog() {
        val context: Context = requireContext()

        val dialogView = layoutInflater.inflate(R.layout.custom_dialog, null)


        val items = listOf("Ученик", "Учитель", "Администратор ")
        val adapter = ColoredSpinnerAdapter(
            context,
            android.R.layout.simple_spinner_item,
            items,
            R.color.black
        )
        val spinner: Spinner = dialogView.findViewById(R.id.spinner)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val login = dialogView.findViewById<EditText>(R.id.login)

        val okButton = dialogView.findViewById<Button>(R.id.dialogOkButton)
        okButton.setOnClickListener {
            val username = login.text.toString()

            if (username.isBlank()) {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            authViewModel = ApiManager(requireContext())


            viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val result = authViewModel.register(username, "123")

                        if (result) {
                            Toast.makeText(
                                context,
                                "Пользователь зарегестрирован",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Чет не вышло",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Ошибка подключения: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                dialog.dismiss()
            }



        dialog.show()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val but: ImageButton = view.findViewById(R.id.add_button)
        but.setOnClickListener {
            showCustomDialog()
        }


        val butt: Button = view.findViewById(R.id.button1)
        butt.setOnClickListener {


            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ClassFragment())
                .addToBackStack(null)
                .commit()
        }


    }
}