package com.smekhnyov.carts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smekhnyov.carts.ui.CartsApp
import com.smekhnyov.carts.ui.theme.CartsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CartsTheme {
                CartsApp()
            }
        }
    }
}
