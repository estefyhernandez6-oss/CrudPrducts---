package com.example.crudprducts1

data class Product (
    val id: String="",
    val name: String="",
    val fecha: String="",
    val precio: String= "",
    val hora: String="",
    val images: List<String> = emptyList()

)


