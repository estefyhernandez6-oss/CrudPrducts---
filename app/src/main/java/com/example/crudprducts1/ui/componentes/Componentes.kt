package com.example.crudprducts1.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.crudprducts1.Product
import com.example.crudprducts1.util.ImagesState
import com.example.crudprducts1.util.ProductState

// =====================================================
// BOTÓN LOGIN
// =====================================================

@Composable
fun ButtonLogin(
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Login")
    }
}

// =====================================================
// TEXT FIELD
// =====================================================

@Composable
fun TextFieldUi(
    value: String,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    // Mensaje de validacion. Si es null el campo se pinta normal; si trae texto, el
    // campo se marca en rojo y lo muestra debajo.
    errorMessage: String? = null,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label)
        },
        isError = errorMessage != null,
        supportingText = errorMessage?.let { msg ->
            { Text(text = msg) }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(
            color = Color.Black
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = Color.Gray,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray
        )
    )
}

// =====================================================
// DIÁLOGO DE RESULTADO
// =====================================================

@Composable
fun DialogResul(
    productState: ProductState<String>,
    reset: () -> Unit,
    aceptar: () -> Unit
) {
    when (productState) {

        is ProductState.Loading -> {

            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = {
                    Text("Procesando")
                },
                text = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(16.dp)
                        )

                        Text("Guardando datos...")
                    }
                }
            )
        }

        is ProductState.Success -> {

            AlertDialog(
                onDismissRequest = reset,
                title = {
                    Text("Éxito")
                },
                text = {
                    Text(productState.data)
                },
                confirmButton = {
                    TextButton(
                        onClick = aceptar
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }

        is ProductState.Error -> {

            AlertDialog(
                onDismissRequest = reset,
                title = {
                    Text(
                        "Error",
                        color = MaterialTheme.colorScheme.error
                    )
                },
                text = {
                    Text(productState.message)
                },
                confirmButton = {
                    TextButton(
                        onClick = reset
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }

        ProductState.Idle -> {
        }
    }
}

// =====================================================
// TARJETA DE PRODUCTO
// =====================================================

@Composable
fun CardProduct(
    product: Product,
    delete: () -> Unit,
    edit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                edit()
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            if (product.images.isNotEmpty()) {

                AsyncImage(
                    model = product.images.first(),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(
                            RoundedCornerShape(8.dp)
                        ),
                    contentScale = ContentScale.Crop
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = product.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "$${product.precio}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row {

                    IconButton(
                        onClick = edit
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar producto",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = delete
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar producto",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// DIÁLOGO ELIMINAR
// =====================================================

@Composable
fun DeleateDialog(
    showDialog: Boolean,
    aceptar: () -> Unit,
    cancelar: () -> Unit
) {

    if (showDialog) {

        AlertDialog(
            onDismissRequest = cancelar,
            title = {
                Text("Eliminar producto")
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar este producto?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = aceptar
                ) {
                    Text(
                        "Eliminar",
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = cancelar
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// =====================================================
// BOTÓN FLOTANTE
// =====================================================

@Composable
fun ButtonNavigate(
    navigate: () -> Unit
) {
    FloatingActionButton(
        onClick = navigate,
        containerColor = MaterialTheme.colorScheme.primary
    ) {

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Agregar producto",
            tint = Color.White
        )
    }
}

// =====================================================
// TARJETA DE IMAGEN
// =====================================================

@Composable
fun CardImages(
    imagesState: ImagesState,
    onDelete: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(
                RoundedCornerShape(12.dp)
            )
    ) {

        when (imagesState) {

            is ImagesState.ImageLocal -> {

                AsyncImage(
                    model = imagesState.byteArray,
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }

            is ImagesState.ImageRemote -> {

                AsyncImage(
                    model = imagesState.url,
                    contentDescription = "Imagen remota",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(
                    Color.White.copy(alpha = 0.7f),
                    CircleShape
                )
        ) {

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar imagen",
                tint = Color.Red
            )
        }
    }
}

// =====================================================
// INDICADOR DE IMÁGENES
// =====================================================

@Composable
fun IndicadorImage(
    cantidad: Int,
    currentPage: Int
) {

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        repeat(cantidad) { index ->

            val color =
                if (currentPage == index) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.LightGray
                }

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

// =====================================================
// BOTÓN GUARDAR
// =====================================================

@Composable
fun SaveButton(
    text: String = "Guardar Producto",
    enabled: Boolean = true,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black
        )
    ) {

        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}