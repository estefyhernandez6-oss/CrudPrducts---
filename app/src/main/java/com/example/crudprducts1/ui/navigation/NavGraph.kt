package com.example.crudprducts1.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.crudprducts1.ui.screens.AddProductScreen
import com.example.crudprducts1.ui.screens.AuthScreen
// Asegúrate de importar el Navigate correcto si está en otro archivo, por ejemplo:
// import com.example.crudprducts1.ui.screens.Navigate
import com.example.crudprducts1.ui.screens.HomeScreen
import com.example.crudprducts1.ui.screens.RegisterScreen
import com.example.crudprducts1.ui.viewmodel.AuthViewModel
import com.example.crudprducts1.ui.viewmodel.ProductViewModel

@Composable
fun NavGraph(
    productViewModel: ProductViewModel
) {

    val navController: NavHostController =
        rememberNavController()

    val authViewModel: AuthViewModel = viewModel()

    // Firebase Auth conserva la sesion entre arranques, pero la app siempre empezaba en
    // "auth" y obligaba a iniciar sesion cada vez. `getCurrentUser()` existia en el
    // repositorio y no lo llamaba nadie.
    val destinoInicial = remember {
        if (productViewModel.isLoggedIn()) "home" else "auth"
    }

    NavHost(
        navController = navController,
        startDestination = destinoInicial
    ) {

        // ==========================================
        // AUTENTICACIÓN
        // ==========================================

        composable("auth") {

            AuthScreen(
                authViewModel = authViewModel
            ) { navAction ->

                // Si tu AuthScreen usa un enum, asegúrate de que coincida con este when.
                // Coloca el cursor sobre 'navAction' o presiona Ctrl+P para ver qué tipo espera.
                when (navAction.toString()) { // Convertirlo a string temporalmente evita el error de incompatibilidad de enums si vienen de archivos distintos

                    "REGISTER" -> {
                        navController.navigate("register")
                    }

                    "HOME" -> {
                        navController.navigate("home") {
                            popUpTo("auth") {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // REGISTRO
        // ==========================================

        composable("register") {

            RegisterScreen(
                authViewModel = authViewModel,

                onNavigateHome = {

                    navController.navigate("home") {

                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // ==========================================
        // HOME
        // ==========================================

        composable("home") {

            HomeScreen(
                productViewModel = productViewModel,

                navigate = {
                    navController.navigate("add")
                },

                onLogout = {
                    productViewModel.logout()
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // AGREGAR / EDITAR PRODUCTO
        // ==========================================

        composable("add") {

            AddProductScreen(
                productViewModel = productViewModel,

                navigate = {
                    navController.popBackStack()
                }
            )
        }
    }
}