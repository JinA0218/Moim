package com.example.moim

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moim.databinding.ActivityCreatePartyBinding

class CreatePartyActivity: AppCompatActivity() {
    private lateinit var binding: ActivityCreatePartyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreatePartyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: 파티 만드는 것 구현하기
    }
}