package com.example.moim

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moim.databinding.ActivityLoginBinding

class LoginActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonGoRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.buttonLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val sharedManager = SharedManager(this)

            // TODO: 서버에 로그인 쿼리를 보내는 것과, 답신을 구현한다.
            //      서버는 쿼리에 대해 다음 답변을 보낸다.
            //      200: 성공! 응답으로 유저 닉네임을 가져온다.
            //      400: 실패
            sharedManager.saveUsername("TEMP")
            startActivity(intent)
        }
    }

}