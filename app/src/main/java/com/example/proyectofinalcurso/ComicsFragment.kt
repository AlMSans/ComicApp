package com.example.proyectofinalcurso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.proyectofinalcurso.databinding.FragmentComicsBinding
import com.google.android.material.tabs.TabLayoutMediator

class ComicsFragment : Fragment() {

    // ViewBinding para fragment_comics.xml
    private var _binding: FragmentComicsBinding? = null
    private val binding get() = _binding!!

    // Títulos que aparecerán en las pestañas
    private val tabTitles = arrayOf("Mis Cómics", "Favoritos")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComicsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1.  Asignamos el adapter al ViewPager2
        binding.viewPagerComics.adapter = ComicsPagerAdapter(this)

        // 2.  Vinculamos TabLayout + ViewPager2
        TabLayoutMediator(binding.tabLayoutComics, binding.viewPagerComics) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null        // Evitar fugas de memoria
    }

    /** Adapter interno que devuelve el fragmento correspondiente por posición */
    private inner class ComicsPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> MisComicsFragment()
                else -> FavoritosFragment()
            }
    }
}
