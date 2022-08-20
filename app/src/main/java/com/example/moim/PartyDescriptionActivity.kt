package com.example.moim

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moim.databinding.ActivityPartyDescriptionBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PartyDescriptionActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPartyDescriptionBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPartyDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: intent 로 파티 오브젝트를 넘겨받지 말고, HTTP POST 요청을 보내 최신 정보를 가져오자.
        //      새로고침 버튼을 넣으면 해소 가능.

        val partyInformation = intent.extras?.getSerializable("party_info")!! as Party
        val partyTypeNumber = intent.extras?.getInt("party_type_number")!!

        // 공통 요소들 세팅
        binding.textPartyHead.text = ""
        binding.textPartyDescription.text = partyInformation.common.detailedDescription
        binding.textPartyCount.text = String.format(
            getString(R.string.party_count),
            partyInformation.common.currentCount,
            partyInformation.common.maximumCount
        )
        binding.textPartyName.text = partyInformation.common.partyName
        binding.textPartyPlace.text = partyInformation.common.place.toWrittenString()

        when (partyTypeNumber) {
            PartyTypeNumber.Taxi -> {
                val taxiParty = partyInformation as TaxiParty
                val dateString = taxiParty.extra.partyDate
                val timeString = taxiParty.extra.partyTime
                binding.textPartyOption1.text = String.format(getString(R.string.taxi_option1), taxiParty.extra.detailedStartPlace)
                binding.textPartyOption2.text = String.format(getString(R.string.taxi_option2), taxiParty.extra.destination)
                binding.textPartyTime.text = String.format(getString(R.string.party_datetime), dateString, timeString)
            }
            PartyTypeNumber.Meal, PartyTypeNumber.NightMeal -> {
                val mealParty = partyInformation as MealParty
                val dateString = mealParty.extra.partyDate
                val timeString = mealParty.extra.partyTime
                binding.textPartyOption1.text = String.format(getString(R.string.meal_option1), mealParty.extra.mealType)
                binding.textPartyOption2.text = String.format(getString(R.string.meal_option2), if (mealParty.extra.outside) "나가서 먹기" else "배달")
                binding.textPartyTime.text = String.format(getString(R.string.party_datetime), dateString, timeString)
            }
            PartyTypeNumber.Study, PartyTypeNumber.Custom -> Unit
            else -> throw Error("Should not happen")
        }

        val context = this
        retrofitHandler.getUsername(partyInformation.common.partyHead).enqueue(
            object: Callback<ResponseUsername> {
                override fun onFailure(call: Call<ResponseUsername>, t: Throwable) {
                    Toast.makeText(context, "서버와 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(
                    call: Call<ResponseUsername>,
                    response: Response<ResponseUsername>
                ) {
                    if (response.isSuccessful) {
                        binding.textPartyHead.text = String.format(getString(R.string.party_head), response.body()!!.username)
                    }
                }
            }
        )

        binding.buttonBack.setOnClickListener {
            if (!isFinishing) finish()
        }
        binding.buttonEnterParty.setOnClickListener {
            // TODO: 단순히 입장만 하고 있는데.. 다른 경우도 생각해보자.
            val sharedManager = SharedManager(context)
            val userId = sharedManager.getUserId()
            retrofitHandler.joinParty(partyTypeString(partyTypeNumber), PartyJoinInformation(userId, partyInformation.common.partyId))
            val intent = Intent(this, PartyChatActivity::class.java)
            intent.putExtra("party_info", partyInformation)
        }
    }
}