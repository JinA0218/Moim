package com.example.moim

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moim.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            val idText = binding.edittextLoginId.text.toString()
            val pwText = binding.edittextLoginPassword.text.toString()
            val context = this

            if (idText.isEmpty()) {
                Toast.makeText(this, "ID를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            else if (pwText.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            else {
                val loginInformation = LoginInformation(idText, pwText)
                retrofitHandler.tryLogin(loginInformation).enqueue(
                    object: Callback<ResponseLogin> {
                        override fun onResponse(call: Call<ResponseLogin>, response: Response<ResponseLogin>) {
                            if (response.isSuccessful) {
                                Log.d("LOGIN", response.body()!!.toString())
                                sharedManager.saveUsername(response.body()!!.username)
                                startActivity(intent)
                            }
                            else {
                                Toast.makeText(context, "ID 또는 비밀번호를 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseLogin>, t: Throwable) {
                            Toast.makeText(context, "서버와 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("LOGIN", t.toString())
                        }
                    }
                )

            }
        }
    }

}