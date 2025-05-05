package com.example.grnz.ui.auth

import android.os.Bundle
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

class LoginFragment : Fragment() {
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var goToRegisterTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailEditText = view.findViewById(R.id.edit_text_email)
        passwordEditText = view.findViewById(R.id.edit_text_password)
        loginButton = view.findViewById(R.id.button_login)
        goToRegisterTextView = view.findViewById(R.id.text_view_go_to_register)
        progressBar = view.findViewById(R.id.progress_bar)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.loginUser(email, password)
            } else {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        goToRegisterTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    progressBar.isVisible = true
                    loginButton.isEnabled = false
                    goToRegisterTextView.isEnabled = false
                }
                is AuthState.Success -> {
                    progressBar.isVisible = false
                    loginButton.isEnabled = true
                    goToRegisterTextView.isEnabled = true
                    Toast.makeText(context, state.message ?: "Вход успешен!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_mainContent)
                }
                is AuthState.Error -> {
                    progressBar.isVisible = false
                    loginButton.isEnabled = true
                    goToRegisterTextView.isEnabled = true
                    Toast.makeText(context, "Ошибка: ${state.message}", Toast.LENGTH_LONG).show()
                }
                is AuthState.Idle -> {
                    progressBar.isVisible = false
                    loginButton.isEnabled = true
                    goToRegisterTextView.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetState()
    }
}
