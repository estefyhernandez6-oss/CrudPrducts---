package com.example.crudprducts1.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crudprducts1.data.model.LoginValidation
import com.example.crudprducts1.data.model.PasswordValidation
import com.example.crudprducts1.data.model.User
import com.example.crudprducts1.data.repository.ProductRepository
import com.example.crudprducts1.util.ProductState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // =====================================================
    // REPOSITORIO
    // =====================================================

    private val repository = ProductRepository()

    // =====================================================
    // USUARIO
    // =====================================================

    private val _user = MutableStateFlow(User())

    val user: StateFlow<User> = _user

    // =====================================================
    // VALIDACIÓN
    // =====================================================

    private val _validation =
        MutableStateFlow(LoginValidation())

    val validation: StateFlow<LoginValidation> = _validation

    // =====================================================
    // RESULTADO
    // =====================================================

    private val _result =
        MutableStateFlow<ProductState<String>>(
            ProductState.Idle
        )

    val result: StateFlow<ProductState<String>> = _result

    // =====================================================
    // NOMBRE
    // =====================================================

    fun setName(name: String) {

        _user.update {
            it.copy(name = name)
        }
    }

    // =====================================================
    // EMAIL
    // =====================================================

    fun setEmail(email: String) {

        _user.update {
            it.copy(email = email)
        }

        _validation.update {
            it.copy(
                email = validateEmail(email)
            )
        }
    }

    private fun validateEmail(
        email: String
    ): String? {

        return when {

            email.isBlank() ->
                "Correo requerido"

            !Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches() ->
                "Formato de correo incorrecto"

            else ->
                null
        }
    }

    // =====================================================
    // PASSWORD
    // =====================================================

    fun setPassword(password: String) {

        _user.update {
            it.copy(password = password)
        }

        _validation.update {
            it.copy(
                password = validatePassword(password)
            )
        }
    }

    private fun validatePassword(
        password: String
    ): PasswordValidation {

        return PasswordValidation(

            hasNumber =
                password.any {
                    it.isDigit()
                },

            hasUpperCase =
                password.any {
                    it.isUpperCase()
                },

            minLength =
                password.length >= 6
        )
    }

    // =====================================================
    // BOTÓN REGISTRO
    // =====================================================

    fun enableButton(): Boolean {

        val currentUser = _user.value
        val validation = _validation.value

        return currentUser.name.isNotBlank() &&
                currentUser.email.isNotBlank() &&
                currentUser.password.isNotBlank() &&
                validation.email == null &&
                validation.password.minLength &&
                validation.password.hasUpperCase &&
                validation.password.hasNumber
    }

    // =====================================================
    // BOTÓN LOGIN
    // =====================================================

    fun enableButtonSignIn(): Boolean {

        val currentUser = _user.value
        val validation = _validation.value

        return currentUser.email.isNotBlank() &&
                currentUser.password.isNotBlank() &&
                validation.email == null
    }

    // =====================================================
    // REGISTRAR USUARIO
    // =====================================================

    fun createUserEmailPassword() {

        if (!enableButton()) {
            return
        }

        viewModelScope.launch {

            _result.value =
                ProductState.Loading

            repository.createUserEmailPassword(
                _user.value
            ) { result ->

                _result.value = result
            }
        }
    }

    // =====================================================
    // INICIAR SESIÓN
    // =====================================================

    fun signInEmailPassword() {

        if (!enableButtonSignIn()) {
            return
        }

        val currentUser = _user.value

        viewModelScope.launch {

            _result.value =
                ProductState.Loading

            repository.signinEmailPassword(
                currentUser.email,
                currentUser.password
            ) { result ->

                _result.value = result
            }
        }
    }

    // =====================================================
    // LOGIN GOOGLE
    // =====================================================

    fun signInGoogle(
        idToken: String
    ) {

        viewModelScope.launch {

            _result.value =
                ProductState.Loading

            repository.signInGoogle(
                idToken
            ) { result ->

                _result.value = result
            }
        }
    }

    // =====================================================
    // RESET
    // =====================================================

    fun reset() {

        _result.value =
            ProductState.Idle

        _user.value =
            User()

        _validation.value =
            LoginValidation()
    }
}