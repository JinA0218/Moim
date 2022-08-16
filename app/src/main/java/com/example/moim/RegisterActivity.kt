package com.example.moim

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moim.databinding.ActivityRegisterBinding

class RegisterActivity: AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRegister.setOnClickListener {
            Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show()
            if (!isFinishing) finish()
        }
    }
}