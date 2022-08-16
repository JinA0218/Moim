package com.example.moim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.moim.databinding.FragmentLikedTabBinding

class LikedFragment: Fragment() {
    private lateinit var binding: FragmentLikedTabBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLikedTabBinding.inflate(layoutInflater)

        return binding.root
    }
}