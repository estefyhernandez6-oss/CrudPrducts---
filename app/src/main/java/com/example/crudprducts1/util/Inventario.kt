package com.example.crudprducts1.util

import com.example.crudprducts1.Product

/**
 * Criterios de ordenacion de la lista de productos.
 *
 * `etiqueta` es lo que se muestra en el chip de la pantalla.
 */
enum class OrdenProducto(val etiqueta: String) {
    RECIENTE("Recientes"),
    NOMBRE("Nombre"),
    PRECIO_ASC("Precio ↑"),
    PRECIO_DESC("Precio ↓")
}

/**
 * Cifras de cabecera de la pantalla de productos.
 *
 * `total` es la suma de los precios de los productos **visibles**, no de todos: si hay
 * una busqueda activa, el resumen responde a lo que el usuario esta viendo.
 */
data class ResumenInventario(
    val cantidad: Int = 0,
    val total: Double = 0.0,
    val sinPrecio: Int = 0
)

/**
 * Convierte el campo `precio` (un String libre) en numero.
 *
 * El formulario no valida el formato, asi que aqui llega de todo: "12,50", "$ 12.50",
 * "12.50 USD" o texto sin numeros. Se limpia todo lo que no sea digito o separador
 * decimal y se normaliza la coma a punto. Si no queda nada parseable, devuelve null:
 * ese producto cuenta como "sin precio" en vez de sumar cero en silencio.
 */
fun Product.precioNumerico(): Double? {
    val limpio = precio
        .replace(',', '.')
        .filter { it.isDigit() || it == '.' }
        .trim('.')

    if (limpio.isBlank()) return null

    // Con varios puntos ("1.234.50") nos quedamos con el ultimo como decimal.
    val partes = limpio.split('.')
    val normalizado =
        if (partes.size <= 2) limpio
        else partes.dropLast(1).joinToString("") + "." + partes.last()

    return normalizado.toDoubleOrNull()
}

/** Filtra por nombre. Sin distinguir mayusculas ni acentos del teclado. */
fun List<Product>.filtrarPor(consulta: String): List<Product> {
    val q = consulta.trim()
    if (q.isBlank()) return this
    return filter { it.name.contains(q, ignoreCase = true) }
}

/** Ordena segun el criterio elegido. Los productos sin precio van al final. */
fun List<Product>.ordenarPor(orden: OrdenProducto): List<Product> = when (orden) {

    OrdenProducto.RECIENTE ->
        // `fecha` y `hora` son cadenas del formulario; ordenar por texto descendente
        // aproxima "lo mas reciente primero" sin inventar un campo nuevo.
        sortedWith(compareByDescending<Product> { it.fecha }.thenByDescending { it.hora })

    OrdenProducto.NOMBRE ->
        sortedBy { it.name.lowercase() }

    OrdenProducto.PRECIO_ASC ->
        sortedWith(compareBy(nullsLast()) { it.precioNumerico() })

    OrdenProducto.PRECIO_DESC ->
        sortedWith(compareByDescending(nullsLast()) { it.precioNumerico() })
}

/** Calcula el resumen de una lista ya filtrada. */
fun List<Product>.resumen(): ResumenInventario {
    val precios = map { it.precioNumerico() }
    return ResumenInventario(
        cantidad = size,
        total = precios.filterNotNull().sum(),
        sinPrecio = precios.count { it == null }
    )
}

// =====================================================
// VALIDACION DEL PRECIO EN EL FORMULARIO
// =====================================================

/**
 * Filtra lo que el usuario teclea en el campo de precio.
 *
 * El filtro anterior era `input.all { it.isDigit() || it == '.' }`, que dejaba pasar
 * `1.2.3`, `.`, `12.3456` o cadena vacia. Aqui se aplican las reglas de verdad:
 * un solo separador decimal, maximo dos decimales y nada de punto inicial suelto.
 *
 * Devuelve el texto aceptado, o `null` si la pulsacion debe ignorarse.
 */
fun filtrarEntradaPrecio(entrada: String): String? {

    if (entrada.isEmpty()) return ""

    // Se acepta la coma como decimal y se normaliza a punto: en Ecuador mucha gente
    // teclea "12,50".
    val texto = entrada.replace(',', '.')

    if (!texto.all { it.isDigit() || it == '.' }) return null

    // Un unico separador decimal.
    if (texto.count { it == '.' } > 1) return null

    // ".50" se convierte en "0.50" en vez de rechazarse.
    val conCero = if (texto.startsWith(".")) "0$texto" else texto

    // Maximo dos decimales.
    val decimales = conCero.substringAfter('.', "")
    if (decimales.length > 2) return null

    // Un limite sano de parte entera: 9 digitos.
    if (conCero.substringBefore('.').length > 9) return null

    return conCero
}

/**
 * Valida el precio ya escrito, de cara a permitir o no el guardado.
 *
 * Devuelve el mensaje de error, o `null` si es valido.
 */
fun validarPrecio(precio: String): String? = when {
    precio.isBlank() -> "Indica el precio"
    precio == "." -> "Precio incompleto"
    precio.toDoubleOrNull() == null -> "Formato de precio no válido"
    precio.toDouble() <= 0.0 -> "El precio debe ser mayor que 0"
    else -> null
}

/** Valida el nombre del producto. */
fun validarNombre(nombre: String): String? = when {
    nombre.isBlank() -> "Indica el nombre del producto"
    nombre.trim().length < 2 -> "Nombre demasiado corto"
    else -> null
}
