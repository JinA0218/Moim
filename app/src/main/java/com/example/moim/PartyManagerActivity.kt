package com.example.moim

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moim.databinding.ActivityPartyManagerBinding

class PartyManagerActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPartyManagerBinding

    private lateinit var sharedManager: SharedManager

    private var editing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPartyManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedManager = SharedManager(this)
        val partyInformation = intent.extras?.getSerializable("party_info")!! as Party
        val partyTypeNumber = intent.extras?.getInt("party_type_number")!!

        val userId = sharedManager.getUserId()

        // Edit 뷰가 없는 상태가 디폴트
        binding.constraintRoot.removeView(binding.constraintEdit)

//        binding.buttonConfirmModify.isEnabled = partyInformation.common.partyHead == userId
        binding.buttonModify.isEnabled = partyInformation.common.partyHead == userId

        // TODO: PartyTypeNumber 에 따라 레이아웃 수정하기

        binding.buttonConfirmModify.setOnClickListener {
            editing = true
            binding.constraintRoot.removeView(binding.constraintView)
            binding.constraintRoot.addView(binding.constraintEdit)
        }

        binding.buttonViewBack.setOnClickListener {
            if (!isFinishing) finish()
        }

        // 수정 상태에서 뒤로가기 버튼을 누를 경우..
        binding.buttonEditBack.setOnClickListener {
            editing = false
            binding.constraintRoot.removeView(binding.constraintEdit)
            binding.constraintRoot.addView(binding.constraintView)
        }
    }

    // 팟 정보 수정 중일 때 BackPress 막기
    override fun onBackPressed() {
        if (!editing) {
            super.onBackPressed()
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
}