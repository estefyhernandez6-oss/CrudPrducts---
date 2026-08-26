package com.example.crudprducts1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.crudprducts1.ui.navigation.NavGraph
import com.example.crudprducts1.ui.theme.CrudPrducts1Theme
import com.example.crudprducts1.ui.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Creamos la instancia aquí
        val viewModel = ProductViewModel()

        setContent {
            CrudPrducts1Theme {
                NavGraph(productViewModel = viewModel)
            }
        }
    }
}