package com.example.crudprducts1.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.crudprducts1.ui.componentes.ButtonNavigate
import com.example.crudprducts1.ui.componentes.CardProduct
import com.example.crudprducts1.ui.componentes.DeleateDialog
import com.example.crudprducts1.ui.viewmodel.ProductViewModel
import com.example.crudprducts1.util.OrdenProducto
import com.example.crudprducts1.util.ProductState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    productViewModel: ProductViewModel,
    navigate: () -> Unit,
    onLogout: () -> Unit = {}
) {

    val context = LocalContext.current

    // =====================================================
    // ESTADOS
    // =====================================================

    var showDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var idProduct by rememberSaveable {
        mutableStateOf("")
    }

    // Se consume la lista YA filtrada y ordenada por el ViewModel, no la cruda.
    val listProductState by
    productViewModel.productosVisibles.collectAsState()

    val consulta by productViewModel.consulta.collectAsState()
    val orden by productViewModel.orden.collectAsState()
    val resumen by productViewModel.resumenInventario.collectAsState()

    // Antes el borrado no reportaba nada: si Firebase o el bucket lo rechazaban, el
    // producto simplemente seguia ahi sin explicacion.
    val deleteState by productViewModel.deleteResult.collectAsState()

    LaunchedEffect(deleteState) {
        when (val d = deleteState) {
            is ProductState.Success -> {
                Toast.makeText(context, d.data, Toast.LENGTH_SHORT).show()
                productViewModel.clearDeleteResult()
            }
            is ProductState.Error -> {
                Toast.makeText(
                    context,
                    "No se pudo eliminar: ${d.message}",
                    Toast.LENGTH_LONG
                ).show()
                productViewModel.clearDeleteResult()
            }
            else -> {}
        }
    }


    // =====================================================
    // DIÁLOGO ELIMINAR
    // =====================================================

    DeleateDialog(
        showDialog = showDialog,

        aceptar = {
            productViewModel.deleteProduct(idProduct)

            showDialog = false
            idProduct = ""
        },

        cancelar = {
            showDialog = false
            idProduct = ""
        }
    )


    // =====================================================
    // CONTENIDO PRINCIPAL
    // =====================================================

    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                actions = {
                    // `logout()` existia en el repositorio y no lo llamaba nadie: no
                    // habia forma de cerrar sesion desde la app.
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },

        floatingActionButton = {

            ButtonNavigate(
                navigate = navigate
            )
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // =================================================
            // BUSCADOR
            // =================================================

            OutlinedTextField(
                value = consulta,
                onValueChange = { productViewModel.setConsulta(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (consulta.isNotEmpty()) {
                        IconButton(onClick = { productViewModel.setConsulta("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // =================================================
            // ORDEN
            // =================================================

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(OrdenProducto.entries.toList()) { opcion ->
                    FilterChip(
                        selected = orden == opcion,
                        onClick = { productViewModel.setOrden(opcion) },
                        label = { Text(opcion.etiqueta) }
                    )
                }
            }

            // =================================================
            // RESUMEN DEL INVENTARIO
            // =================================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (consulta.isBlank()) {
                        "${resumen.cantidad} productos"
                    } else {
                        "${resumen.cantidad} coinciden"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = buildString {
                        append("Total: $")
                        append(String.format(Locale.US, "%,.2f", resumen.total))
                        // Se avisa si el total no cuenta a todos: si no, un producto
                        // con el precio mal escrito falsea la cifra en silencio.
                        if (resumen.sinPrecio > 0) {
                            append("  (${resumen.sinPrecio} sin precio)")
                        }
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider()

            when (val result = listProductState) {

                // =================================================
                // CARGANDO
                // =================================================

                is ProductState.Loading -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }


                // =================================================
                // PRODUCTOS CARGADOS
                // =================================================

                is ProductState.Success -> {

                    if (result.data.isEmpty()) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),

                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = if (consulta.isBlank()) {
                                    "No hay productos registrados"
                                } else {
                                    "Ningún producto coincide con \"$consulta\""
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                    } else {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {

                            items(
                                items = result.data,
                                key = { product ->
                                    product.id
                                }
                            ) { product ->

                                CardProduct(

                                    product = product,

                                    // -----------------------------
                                    // ELIMINAR
                                    // -----------------------------

                                    delete = {

                                        idProduct = product.id

                                        showDialog = true
                                    },

                                    // -----------------------------
                                    // EDITAR
                                    // -----------------------------

                                    edit = {

                                        productViewModel
                                            .setProductForEdit(product)

                                        navigate()
                                    }
                                )
                            }
                        }
                    }
                }


                // =================================================
                // ERROR
                // =================================================

                is ProductState.Error -> {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),

                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = result.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }


                // =================================================
                // IDLE
                // =================================================

                ProductState.Idle -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "No hay información disponible",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}