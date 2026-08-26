package com.example.crudprducts1.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crudprducts1.Product
import com.example.crudprducts1.data.repository.ProductRepository
import com.example.crudprducts1.util.ImagesState
import com.example.crudprducts1.util.OrdenProducto
import com.example.crudprducts1.util.ProductState
import com.example.crudprducts1.util.ResumenInventario
import com.example.crudprducts1.util.filtrarPor
import com.example.crudprducts1.util.ordenarPor
import com.example.crudprducts1.util.resumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _product = MutableStateFlow(Product())
    val product: StateFlow<Product> get() = _product

    private val _imagesList = MutableStateFlow<List<ImagesState>>(emptyList())
    val imagesList: StateFlow<List<ImagesState>> get() = _imagesList

    private val _result = MutableStateFlow<ProductState<String>>(ProductState.Idle)
    val result: StateFlow<ProductState<String>> get() = _result

    private val _listProducts = MutableStateFlow<ProductState<List<Product>>>(ProductState.Idle)
    val listProduct: StateFlow<ProductState<List<Product>>> get() = _listProducts

    /**
     * Resultado del BORRADO, separado de [_result] a proposito.
     *
     * Compartirlos hacia que, tras borrar un producto en Home, `_result` quedara en
     * Success; al abrir despues "Anadir producto" saltaba un dialogo de "guardado"
     * fantasma antes de tocar nada.
     */
    private val _deleteResult = MutableStateFlow<ProductState<String>>(ProductState.Idle)
    val deleteResult: StateFlow<ProductState<String>> get() = _deleteResult

    /**
     * Imagenes remotas que el usuario quito en pantalla y que se borraran del bucket
     * **solo si confirma** el guardado. Antes se borraban en el acto: si cancelaba la
     * edicion, el archivo ya no existia pero el producto lo seguia referenciando, y
     * quedaba una imagen rota.
     */
    private val pendingBucketDeletions = mutableListOf<String>()

    val isEdit = MutableStateFlow(false)

    // ==========================================================
    // BUSQUEDA, ORDEN Y RESUMEN
    // ==========================================================

    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> get() = _consulta

    private val _orden = MutableStateFlow(OrdenProducto.RECIENTE)
    val orden: StateFlow<OrdenProducto> get() = _orden

    /**
     * Lista que realmente se pinta: la de Firebase, filtrada y ordenada.
     *
     * Se deriva con `combine` en vez de calcularse en el Composable para que no se
     * rehaga en cada recomposicion y para que la busqueda sobreviva a giros de pantalla.
     */
    val productosVisibles: StateFlow<ProductState<List<Product>>> =
        combine(_listProducts, _consulta, _orden) { estado, q, orden ->
            when (estado) {
                is ProductState.Success ->
                    ProductState.Success(
                        estado.data.filtrarPor(q).ordenarPor(orden)
                    )
                else -> estado
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProductState.Idle
        )

    /** Cifras de cabecera, calculadas sobre lo que el usuario esta viendo. */
    val resumenInventario: StateFlow<ResumenInventario> =
        productosVisibles.map { estado ->
            if (estado is ProductState.Success) estado.data.resumen()
            else ResumenInventario()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ResumenInventario()
        )

    fun setConsulta(texto: String) {
        _consulta.value = texto
    }

    fun setOrden(nuevo: OrdenProducto) {
        _orden.value = nuevo
    }

    init {
        getAllProduct()
    }

    /** Hay sesion abierta (Firebase Auth la recuerda entre arranques). */
    fun isLoggedIn(): Boolean = repository.getCurrentUser() != null

    fun logout() {
        repository.logout()
        reset()
    }

    fun setProductForEdit(product: Product) {
        _product.value = product
        isEdit.value = true
        val list = mutableListOf<ImagesState>()
        product.images.forEach { url ->
            list.add(ImagesState.ImageRemote(url))
        }
        _imagesList.value = list
    }

    fun setList(listUri: List<Uri>, context: Context) {
        val list = _imagesList.value.toMutableList()
        listUri.forEach { uri ->
            val byteArray = uriToByteArray(uri, context)
            byteArray?.let {
                list.add(ImagesState.ImageLocal(it))
            }
        }
        _imagesList.value = list
    }

    /**
     * Quita la imagen de la pantalla. Si era remota, **encola** su borrado del bucket
     * en vez de ejecutarlo: se hara efectivo al guardar, y se descartara si el usuario
     * cancela.
     */
    fun deleteImageLocalAnRemote(position: Int) {
        val currentImages = _imagesList.value.toMutableList()
        if (position in currentImages.indices) {
            val image = currentImages[position]
            if (image is ImagesState.ImageRemote) {
                pendingBucketDeletions.add(image.url.substringAfterLast("/"))
            }
            currentImages.removeAt(position)
            _imagesList.value = currentImages
        }
    }

    /** Ejecuta los borrados encolados. Solo tras un guardado correcto. */
    private suspend fun aplicarBorradosPendientes() {
        if (pendingBucketDeletions.isEmpty()) return
        repository.deleteImagesFromBucket(pendingBucketDeletions.toList())
        pendingBucketDeletions.clear()
    }

    private fun uriToByteArray(uri: Uri, context: Context): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun setNameProduct(name: String) {
        _product.update { it.copy(name = name) }
    }

    fun setPrecio(precio: String) {
        _product.update { it.copy(precio = precio) }
    }

    fun setFecha(fecha: String) {
        _product.update { it.copy(fecha = fecha) }
    }

    fun setHora(hora: String) {
        _product.update { it.copy(hora = hora) }
    }

    fun setId(id: String) {
        _product.update { it.copy(id = id) }
    }

    fun saveProduct() {
        viewModelScope.launch {
            repository.saveProduct(
                product = _product.value,
                currentImage = _imagesList.value
            ) { res ->
                _result.value = res
                if (res is ProductState.Success) {
                    viewModelScope.launch { aplicarBorradosPendientes() }
                }
            }
        }
    }

    fun updateProduct() {
        viewModelScope.launch {
            repository.updateProduct(
                product = _product.value,
                currentImage = _imagesList.value
            ) { res ->
                _result.value = res
                if (res is ProductState.Success) {
                    viewModelScope.launch { aplicarBorradosPendientes() }
                }
            }
        }
    }

    fun getAllProduct() {
        viewModelScope.launch {
            repository.getAllProduct { res ->
                _listProducts.value = res
            }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            repository.deleteProduct(id) { res ->
                _deleteResult.value = res
            }
        }
    }

    /**
     * Suelta el listener de Realtime Database. Sin esto quedaba registrado de por vida:
     * cada ProductViewModel nuevo apilaba otro sobre la misma referencia.
     */
    override fun onCleared() {
        super.onCleared()
        repository.removeProductsListener()
    }

    /**
     * Vuelve al estado inicial. Descarta los borrados encolados **sin ejecutarlos**:
     * cancelar una edicion no debe tocar el bucket.
     */
    fun reset() {
        _product.value = Product()
        _result.value = ProductState.Idle
        _imagesList.value = emptyList()
        pendingBucketDeletions.clear()
        isEdit.value = false
    }

    /** Limpia el aviso de borrado una vez mostrado. */
    fun clearDeleteResult() {
        _deleteResult.value = ProductState.Idle
    }
}