package com.example.moim

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moim.databinding.ActivityCreatePartyBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreatePartyActivity: AppCompatActivity() {
    private lateinit var binding: ActivityCreatePartyBinding
    private lateinit var sharedManager: SharedManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreatePartyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedManager = SharedManager(this)
        val partyTypeNumber = intent.extras?.getInt("party_type_number")!!

        actionBar?.title = partyTypeKorean(partyTypeNumber)

        // TODO: 파티 만드는 것 구현하기

        // 몇몇 View 들을 숨겨줘야 한다. 레이아웃도 맞춰서 조정해야 한다.
        when (partyTypeNumber) {
            PartyTypeNumber.Taxi -> {
                binding.root.removeView(binding.spinnerMealType)
                binding.root.removeView(binding.spinnerMealOutside)
                binding.textPartyOption1.text = "출발점"
                binding.textPartyOption2.text = "도착점"
            }
            PartyTypeNumber.Meal, PartyTypeNumber.NightMeal -> {
                binding.root.removeView(binding.edittextTaxiStart)
                binding.root.removeView(binding.edittextTaxiDestination)
                binding.textPartyOption1.text = "음식 종류"
                binding.textPartyOption2.text = "먹는 위치"

            }
            PartyTypeNumber.Study, PartyTypeNumber.Custom -> {
                binding.root.removeView(binding.spinnerMealType)
                binding.root.removeView(binding.spinnerMealOutside)
                binding.root.removeView(binding.edittextTaxiStart)
                binding.root.removeView(binding.edittextTaxiDestination)
                binding.root.removeView(binding.textPartyOption1)
                binding.root.removeView(binding.textPartyOption2)
            }
            else -> throw Error("Should not happen!!")
        }

        binding.buttonBack.setOnClickListener {
            if (!isFinishing) finish()
        }

        binding.buttonPartyDate.setOnClickListener {
            // TODO: 날짜 스트링
        }

        binding.buttonCreateParty.setOnClickListener {
            val userId = sharedManager.getUserId()
            when (partyTypeNumber) {
                PartyTypeNumber.Taxi -> {
                    val timeString = "TODO"

                    retrofitHandler.createParty("taxi-party", TaxiParty(
                        common = CommonPartyAttributes(
                            partyId = -1,
                            partyName = binding.edittextPartyName.text.toString(),
                            partyHead = userId,
                            place = Place(
                                hasPlace = 1,
                                place1 = binding.spinnerPlace1.selectedItem.toString(),
                                place2 = binding.spinnerPlace2.selectedItem.toString(),
                                place3 = binding.spinnerPlace3.selectedItem.toString(),
                            ),
                            currentCount = 1,
                            maximumCount = binding.edittextPartyMaximumCount.text.toString().toInt(),
                            detailedDescription = binding.edittextPartyDescription.text.toString(),
                            countDifference = 0
                        ),
                        extra = TaxiExtra(
                            detailedStartPlace = binding.edittextTaxiStart.text.toString(),
                            destination = binding.edittextTaxiDestination.text.toString(),
                            partyDate = binding.buttonPartyDate.text.toString(),
                            partyTime = timeString
                        )
                    )).enqueue(object: Callback<ResponseCreateParty> {
                        override fun onResponse(
                            call: Call<ResponseCreateParty>,
                            response: Response<ResponseCreateParty>
                        ) {
                            TODO("On success, where should user go?")
                        }

                        override fun onFailure(call: Call<ResponseCreateParty>, t: Throwable) {
                            Toast.makeText(binding.root.context, "서버와 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("CREATE", t.toString())
                        }
                    })
                }
                PartyTypeNumber.Meal -> {
                    val timeString = "TODO"
                    val outside = if (binding.spinnerMealOutside.selectedItem.toString() == "배달 시키기") {
                        0
                    }
                    else {
                        1
                    }

                    retrofitHandler.createParty("meal-party", MealParty(
                        common = CommonPartyAttributes(
                            partyId = -1,
                            partyName = binding.edittextPartyName.text.toString(),
                            partyHead = userId,
                            place = Place(
                                hasPlace = 1,
                                place1 = binding.spinnerPlace1.selectedItem.toString(),
                                place2 = binding.spinnerPlace2.selectedItem.toString(),
                                place3 = binding.spinnerPlace3.selectedItem.toString(),
                            ),
                            currentCount = 1,
                            maximumCount = binding.edittextPartyMaximumCount.text.toString().toInt(),
                            detailedDescription = binding.edittextPartyDescription.text.toString(),
                            countDifference = 0
                        ),
                        extra = MealExtra(
                            mealType = binding.spinnerMealType.selectedItem.toString(),
                            outside = outside,
                            partyDate = binding.buttonPartyDate.text.toString(),
                            partyTime = timeString
                        )
                    )).enqueue(object: Callback<ResponseCreateParty> {
                        override fun onResponse(
                            call: Call<ResponseCreateParty>,
                            response: Response<ResponseCreateParty>
                        ) {
                            TODO("On success, where should user go?")
                        }

                        override fun onFailure(call: Call<ResponseCreateParty>, t: Throwable) {
                            Toast.makeText(binding.root.context, "서버와 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("CREATE", t.toString())
                        }
                    })
                }
                PartyTypeNumber.NightMeal -> {
                    val timeString = "TODO"
                    val outside = if (binding.spinnerMealOutside.selectedItem.toString() == "배달 시키기") {
                        0
                    }
                    else {
                        1
                    }

                    retrofitHandler.createParty("night-meal-party", MealParty(
                        common = CommonPartyAttributes(
                            partyId = -1,
                            partyName = binding.edittextPartyName.text.toString(),
                            partyHead = userId,
                            place = Place(
                                hasPlace = 1,
                                place1 = binding.spinnerPlace1.selectedItem.toString(),
                                place2 = binding.spinnerPlace2.selectedItem.toString(),
                                place3 = binding.spinnerPlace3.selectedItem.toString(),
                            ),
                            currentCount = 1,
                            maximumCount = binding.edittextPartyMaximumCount.text.toString().toInt(),
                            detailedDescription = binding.edittextPartyDescription.text.toString(),
                            countDifference = 0
                        ),
                        extra = MealExtra(
                            mealType = binding.spinnerMealType.selectedItem.toString(),
                            outside = outside,
                            partyDate = binding.buttonPartyDate.text.toString(),
                            partyTime = timeString
                        )
                    )).enqueue(object: Callback<ResponseCreateParty> {
                        override fun onResponse(
                            call: Call<ResponseCreateParty>,
                            response: Response<ResponseCreateParty>
                        ) {
                            TODO("On success, where should user go?")
                        }

                        override fun onFailure(call: Call<ResponseCreateParty>, t: Throwable) {
                            Toast.makeText(binding.root.context, "서버와 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("CREATE", t.toString())
                        }
                    })
                }
                PartyTypeNumber.Study -> {
                    retrofitHandler.createParty("taxi-party", StudyParty(
                        common = CommonPartyAttributes(
                            partyId = -1,
                            partyName = binding.edittextPartyName.text.toString(),
                            partyHead = userId,
                            place = Place(
                                hasPlace = 1,
                                place1 = binding.spinnerPlace1.selectedItem.toString(),
                                place2 = binding.spinnerPlace2.selectedItem.toString(),
                                place3 = binding.spinnerPlace3.selectedItem.toString(),
                            ),
                            currentCount = 1,
                            maximumCount = binding.edittextPartyMaximumCount.text.toString().toInt(),
                            detailedDescription = binding.edittextPartyDescription.text.toString(),
                            countDifference = 0
                        )
                    )).enqueue(object: Callback<ResponseCreateParty> {
                        override fun onResponse(
                            call: Call<ResponseCreateParty>,
                            response: Response<ResponseCreateParty>
                        ) {
                            TODO("On success, where should user go?")
                        }

                        override fun onFailure(call: Call<ResponseCreateParty>, t: Throwable) {
                            Toast.makeText(binding.root.context, "서버와 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("CREATE", t.toString())
                        }
                    })
                }
                PartyTypeNumber.Custom -> {
                    retrofitHandler.createParty("custom-party", CustomParty(
                        common = CommonPartyAttributes(
                            partyId = -1,
                            partyName = binding.edittextPartyName.text.toString(),
                            partyHead = userId,
                            place = Place(
                                hasPlace = 1,
                                place1 = binding.spinnerPlace1.selectedItem.toString(),
                                place2 = binding.spinnerPlace2.selectedItem.toString(),
                                place3 = binding.spinnerPlace3.selectedItem.toString(),
                            ),
                            currentCount = 1,
                            maximumCount = binding.edittextPartyMaximumCount.text.toString().toInt(),
                            detailedDescription = binding.edittextPartyDescription.text.toString(),
                            countDifference = 0
                        )
                    )).enqueue(object: Callback<ResponseCreateParty> {
                        override fun onResponse(
                            call: Call<ResponseCreateParty>,
                            response: Response<ResponseCreateParty>
                        ) {
                            TODO("On success, where should user go?")
                        }

                        override fun onFailure(call: Call<ResponseCreateParty>, t: Throwable) {
                            Toast.makeText(binding.root.context, "서버와 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("CREATE", t.toString())
                        }
                    })
                }
                else -> throw Error("ERRRRRRR")
            }
        }
    }
}