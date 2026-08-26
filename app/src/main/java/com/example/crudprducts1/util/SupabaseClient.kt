package com.example.crudprducts1.util

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://nyjzbjtcpritqowohvkj.supabase.co",
        supabaseKey = "sb_publishable_HVEN4YwVJ5ic2IFthDJ94g_DilQBlMK"
    ) {
        install(Storage)
    }
}