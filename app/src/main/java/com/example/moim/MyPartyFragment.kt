package com.example.moim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.moim.databinding.FragmentMyPartyBinding

class MyPartyFragment: Fragment() {
    private lateinit var binding: FragmentMyPartyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMyPartyBinding.inflate(layoutInflater)

        return binding.root
    }
}