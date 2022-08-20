package com.example.moim

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moim.databinding.ActivityPartyChatBinding

class PartyChatActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPartyChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPartyChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}