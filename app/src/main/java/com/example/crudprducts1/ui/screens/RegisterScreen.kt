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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crudprducts1.data.model.LoginValidation
import com.example.crudprducts1.ui.viewmodel.AuthViewModel
import com.example.crudprducts1.util.ProductState

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateHome: () -> Unit
) {

    // =====================================================
    // ESTADOS
    // =====================================================

    val user by authViewModel.user.collectAsState()
    val validation by authViewModel.validation.collectAsState()
    val result by authViewModel.result.collectAsState()

    val context = LocalContext.current

    // =====================================================
    // RESULTADO DEL REGISTRO
    // =====================================================

    LaunchedEffect(result) {

        when (result) {

            is ProductState.Success -> {

                Toast.makeText(
                    context,
                    "¡Cuenta creada con éxito!",
                    Toast.LENGTH_SHORT
                ).show()

                onNavigateHome()

                authViewModel.reset()
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

    // =====================================================
    // PANTALLA
    // =====================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(
                rememberScrollState()
            ),
        verticalArrangement = Arrangement.Center
    ) {

        // =================================================
        // TÍTULO
        // =================================================

        Text(
            text = "Crear cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Regístrate para continuar",
            color = Color.Gray
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        // =================================================
        // NOMBRE
        // =================================================

        OutlinedTextField(
            value = user.name,
            onValueChange = {
                authViewModel.setName(it)
            },
            label = {
                Text("Nombre")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // =================================================
        // EMAIL
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

        // Mensaje de error del correo

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
            modifier = Modifier.height(8.dp)
        )

        // =================================================
        // VALIDACIONES DE CONTRASEÑA
        // =================================================

        Text(
            text = "• Mínimo 6 caracteres: ${
                if (validation.password.minLength)
                    "✔"
                else
                    "❌"
            }",
            color =
                if (validation.password.minLength)
                    Color(0xFF2E7D32)
                else
                    MaterialTheme.colorScheme.error
        )

        Text(
            text = "• Una letra mayúscula: ${
                if (validation.password.hasUpperCase)
                    "✔"
                else
                    "❌"
            }",
            color =
                if (validation.password.hasUpperCase)
                    Color(0xFF2E7D32)
                else
                    MaterialTheme.colorScheme.error
        )

        Text(
            text = "• Un número: ${
                if (validation.password.hasNumber)
                    "✔"
                else
                    "❌"
            }",
            color =
                if (validation.password.hasNumber)
                    Color(0xFF2E7D32)
                else
                    MaterialTheme.colorScheme.error
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // =================================================
        // BOTÓN REGISTRAR
        // =================================================

        Button(
            onClick = {
                authViewModel.createUserEmailPassword()
            },
            enabled = authViewModel.enableButton(),
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
                    modifier = Modifier
                        .height(24.dp)
                )

            } else {

                Text(
                    text = "Registrarse",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // =================================================
        // MENSAJE
        // =================================================

        Text(
            text = "La contraseña debe tener al menos 6 caracteres, una mayúscula y un número.",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
}