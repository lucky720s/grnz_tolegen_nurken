package com.example.grnz.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.grnz.R

class RegisterFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var goToLoginTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailEditText = view.findViewById(R.id.edit_text_email)
        passwordEditText = view.findViewById(R.id.edit_text_password)
        registerButton = view.findViewById(R.id.button_register)
        goToLoginTextView = view.findViewById(R.id.text_view_go_to_login)
        progressBar = view.findViewById(R.id.progress_bar)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (validateInput(email, password)) {
                viewModel.registerUser(email, password)
            }
        }

        goToLoginTextView.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    progressBar.isVisible = true
                    registerButton.isEnabled = false
                    goToLoginTextView.isEnabled = false
                }
                is AuthState.Success -> {
                    progressBar.isVisible = false
                    registerButton.isEnabled = true
                    goToLoginTextView.isEnabled = true
                    Toast.makeText(context, state.message ?: "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                    emailEditText.text.clear()
                    passwordEditText.text.clear()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
                is AuthState.Error -> {
                    progressBar.isVisible = false
                    registerButton.isEnabled = true
                    goToLoginTextView.isEnabled = true
                    Toast.makeText(context, "Ошибка: ${state.message}", Toast.LENGTH_LONG).show()
                }
                is AuthState.Idle -> {
                    progressBar.isVisible = false
                    registerButton.isEnabled = true
                    goToLoginTextView.isEnabled = true
                }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(context, "Введите Email", Toast.LENGTH_SHORT).show()
            emailEditText.error = "Email не может быть пустым"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Неверный формат Email", Toast.LENGTH_SHORT).show()
            emailEditText.error = "Неверный формат Email"
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(context, "Введите пароль", Toast.LENGTH_SHORT).show()
            passwordEditText.error = "Пароль не может быть пустым"
            return false
        }
        val minPasswordLength = 6
        if (password.length < minPasswordLength) {
            val errorMsg = "Пароль должен быть не менее $minPasswordLength символов"
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            passwordEditText.error = errorMsg
            return false
        }
        emailEditText.error = null
        passwordEditText.error = null
        return true
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetState()
    }
}
