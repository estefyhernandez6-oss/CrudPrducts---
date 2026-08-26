package com.example.crudprducts1.util

sealed class ImagesState {

    /**
     * Imagen recien elegida de la galeria, aun sin subir.
     *
     * `equals`/`hashCode` estan escritos a mano a proposito. Al ser un `data class` con
     * un `ByteArray` dentro, Kotlin generaba una comparacion por **referencia** del
     * array: dos imagenes con el mismo contenido salian distintas, y `indexOf`,
     * `remove` o `distinct` sobre la lista se comportaban de forma impredecible.
     */
    class ImageLocal(val byteArray: ByteArray) : ImagesState() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ImageLocal) return false
            return byteArray.contentEquals(other.byteArray)
        }

        override fun hashCode(): Int = byteArray.contentHashCode()
    }

    /** Imagen ya subida al bucket, referenciada por su URL publica. */
    data class ImageRemote(val url: String) : ImagesState()
}
