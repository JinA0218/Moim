package com.example.moim

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moim.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: AppDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDB.getInstance(this)!!

        binding.buttonGoRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.buttonLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val sharedManager = SharedManager(this)

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
                                sharedManager.saveUserId(idText)

                                runBlocking {
                                    withContext(Dispatchers.IO) {
                                        db.LDao().reset()
                                        db.MDao().reset()
                                        getMyPartyList(idText)
                                        getMyLikedList(idText)
                                    }
                                }

                                startActivity(intent)
                                finish()
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

    private fun getMyLikedList(userid: String) {
        retrofitHandler.getLikedPartyList(userid).enqueue(
            object: Callback<ResponseMixedParty> {
                override fun onResponse(
                    call: Call<ResponseMixedParty>,
                    response: Response<ResponseMixedParty>
                ) {
                    val mixed = response.body()

                    if (mixed != null) {
                        mixed.taxiParty.map {
                            val entry = LikedParty(it.common.partyId)
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    db.LDao().insert(entry)
                                }
                            }
                        }

                        mixed.mealParty.map {
                            val entry = LikedParty(it.common.partyId)
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    db.LDao().insert(entry)
                                }
                            }
                        }

                        mixed.nightMealParty.map {
                            val entry = LikedParty(it.common.partyId)
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    db.LDao().insert(entry)
                                }
                            }
                        }

                        mixed.studyParty.map {
                            val entry = LikedParty(it.common.partyId)
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    db.LDao().insert(entry)
                                }
                            }
                        }

                        mixed.customParty.map {
                            val entry = LikedParty(it.common.partyId)
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    db.LDao().insert(entry)
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseMixedParty>, t: Throwable) {
                    Log.d("LOGIN", t.toString())
                }
            }
        )
    }

    private fun getMyPartyList(userid: String) {
        retrofitHandler.getMyPartyList(userid).enqueue(
            object: Callback<ResponseMixedParty> {
                override fun onResponse(
                    call: Call<ResponseMixedParty>,
                    response: Response<ResponseMixedParty>
                ) {
                    val mixed = response.body()

                    if (mixed != null) {
                        mixed.taxiParty.map {
                            val entry = MyParty(it.common.partyId)
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    db.MDao().insert(entry)
                                }
                            }
                        }

                        mixed.mealParty.map {
                            val entry = MyParty(it.common.partyId)
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    db.MDao().insert(entry)
                                }
                            }
                        }

                        mixed.nightMealParty.map {
                            val entry = MyParty(it.common.partyId)
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    db.MDao().insert(entry)
                                }
                            }
                        }

                        mixed.studyParty.map {
                            val entry = MyParty(it.common.partyId)
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    db.MDao().insert(entry)
                                }
                            }
                        }

                        mixed.customParty.map {
                            val entry = MyParty(it.common.partyId)
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    db.MDao().insert(entry)
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseMixedParty>, t: Throwable) {
                    Log.d("LOGIN", t.toString())
                }
            }
        )
    }

}