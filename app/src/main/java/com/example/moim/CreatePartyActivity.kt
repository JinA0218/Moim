package com.example.moim

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moim.databinding.ActivityCreatePartyBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreatePartyActivity: AppCompatActivity() {
    private lateinit var binding: ActivityCreatePartyBinding
    private lateinit var sharedManager: SharedManager

    private var dateString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreatePartyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedManager = SharedManager(this)
        val partyTypeNumber = intent.extras?.getInt("party_type_number")!!

        actionBar?.title = partyTypeKorean(partyTypeNumber)

        // 몇몇 View 들을 숨겨줘야 한다. 레이아웃도 맞춰서 조정해야 한다.
        when (partyTypeNumber) {
            PartyTypeNumber.Taxi -> {
                binding.constraintMain.removeView(binding.spinnerMealType)
                binding.constraintMain.removeView(binding.spinnerMealOutside)
                binding.textPartyOption1.text = "출발점"
                binding.textPartyOption2.text = "도착점"
            }
            PartyTypeNumber.Meal, PartyTypeNumber.NightMeal -> {
                binding.constraintMain.removeView(binding.edittextTaxiStart)
                binding.constraintMain.removeView(binding.edittextTaxiDestination)
                binding.textPartyOption1.text = "음식 종류"
                binding.textPartyOption2.text = "먹는 위치"

            }
            PartyTypeNumber.Study, PartyTypeNumber.Custom -> {
                binding.constraintMain.removeView(binding.spinnerMealType)
                binding.constraintMain.removeView(binding.spinnerMealOutside)
                binding.constraintMain.removeView(binding.edittextTaxiStart)
                binding.constraintMain.removeView(binding.edittextTaxiDestination)
                binding.constraintMain.removeView(binding.textPartyOption1)
                binding.constraintMain.removeView(binding.textPartyOption2)
            }
            else -> throw Error("Should not happen!!")
        }

        // 지역을 고를 수 있게 해주는 스피너 설정

        val timeTypeSpinner = binding.spinnerTimeType
        val timeTypeAdapter = ArrayAdapter.createFromResource(this, R.array.time_type, android.R.layout.simple_spinner_item)
        timeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeTypeSpinner.adapter = timeTypeAdapter

        val placeSpinner1 = binding.spinnerPlace1
        val placeAdapter1 = ArrayAdapter.createFromResource(this, R.array.place1, android.R.layout.simple_spinner_item)
        placeAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        placeSpinner1.adapter = placeAdapter1

        binding.spinnerPlace2.isEnabled = false
        binding.spinnerPlace2.isClickable = false

        val placeSpinner3 = binding.spinnerPlace3
        val placeAdapter3 = ArrayAdapter.createFromResource(this, R.array.place3, android.R.layout.simple_spinner_item)
        placeAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        placeSpinner3.adapter = placeAdapter3

        binding.buttonBack.setOnClickListener {
            if (!isFinishing) finish()
        }

        binding.buttonPartyDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                dateString = "${year}년 ${month+1}월 ${dayOfMonth}일"
                binding.buttonPartyDate.text = String.format(getString(R.string.string_simple_date), month + 1, dayOfMonth)
            }
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.buttonCreateParty.setOnClickListener {
            val userId = sharedManager.getUserId()
            val username = sharedManager.getUsername()

            if (!userInputChecker(partyTypeNumber)) {
                Toast.makeText(binding.root.context, "정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (partyTypeNumber) {
                PartyTypeNumber.Taxi -> {
                    val timeString = makeTimeString()
                    val maxCount = binding.edittextPartyMaximumCount.text.toString().toInt()
                    val finalDateString = dateString
                    if (finalDateString == null) {
                        Toast.makeText(this, "날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val formatDateString = makeDateString(finalDateString)
                        val taxiParty = TaxiParty(
                            common = CommonPartyAttributes(
                                partyId = -1,
                                partyName = binding.edittextPartyName.text.toString(),
                                partyHead = userId,
                                place = Place(
                                    hasPlace = 1,
                                    place1 = binding.spinnerPlace1.selectedItem.toString(),
                                    place2 = "",
                                    place3 = binding.spinnerPlace3.selectedItem.toString(),
                                ),
                                currentCount = 1,
                                maximumCount = maxCount,
                                detailedDescription = binding.edittextPartyDescription.text.toString(),
                                countDifference = maxCount - 1
                            ),
                            extra = TaxiExtra(
                                detailedStartPlace = binding.edittextTaxiStart.text.toString(),
                                destination = binding.edittextTaxiDestination.text.toString(),
                                partyDate = formatDateString,
                                partyTime = timeString
                            ),
                            partyType = "taxi_party"
                        )

                        retrofitHandler.createTaxiParty(taxiParty).enqueue(
                            object: Callback<ResponseCreateParty> {
                                override fun onResponse(call: Call<ResponseCreateParty>, response: Response<ResponseCreateParty>) {
                                    if (response.isSuccessful) {
                                        val intent = Intent(binding.root.context, PartyChatActivity::class.java)
                                        val partyId = response.body()!!.partyId
                                        taxiParty.common.partyId = partyId
                                        intent.putExtra("party_info", taxiParty)
                                        Toast.makeText(binding.root.context, "팟 생성 성공!", Toast.LENGTH_SHORT).show()

                                        runBlocking {
                                            withContext(Dispatchers.IO) {
                                                retrofitHandler.joinTaxiParty(PartyJoinInformation(userId, partyId, username)).execute()
                                            }
                                        }

                                        if (!isFinishing) finish()
                                        startActivity(intent)
                                    }
                                    else {
                                        Toast.makeText(binding.root.context, "팟 생성에 실패했습니다. 나중에 다시 해주세요.", Toast.LENGTH_SHORT).show()
                                        Log.d("CREATE", "$response")
                                    }
                                }

                                override fun onFailure(call: Call<ResponseCreateParty>, t: Throwable) {
                                    Toast.makeText(binding.root.context, "서버와 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                    Log.d("CREATE", t.toString())
                                }
                            })
                    }

                }
                PartyTypeNumber.Meal -> {
                    val maxCount = binding.edittextPartyMaximumCount.text.toString().toInt()
                    val timeString = makeTimeString()
                    val outside = if (binding.spinnerMealOutside.selectedItem.toString() == "배달 시키기") {
                        0
                    }
                    else {
                        1
                    }

                    val finalDateString = dateString
                    if (finalDateString == null) {
                        Toast.makeText(this, "날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val mealParty = MealParty(
                            common = CommonPartyAttributes(
                                partyId = -1,
                                partyName = binding.edittextPartyName.text.toString(),
                                partyHead = userId,
                                place = Place(
                                    hasPlace = 1,
                                    place1 = binding.spinnerPlace1.selectedItem.toString(),
                                    place2 = "",
                                    place3 = binding.spinnerPlace3.selectedItem.toString(),
                                ),
                                currentCount = 1,
                                maximumCount = maxCount,
                                detailedDescription = binding.edittextPartyDescription.text.toString(),
                                countDifference = maxCount - 1,
                            ),
                            extra = MealExtra(
                                mealType = binding.spinnerMealType.selectedItem.toString(),
                                outside = outside,
                                partyDate = finalDateString,
                                partyTime = timeString
                            ),
                            partyType = "meal_party"
                        )
                        retrofitHandler.createMealParty(mealParty).enqueue(object: Callback<ResponseCreateParty> {
                            override fun onResponse(
                                call: Call<ResponseCreateParty>,
                                response: Response<ResponseCreateParty>
                            ) {
                                if (response.isSuccessful) {
                                    val intent = Intent(binding.root.context, PartyChatActivity::class.java)
                                    val partyId = response.body()!!.partyId
                                    mealParty.common.partyId = partyId
                                    intent.putExtra("party_info", mealParty)
                                    Toast.makeText(binding.root.context, "팟 생성 성공!", Toast.LENGTH_SHORT).show()

                                    runBlocking {
                                        withContext(Dispatchers.IO) {
                                            retrofitHandler.joinMealParty(PartyJoinInformation(userId, partyId, username)).execute()
                                        }
                                    }

                                    if (!isFinishing) finish()
                                    startActivity(intent)
                                }
                                else {
                                    Toast.makeText(binding.root.context, "팟 생성에 실패했습니다. 나중에 다시 해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<ResponseCreateParty>, t: Throwable) {
                                Toast.makeText(binding.root.context, "서버와 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                Log.d("CREATE", t.toString())
                            }
                        })
                    }
                }
                PartyTypeNumber.NightMeal -> {
                    val maxCount = binding.edittextPartyMaximumCount.text.toString().toInt()
                    val timeString = makeTimeString()
                    val outside = if (binding.spinnerMealOutside.selectedItem.toString() == "배달 시키기") {
                        0
                    }
                    else {
                        1
                    }

                    val finalDateString = dateString
                    if (finalDateString == null) {
                        Toast.makeText(this, "날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val nightMealParty = MealParty(
                            common = CommonPartyAttributes(
                                partyId = -1,
                                partyName = binding.edittextPartyName.text.toString(),
                                partyHead = userId,
                                place = Place(
                                    hasPlace = 1,
                                    place1 = binding.spinnerPlace1.selectedItem.toString(),
                                    place2 = "",
                                    place3 = binding.spinnerPlace3.selectedItem.toString(),
                                ),
                                currentCount = 1,
                                maximumCount = maxCount,
                                detailedDescription = binding.edittextPartyDescription.text.toString(),
                                countDifference = maxCount - 1
                            ),
                            extra = MealExtra(
                                mealType = binding.spinnerMealType.selectedItem.toString(),
                                outside = outside,
                                partyDate = finalDateString,
                                partyTime = timeString
                            ),
                            partyType = "night_meal_party"
                        )
                        retrofitHandler.createNightMealParty(nightMealParty).enqueue(object: Callback<ResponseCreateParty> {
                            override fun onResponse(
                                call: Call<ResponseCreateParty>,
                                response: Response<ResponseCreateParty>
                            ) {
                                if (response.isSuccessful) {
                                    val intent = Intent(binding.root.context, PartyChatActivity::class.java)
                                    val partyId = response.body()!!.partyId
                                    nightMealParty.common.partyId = partyId
                                    intent.putExtra("party_info", nightMealParty)
                                    Toast.makeText(binding.root.context, "팟 생성 성공!", Toast.LENGTH_SHORT).show()

                                    runBlocking {
                                        withContext(Dispatchers.IO) {
                                            retrofitHandler.joinNightMealParty(PartyJoinInformation(userId, partyId, username)).execute()
                                        }
                                    }

                                    if (!isFinishing) finish()
                                    startActivity(intent)
                                }
                                else {
                                    Toast.makeText(binding.root.context, "팟 생성에 실패했습니다. 나중에 다시 해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<ResponseCreateParty>, t: Throwable) {
                                Toast.makeText(binding.root.context, "서버와 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                Log.d("CREATE", t.toString())
                            }
                        })
                    }


                }
                PartyTypeNumber.Study -> {
                    val maxCount = binding.edittextPartyMaximumCount.text.toString().toInt()
                    val studyParty = StudyParty(
                        common = CommonPartyAttributes(
                            partyId = -1,
                            partyName = binding.edittextPartyName.text.toString(),
                            partyHead = userId,
                            place = Place(
                                hasPlace = 1,
                                place1 = binding.spinnerPlace1.selectedItem.toString(),
                                place2 = "",
                                place3 = binding.spinnerPlace3.selectedItem.toString(),
                            ),
                            currentCount = 1,
                            maximumCount = maxCount,
                            detailedDescription = binding.edittextPartyDescription.text.toString(),
                            countDifference = maxCount - 1,
                        ),
                        partyType = "study_party"
                    )
                    retrofitHandler.createStudyParty(studyParty).enqueue(object: Callback<ResponseCreateParty> {
                        override fun onResponse(
                            call: Call<ResponseCreateParty>,
                            response: Response<ResponseCreateParty>
                        ) {
                            if (response.isSuccessful) {
                                val intent = Intent(binding.root.context, PartyChatActivity::class.java)
                                val partyId = response.body()!!.partyId
                                studyParty.common.partyId = partyId
                                intent.putExtra("party_info", studyParty)
                                Toast.makeText(binding.root.context, "팟 생성 성공!", Toast.LENGTH_SHORT).show()

                                runBlocking {
                                    withContext(Dispatchers.IO) {
                                        retrofitHandler.joinStudyParty(PartyJoinInformation(userId, partyId, username)).execute()
                                    }
                                }

                                if (!isFinishing) finish()
                                startActivity(intent)
                            }
                            else {
                                Toast.makeText(binding.root.context, "팟 생성에 실패했습니다. 나중에 다시 해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseCreateParty>, t: Throwable) {
                            Toast.makeText(binding.root.context, "서버와 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("CREATE", t.toString())
                        }
                    })
                }
                PartyTypeNumber.Custom -> {
                    val maxCount = binding.edittextPartyMaximumCount.text.toString().toInt()
                    val customParty = CustomParty(
                        common = CommonPartyAttributes(
                            partyId = -1,
                            partyName = binding.edittextPartyName.text.toString(),
                            partyHead = userId,
                            place = Place(
                                hasPlace = 1,
                                place1 = binding.spinnerPlace1.selectedItem.toString(),
                                place2 = "",
                                place3 = binding.spinnerPlace3.selectedItem.toString(),
                            ),
                            currentCount = 1,
                            maximumCount = maxCount,
                            detailedDescription = binding.edittextPartyDescription.text.toString(),
                            countDifference = maxCount - 1
                        ),
                        partyType = "custom_party"
                    )
                    retrofitHandler.createCustomParty(customParty).enqueue(object: Callback<ResponseCreateParty> {
                        override fun onResponse(
                            call: Call<ResponseCreateParty>,
                            response: Response<ResponseCreateParty>
                        ) {
                            if (response.isSuccessful) {
                                val intent = Intent(binding.root.context, PartyChatActivity::class.java)
                                val partyId = response.body()!!.partyId
                                customParty.common.partyId = partyId
                                intent.putExtra("party_info", customParty)
                                Toast.makeText(binding.root.context, "팟 생성 성공!", Toast.LENGTH_SHORT).show()

                                runBlocking {
                                    withContext(Dispatchers.IO) {
                                        retrofitHandler.joinCustomParty(PartyJoinInformation(userId, partyId, username)).execute()
                                    }
                                }

                                if (!isFinishing) finish()
                                startActivity(intent)
                            }
                            else {
                                Toast.makeText(binding.root.context, "팟 생성에 실패했습니다. 나중에 다시 해주세요.", Toast.LENGTH_SHORT).show()
                            }
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

    private fun userInputChecker(partyTypeNumber: Int): Boolean {
        val partyName = binding.edittextPartyName.text.toString()
        val partyMaxCount = binding.edittextPartyMaximumCount.text.toString()
        val partyPlace1 = binding.spinnerPlace1.selectedItem.toString()
        val partyPlace3 = binding.spinnerPlace3.selectedItem.toString()
        val partyDetail = binding.edittextPartyDescription.text.toString()
        if (partyName.isEmpty() || partyMaxCount.isEmpty() ||
            partyPlace1.isEmpty() || partyPlace3.isEmpty() || partyDetail.isEmpty()) {
            return false
        }

        return when (partyTypeNumber) {
            PartyTypeNumber.Taxi -> {
                val partyTimeHour = binding.edittextPartyTimeHour.text.toString()
                val partyTimeMinute = binding.edittextPartyTimeMinute.text.toString()
                val partyTimeType = binding.spinnerTimeType.selectedItem.toString()
                val partyStart = binding.edittextTaxiStart.text.toString()
                val partyDestination = binding.edittextTaxiDestination.text.toString()

                partyTimeHour.isNotEmpty() && partyTimeMinute.isNotEmpty() && partyTimeType.isNotEmpty() &&
                        partyStart.isNotEmpty() && partyDestination.isNotEmpty()
            }
            PartyTypeNumber.Meal, PartyTypeNumber.NightMeal -> {
                val partyTimeHour = binding.edittextPartyTimeHour.text.toString()
                val partyTimeMinute = binding.edittextPartyTimeMinute.text.toString()
                val partyTimeType = binding.spinnerTimeType.selectedItem.toString()
                val partyMealType = binding.spinnerMealType.selectedItem.toString()
                val partyOutside = binding.spinnerMealOutside.selectedItem.toString()

                partyTimeHour.isNotEmpty() && partyTimeMinute.isNotEmpty() && partyTimeType.isNotEmpty() &&
                        partyMealType.isNotEmpty() && partyOutside.isNotEmpty()
            }
            else -> true
        }


    }

    private fun makeTimeString(): String {
        val partyHour = binding.edittextPartyTimeHour.text.toString().toInt()
        val partyMinute = binding.edittextPartyTimeMinute.text.toString().toInt()

        return when (binding.spinnerTimeType.selectedItem.toString()) {
            "오전" -> {
                val realHour = if (partyHour == 12) 0 else partyHour
                String.format("%02d:%02d:00", realHour, partyMinute)
            }
            "오후" -> {
                val realHour = if (partyHour == 12) 12 else partyHour + 12
                String.format("%02d:%02d:00", realHour, partyMinute)
            }
            else -> throw Error("Assert!!!")
        }
    }

    private fun makeDateString(dateString: String): String {
        val dateList = dateString.split(" ")
        return String.format("%s-%s-%s",
            dateList[0].substring(0, dateList[0].length - 1),
            dateList[1].substring(0, dateList[1].length - 1),
            dateList[2].substring(0, dateList[2].length - 1),
        )
    }
}