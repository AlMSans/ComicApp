package com.example.proyectofinalcurso

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.widget.Toolbar

class MainPanelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main_panel)



        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Cargar fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(HomeFragment()) // Muestra el fragmento de inicio
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment()) // Fragmento de Inicio
                R.id.nav_search -> loadFragment(SearchFragment()) // Fragmento de Búsqueda
                R.id.nav_profile -> loadFragment(ProfileFragment()) // Fragmento de Perfil
                R.id.nav_mis_comics->loadFragment(ComicsFragment())//Fragmento de los comics
                R.id.nav_messages -> loadFragment(ChatFragment()) // Fragmento de Mensajes

            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
