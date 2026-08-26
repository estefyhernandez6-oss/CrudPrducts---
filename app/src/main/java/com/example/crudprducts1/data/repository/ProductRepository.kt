package com.example.crudprducts1.data.repository

import com.example.crudprducts1.Product
import com.example.crudprducts1.data.model.User
import com.example.crudprducts1.util.ImagesState
import com.example.crudprducts1.util.ProductState
import android.util.Log
import com.example.crudprducts1.util.SupabaseClient
import kotlinx.coroutines.tasks.await
// `client.storage` es una propiedad de extension de supabase-kt, no un miembro:
// sin este import da "Unresolved reference 'storage'".
import io.github.jan.supabase.storage.storage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
// --- IMPORTACIONES NECESARIAS PARA FIREBASE REALTIME DATABASE ---
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class ProductRepository {

    // =====================================================
    // FIREBASE AUTHENTICATION
    // =====================================================

    private val auth: FirebaseAuth =
        FirebaseAuth.getInstance()


    // =====================================================
    // FIREBASE REALTIME DATABASE
    // =====================================================

    private val reference: DatabaseReference =
        FirebaseDatabase
            .getInstance()
            .getReference("product")

    /** Listener activo de la lista de productos, para poder soltarlo despues. */
    private var productsListener: ValueEventListener? = null


    // =====================================================
    // REGISTRAR USUARIO
    // =====================================================

    fun createUserEmailPassword(
        user: User,
        result: (ProductState<String>) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(
            user.email,
            user.password
        )
            .addOnSuccessListener { authResult ->

                val uid = authResult.user?.uid ?: ""

                // El formulario de registro pide "Nombre" y hasta ahora se descartaba:
                // se creaba la cuenta y el nombre no llegaba a ninguna parte. Se guarda
                // como displayName del perfil de Firebase Auth.
                val perfil = userProfileChangeRequest {
                    displayName = user.name.trim()
                }

                authResult.user
                    ?.updateProfile(perfil)
                    ?.addOnFailureListener { e ->
                        Log.e("AUTH", "No se pudo guardar el nombre del perfil", e)
                    }

                result(
                    ProductState.Success(
                        "Cuenta creada correctamente. UID: $uid"
                    )
                )
            }
            .addOnFailureListener { exception ->

                result(
                    ProductState.Error(
                        exception.message
                            ?: "Error al crear la cuenta"
                    )
                )
            }
    }


    // =====================================================
    // INICIAR SESIÓN
    // =====================================================

    fun signinEmailPassword(
        email: String,
        password: String,
        result: (ProductState<String>) -> Unit
    ) {

        auth.signInWithEmailAndPassword(
            email,
            password
        )
            .addOnSuccessListener {

                result(
                    ProductState.Success(
                        "Inicio de sesión correcto"
                    )
                )
            }
            .addOnFailureListener { exception ->

                result(
                    ProductState.Error(
                        exception.message
                            ?: "Error al iniciar sesión"
                    )
                )
            }
    }


    // =====================================================
    // LOGIN CON GOOGLE
    // =====================================================

    fun signInGoogle(
        idToken: String,
        result: (ProductState<String>) -> Unit
    ) {

        val credential =
            GoogleAuthProvider.getCredential(
                idToken,
                null
            )

        auth.signInWithCredential(credential)
            .addOnSuccessListener {

                result(
                    ProductState.Success(
                        "Inicio de sesión con Google correcto"
                    )
                )
            }
            .addOnFailureListener { exception ->

                result(
                    ProductState.Error(
                        exception.message
                            ?: "Error al iniciar sesión con Google"
                    )
                )
            }
    }


    // =====================================================
    // CERRAR SESIÓN
    // =====================================================

    fun logout() {
        auth.signOut()
    }


    // =====================================================
    // OBTENER USUARIO ACTUAL
    // =====================================================

    fun getCurrentUser() =
        auth.currentUser


    // =====================================================
    // GUARDAR PRODUCTO
    // =====================================================

    suspend fun saveProduct(
        product: Product,
        currentImage: List<ImagesState>,
        result: (ProductState<String>) -> Unit
    ) {

        result(ProductState.Loading)

        try {

            val uid =
                product.id.ifEmpty {
                    UUID.randomUUID().toString()
                }

            val listUrls =
                getPublicUrl(currentImage)

            val productToSave =
                product.copy(
                    id = uid,
                    images = listUrls
                )

            reference
                .child(uid)
                .setValue(productToSave)
                .addOnSuccessListener {

                    result(
                        ProductState.Success(
                            "Registro realizado con éxito"
                        )
                    )
                }
                .addOnFailureListener { exception ->

                    result(
                        ProductState.Error(
                            exception.message
                                ?: "Error al guardar"
                        )
                    )
                }

        } catch (e: Exception) {

            result(
                ProductState.Error(
                    e.message
                        ?: "Error al guardar el producto"
                )
            )
        }
    }


    // =====================================================
    // ACTUALIZAR PRODUCTO
    // =====================================================

    suspend fun updateProduct(
        product: Product,
        currentImage: List<ImagesState>,
        result: (ProductState<String>) -> Unit
    ) {

        result(ProductState.Loading)

        try {

            val listUrls =
                getPublicUrl(currentImage)

            val productToUpdate =
                product.copy(
                    images = listUrls
                )

            reference
                .child(product.id)
                .setValue(productToUpdate)
                .addOnSuccessListener {

                    result(
                        ProductState.Success(
                            "Registro modificado con éxito"
                        )
                    )
                }
                .addOnFailureListener { exception ->

                    result(
                        ProductState.Error(
                            exception.message
                                ?: "Error al actualizar"
                        )
                    )
                }

        } catch (e: Exception) {

            result(
                ProductState.Error(
                    e.message
                        ?: "Error al actualizar el producto"
                )
            )
        }
    }


    // =====================================================
    // OBTENER URLS PÚBLICAS DE SUPABASE
    // =====================================================

    private suspend fun getPublicUrl(
        currentImage: List<ImagesState>
    ): List<String> {

        val client =
            SupabaseClient.client

        val listPublicUrl =
            mutableListOf<String>()

        currentImage.forEach { imageState ->

            when (imageState) {

                is ImagesState.ImageLocal -> {

                    val imageName =
                        "${System.currentTimeMillis()}.jpg"

                    client
                        .storage
                        .from("image")
                        .upload(
                            path = imageName,
                            data = imageState.byteArray
                        )

                    val publicUrl =
                        client
                            .storage
                            .from("image")
                            .publicUrl(imageName)

                    listPublicUrl.add(
                        publicUrl
                    )
                }

                is ImagesState.ImageRemote -> {

                    listPublicUrl.add(
                        imageState.url
                    )
                }
            }
        }

        return listPublicUrl
    }


    // =====================================================
    // OBTENER TODOS LOS PRODUCTOS
    // =====================================================

    /**
     * Escucha la lista de productos en tiempo real.
     *
     * El listener se guarda en [productsListener] para poder soltarlo en
     * [removeProductsListener]. Antes se registraba y nunca se quitaba: cada vez que se
     * creaba un ProductViewModel se apilaba otro listener sobre la misma referencia,
     * y ninguno se liberaba al destruirse la pantalla.
     */
    fun getAllProduct(
        result: (ProductState<List<Product>>) -> Unit
    ) {

        result(ProductState.Loading)

        removeProductsListener()

        productsListener = object : ValueEventListener {

                override fun onDataChange(
                    snapshot: DataSnapshot
                ) {

                    val listProduct =
                        mutableListOf<Product>()

                    for (
                    childSnapshot
                    in snapshot.children
                    ) {

                        val product =
                            childSnapshot.getValue(
                                Product::class.java
                            )

                        product?.let {
                            listProduct.add(it)
                        }
                    }

                    result(
                        ProductState.Success(
                            listProduct
                        )
                    )
                }

                override fun onCancelled(
                    error: DatabaseError
                ) {

                    result(
                        ProductState.Error(
                            error.message
                        )
                    )
                }
        }

        productsListener?.let { reference.addValueEventListener(it) }
    }


    /** Suelta el listener de productos. Llamar desde `ProductViewModel.onCleared()`. */
    fun removeProductsListener() {
        productsListener?.let { reference.removeEventListener(it) }
        productsListener = null
    }


    // =====================================================
    // ELIMINAR PRODUCTO
    // =====================================================

    /**
     * Borra el producto y **sus imagenes del bucket de Supabase**.
     *
     * Antes solo hacia `removeValue()`: el registro desaparecia de la base pero los
     * archivos quedaban huerfanos en el bucket para siempre, ocupando (y costando)
     * espacio que nadie volveria a referenciar. Ademas fallaba en silencio.
     */
    suspend fun deleteProduct(
        id: String,
        result: (ProductState<String>) -> Unit = {}
    ) {

        result(ProductState.Loading)

        try {

            // 1. Leer el producto para saber que imagenes cuelgan de el.
            val snapshot =
                reference
                    .child(id)
                    .get()
                    .await()

            val product =
                snapshot.getValue(Product::class.java)

            // 2. Borrar sus archivos del bucket. Se extrae el nombre del final de la
            //    URL publica, que es como se subieron.
            val nombres =
                product
                    ?.images
                    .orEmpty()
                    .map { it.substringAfterLast("/") }
                    .filter { it.isNotBlank() }

            if (nombres.isNotEmpty()) {
                try {
                    SupabaseClient
                        .client
                        .storage
                        .from("image")
                        .delete(nombres)
                } catch (e: Exception) {
                    // Que falle el bucket no debe impedir borrar el registro:
                    // quedaria un producto fantasma imposible de eliminar.
                    Log.e("CRUD", "No se pudieron borrar las imagenes de $id", e)
                }
            }

            // 3. Borrar el registro.
            reference
                .child(id)
                .removeValue()
                .await()

            result(ProductState.Success("Producto eliminado"))

        } catch (e: Exception) {
            Log.e("CRUD", "No se pudo eliminar el producto $id", e)
            result(
                ProductState.Error(
                    e.message ?: "Error al eliminar el producto"
                )
            )
        }
    }


    // =====================================================
    // ELIMINAR IMAGEN DE SUPABASE
    // =====================================================

    /**
     * Borra del bucket los archivos indicados por nombre.
     *
     * Sustituye al antiguo `deleteImageSupabase`, que ademas de borrar el archivo
     * manipulaba a mano el array `images` del producto en la base. Eso sobraba: al
     * guardar, `updateProduct` reescribe la lista completa. Y era peligroso, porque
     * borraba el archivo en el momento de quitar la imagen de la pantalla, antes de
     * que el usuario confirmara.
     */
    suspend fun deleteImagesFromBucket(imageNames: List<String>) {

        val nombres = imageNames.filter { it.isNotBlank() }
        if (nombres.isEmpty()) return

        try {
            SupabaseClient
                .client
                .storage
                .from("image")
                .delete(nombres)
        } catch (e: Exception) {
            Log.e("CRUD", "No se pudieron borrar del bucket: $nombres", e)
        }
    }


    @Deprecated("Borraba el archivo antes de confirmar. Usar deleteImagesFromBucket.")
    suspend fun deleteImageSupabase(
        imageName: String,
        productId: String,
        position: Int
    ) {

        try {

            // Eliminar imagen de Supabase

            SupabaseClient
                .client
                .storage
                .from("image")
                .delete(
                    listOf(imageName)
                )


            // Obtener producto de Firebase

            reference
                .child(productId)
                .get()
                .addOnSuccessListener { snapshot ->

                    if (snapshot.exists()) {

                        val product =
                            snapshot.getValue(
                                Product::class.java
                            )

                        product?.let { prod ->

                            val updatedImages =
                                prod.images.toMutableList()

                            if (
                                position in
                                updatedImages.indices
                            ) {

                                updatedImages.removeAt(
                                    position
                                )

                                reference
                                    .child(productId)
                                    .child("images")
                                    .setValue(
                                        updatedImages
                                    )
                            }
                        }
                    }
                }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
}