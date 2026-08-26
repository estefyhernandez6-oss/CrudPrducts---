package com.example.crudprducts1.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crudprducts1.ui.componentes.CardImages
import com.example.crudprducts1.ui.componentes.DialogResul
import com.example.crudprducts1.ui.componentes.IndicadorImage
import com.example.crudprducts1.ui.componentes.SaveButton
import com.example.crudprducts1.util.filtrarEntradaPrecio
import com.example.crudprducts1.util.validarNombre
import com.example.crudprducts1.util.validarPrecio
import com.example.crudprducts1.ui.componentes.TextFieldUi
import com.example.crudprducts1.ui.viewmodel.ProductViewModel
import com.example.crudprducts1.util.ProductState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AddProductScreen(
    productViewModel: ProductViewModel,
    navigate: () -> Unit
) {

    val product by productViewModel.product.collectAsState()
    val isEdit by productViewModel.isEdit.collectAsState()
    val listImage by productViewModel.imagesList.collectAsState()
    val result by productViewModel.result.collectAsState()

    val context: Context = LocalContext.current

    // ==========================================
    // VALIDACION
    // ==========================================

    val errorNombre = validarNombre(product.name)
    val errorPrecio = validarPrecio(product.precio)
    val formularioValido = errorNombre == null && errorPrecio == null

    // ==========================================
    // PAGINADOR DE IMÁGENES
    // ==========================================

    val pager = rememberPagerState(
        initialPage = 0,
        pageCount = {
            listImage.size
        }
    )

    // ==========================================
    // DATE PICKER
    // ==========================================

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        datePickerState.selectedDateMillis?.let { millis ->

                            val formattedDate =
                                SimpleDateFormat(
                                    "dd/MM/yyyy",
                                    Locale.getDefault()
                                ).format(
                                    Date(millis)
                                )

                            productViewModel.setFecha(
                                formattedDate
                            )
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }

    // ==========================================
    // RESULTADO
    // ==========================================

    LaunchedEffect(result) {

        when (result) {

            is ProductState.Success -> {

                Toast.makeText(
                    context,
                    "¡Guardado exitosamente!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is ProductState.Error -> {

                Toast.makeText(
                    context,
                    "Error al guardar el registro",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {}
        }
    }

    DialogResul(
        productState = result,

        reset = {
            productViewModel.reset()
        },

        aceptar = {

            productViewModel.reset()
            navigate()
        }
    )

    // ==========================================
    // BOTÓN ATRÁS
    // ==========================================

    BackHandler {
        navigate()
    }

    // ==========================================
    // SELECTOR DE IMÁGENES
    // ==========================================

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents()
        ) { uris: List<Uri> ->

            productViewModel.setList(
                listUri = uris,
                context = context
            )
        }

    // ==========================================
    // CONTENIDO
    // ==========================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(
                rememberScrollState()
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = if (isEdit) {
                "Editar Servicio"
            } else {
                "Nuevo Servicio"
            },

            fontSize = 24.sp,

            fontWeight = FontWeight.Bold,

            color = MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // ==========================================
        // NOMBRE
        // ==========================================

        TextFieldUi(
            value = product.name,
            label = "Nombre del servicio",
            keyboardType = KeyboardType.Text,

            onValueChange = { input ->

                if (
                    input.all {
                        it.isLetter() || it.isWhitespace()
                    }
                ) {
                    productViewModel.setNameProduct(input)
                }
            },
            errorMessage = if (product.name.isNotEmpty()) errorNombre else null
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // ==========================================
        // PRECIO
        // ==========================================

        TextFieldUi(
            value = product.precio,
            label = "Precio ($)",
            keyboardType = KeyboardType.Decimal,
            // Solo se marca en rojo si el usuario ya escribio algo: no tiene sentido
            // regañarle por un campo que aun no ha tocado.
            errorMessage = if (product.precio.isNotEmpty()) errorPrecio else null,

            onValueChange = { input ->
                // El filtro viejo dejaba pasar "1.2.3", ".", "12.3456" o vacio.
                filtrarEntradaPrecio(input)?.let { limpio ->
                    productViewModel.setPrecio(limpio)
                }
            }
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // ==========================================
        // HORA
        // ==========================================

        TextFieldUi(
            value = product.hora,
            label = "Hora (ej. 14:30)",
            keyboardType = KeyboardType.Text,

            onValueChange = {
                productViewModel.setHora(it)
            }
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // ==========================================
        // FECHA
        // ==========================================

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedTextField(
                value = product.fecha,
                onValueChange = {},
                readOnly = true,

                label = {
                    Text("Fecha de servicio")
                },

                modifier = Modifier.fillMaxWidth(),

                trailingIcon = {

                    IconButton(
                        onClick = {
                            showDatePicker = true
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha"
                        )
                    }
                }
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        showDatePicker = true
                    }
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // ==========================================
        // IMÁGENES
        // ==========================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = {
                        launcher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seleccionar Imágenes")
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                if (listImage.isNotEmpty()) {

                    HorizontalPager(
                        state = pager,
                        modifier = Modifier.height(200.dp)
                    ) { page ->

                        CardImages(
                            imagesState = listImage[page],

                            onDelete = {
                                productViewModel
                                    .deleteImageLocalAnRemote(
                                        position = page
                                    )
                            }
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    IndicadorImage(
                        cantidad = listImage.size,
                        currentPage = pager.currentPage
                    )

                } else {

                    Text(
                        text = "No hay imágenes seleccionadas",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // ==========================================
        // GUARDAR
        // ==========================================

        // El boton estaba siempre activo: se podian guardar productos sin nombre o con
        // el precio en blanco, y luego el resumen del inventario los contaba como
        // "sin precio".
        SaveButton(
            text = if (isEdit) {
                "Actualizar Registro"
            } else {
                "Guardar Registro"
            },

            enabled = formularioValido,

            onClick = {

                if (isEdit) {
                    productViewModel.updateProduct()
                } else {
                    productViewModel.saveProduct()
                }
            }
        )
    }
}