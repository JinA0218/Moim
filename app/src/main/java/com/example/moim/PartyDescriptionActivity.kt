package com.example.moim

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moim.databinding.ActivityPartyDescriptionBinding

class PartyDescriptionActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPartyDescriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPartyDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val partyInformation = intent.extras?.getSerializable("party_info")
        val partyType = intent.extras?.getString("party_type")
    }
}