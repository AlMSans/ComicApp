package com.example.proyectofinalcurso

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.proyectofinalcurso.databinding.FragmentSubastasBinding
import com.example.proyectofinalcurso.subastas.MisSubastasFragment
import com.google.android.material.tabs.TabLayoutMediator


class SubastasFragment : Fragment() {

    private var _binding: FragmentSubastasBinding? = null
    private val binding get() = _binding!!

    private val tabTitles = arrayOf("Mis Subastas", "Subastas Disponibles")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubastasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPagerSubastas.adapter = SubastasPagerAdapter(this)

        TabLayoutMediator(binding.tabLayoutSubastas, binding.viewPagerSubastas) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class SubastasPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> MisSubastasFragment()
            else -> SDisponiblesFragment()
        }
    }
}
