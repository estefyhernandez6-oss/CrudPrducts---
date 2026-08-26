package com.example.crudprducts1.data.model

data class LoginValidation(
    val email: String? = null,
    val password: PasswordValidation = PasswordValidation()
)

data class PasswordValidation(
    val hasNumber: Boolean = false,
    val hasUpperCase: Boolean = false,
    val minLength: Boolean = false
)
data class User(
    val name: String = "",
    val email: String = "",
    val password: String = ""
)