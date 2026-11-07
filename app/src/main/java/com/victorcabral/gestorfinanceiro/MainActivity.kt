package com.victorcabral.gestorfinanceiro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.victorcabral.gestorfinanceiro.ui.navigation.AppNavGraph
import com.victorcabral.gestorfinanceiro.ui.navigation.resolveStartDestination
import com.victorcabral.gestorfinanceiro.ui.theme.GestorFinanceiroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestorFinanceiroTheme {
                AppNavGraph(startDestination = resolveStartDestination(this))
            }
        }
    }
}
