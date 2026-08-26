package com.example.crudprducts1.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crudprducts1.ui.viewmodel.AuthViewModel
import com.example.crudprducts1.util.ProductState

// =====================================================
// ACCIONES DE NAVEGACIÓN
// =====================================================

enum class Navigate {
    REGISTER,
    HOME
}

// =====================================================
// AUTH SCREEN
// =====================================================

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onNavigate: (Navigate) -> Unit
) {

    val user by authViewModel.user.collectAsState()
    val validation by authViewModel.validation.collectAsState()
    val result by authViewModel.result.collectAsState()

    val context = LocalContext.current

    // =================================================
    // RESULTADO DEL LOGIN
    // =================================================

    LaunchedEffect(result) {

        when (result) {

            is ProductState.Success -> {

                Toast.makeText(
                    context,
                    "¡Inicio de sesión exitoso!",
                    Toast.LENGTH_SHORT
                ).show()

                authViewModel.reset()

                onNavigate(Navigate.HOME)
            }

            is ProductState.Error -> {

                val message =
                    (result as ProductState.Error).message

                Toast.makeText(
                    context,
                    message,
                    Toast.LENGTH_LONG
                ).show()

                authViewModel.reset()
            }

            else -> {}
        }
    }

    // =================================================
    // PANTALLA
    // =================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(
                rememberScrollState()
            ),

        verticalArrangement = Arrangement.Center,

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // =================================================
        // TÍTULO
        // =================================================

        Text(
            text = "Iniciar sesión",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Ingresa tus datos para continuar",
            color = Color.Gray
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        // =================================================
        // CORREO
        // =================================================

        OutlinedTextField(
            value = user.email,

            onValueChange = {
                authViewModel.setEmail(it)
            },

            label = {
                Text("Correo electrónico")
            },

            singleLine = true,

            isError = validation.email != null,

            modifier = Modifier.fillMaxWidth()
        )

        validation.email?.let { error ->

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    start = 8.dp,
                    top = 4.dp
                )
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // =================================================
        // CONTRASEÑA
        // =================================================

        OutlinedTextField(
            value = user.password,

            onValueChange = {
                authViewModel.setPassword(it)
            },

            label = {
                Text("Contraseña")
            },

            singleLine = true,

            visualTransformation =
                PasswordVisualTransformation(),

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // =================================================
        // BOTÓN LOGIN
        // =================================================

        Button(
            onClick = {
                authViewModel.signInEmailPassword()
            },

            enabled =
                authViewModel.enableButtonSignIn() &&
                        result !is ProductState.Loading,

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {

            if (result is ProductState.Loading) {

                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.height(24.dp)
                )

            } else {

                Text(
                    text = "Iniciar sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // =================================================
        // REGISTRARSE
        // =================================================

        TextButton(
            onClick = {
                onNavigate(Navigate.REGISTER)
            }
        ) {

            Text(
                text = "¿No tienes una cuenta? Regístrate"
            )
        }
    }
}