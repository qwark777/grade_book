package com.example.one.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.one.R
import com.example.one.databinding.DialogAddTeacherBinding
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTeacherDialogFragment(
    private val onTeacherAdded: (() -> Unit)? = null
) : DialogFragment() {

    private var _binding: DialogAddTeacherBinding? = null
    private val binding get() = _binding!!

    private lateinit var apiManager: ApiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.PopupDialogStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTeacherBinding.inflate(inflater, container, false)
        apiManager = ApiManager(requireContext())

        binding.addButton.setOnClickListener {
            val username = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val success = withContext(Dispatchers.IO) {
                    apiManager.register(username, password)
                }

                if (success) {
                    Toast.makeText(context, "Преподаватель добавлен", Toast.LENGTH_SHORT).show()
                    onTeacherAdded?.invoke()
                    dismiss()
                } else {
                    Toast.makeText(context, "Ошибка при добавлении", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(onTeacherAdded: () -> Unit): AddTeacherDialogFragment {
            return AddTeacherDialogFragment(onTeacherAdded)
        }
    }
}
