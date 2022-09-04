package com.example.moim

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.moim.databinding.ActivityRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity: AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var availableId = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val placeSpinner1 = binding.spinnerPlace1
        val placeAdapter1 = ArrayAdapter.createFromResource(this, R.array.place1, android.R.layout.simple_spinner_item)
        placeAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        placeSpinner1.adapter = placeAdapter1

        // TODO: 어떤 지역이냐에 따라 place2가 필요할 수 있음.
        //      현재 코드는 place2를 아예 고려하지 않았으므로, 추후 추가해보자.
        binding.spinnerPlace2.isEnabled = false
        binding.spinnerPlace2.isClickable = false

        val placeSpinner3 = binding.spinnerPlace3
        val placeAdapter3 = ArrayAdapter.createFromResource(this, R.array.place3, android.R.layout.simple_spinner_item)
        placeAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        placeSpinner3.adapter = placeAdapter3

        binding.edittextRegisterId.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                availableId = false
            }
        })

        binding.buttonCheckDuplicateId.setOnClickListener {
            val idText = binding.edittextRegisterId.text.toString()
            val context = this
            if (idText.isEmpty()) {
                Toast.makeText(this, "먼저 아이디를 입력해야 합니다.", Toast.LENGTH_SHORT).show()
            }
            else {
                retrofitHandler.checkDuplicateId(idText).enqueue(
                    object: Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "사용 가능한 아이디입니다!", Toast.LENGTH_SHORT).show()
                                availableId = true
                            }
                            else {
                                Toast.makeText(context, "이 아이디는 이미 사용중입니다.", Toast.LENGTH_SHORT).show()
                                Log.d("REGISTER", response.toString())
                            }
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Toast.makeText(context, "서버와 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("REGISTER", t.toString())
                        }
                    }
                )
            }
        }

        binding.buttonRegister.setOnClickListener {
            val idText = binding.edittextRegisterId.text.toString()
            val pwText = binding.edittextRegisterPassword.text.toString()
            val usernameText = binding.edittextRegisterUsername.text.toString()
            val ageText = binding.edittextRegisterAge.text.toString()
            val place1 = binding.spinnerPlace1.selectedItem.toString()
            val place3 = binding.spinnerPlace3.selectedItem.toString()

            if (idText.isEmpty() || pwText.isEmpty() || usernameText.isEmpty() || place1.isEmpty() || place3.isEmpty()) {
                Toast.makeText(this, "빈 칸을 채워주세요.", Toast.LENGTH_SHORT).show()
            }
            else if (!availableId) {
                Toast.makeText(this, "아이디 중복확인을 해주세요.", Toast.LENGTH_SHORT).show()
            }
            else {
                val registerInformation = RegisterInformation(
                    idText,
                    pwText,
                    if (ageText.isEmpty()) null else ageText.toInt(),
                    usernameText,
                    place1,
                    null,
                    place3
                )
                val context = this
                retrofitHandler.tryRegister(registerInformation).enqueue(
                    object : Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                                if (!isFinishing) finish()
                            } else {
                                Toast.makeText(context, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Toast.makeText(context, "서버와 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("REGISTER", t.toString())
                        }
                    }
                )
            }
        }
    }
}