package com.example.moim

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moim.databinding.ActivityPartyManagerBinding
import com.example.moim.databinding.ChatItemBinding
import com.example.moim.databinding.DialogUserListBinding
import com.example.moim.databinding.UserListItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PartyManagerActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPartyManagerBinding

    private lateinit var sharedManager: SharedManager

    private var editing = false
    private var dateString: String = ""
    private lateinit var partyInfo: Party

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPartyManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedManager = SharedManager(this)
        partyInfo = intent.extras?.getSerializable("party_info")!! as Party
        val partyTypeNumber = intent.extras?.getInt("party_type_number")!!

        val partyInformation = partyInfo

        val userId = sharedManager.getUserId()

        // Edit 뷰가 없는 상태가 디폴트
        binding.constraintRoot.removeView(binding.constraintEdit)

        val isHead = partyInformation.common.partyHead == userId
        binding.buttonEdit.isEnabled = isHead
        binding.buttonEdit.setTextColor(
            if (isHead) {
                ContextCompat.getColor(this, R.color.black)
            }
            else {
                ContextCompat.getColor(this, R.color.gray)
            }
        )

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

        // 방장 이름 세팅
        val context = this
        var partyHead = ""
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
                        partyHead = response.body()!!.username
                        binding.textPartyHead.text = String.format(getString(R.string.party_head), response.body()!!.username)
                    }
                }
            }
        )

        // 시간 스피너, 지역 스피너 값 설정
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

        val mealTypeSpinner = binding.spinnerMealType
        val mealTypeAdapter = ArrayAdapter.createFromResource(this, R.array.meal_type, android.R.layout.simple_spinner_item)
        mealTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mealTypeSpinner.adapter = mealTypeAdapter

        val outsideSpinner = binding.spinnerMealOutside
        val outsideAdapter = ArrayAdapter.createFromResource(this, R.array.meal_outside, android.R.layout.simple_spinner_item)
        outsideAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        outsideSpinner.adapter = outsideAdapter

        when (partyTypeNumber) {
            PartyTypeNumber.Taxi -> {
                val taxiParty = partyInformation as TaxiParty
                val dateString = parseDateString(taxiParty.extra.partyDate)
                val timeString = parseTimeString(taxiParty.extra.partyTime)
                binding.textViewPartyOption1.text = String.format(getString(R.string.taxi_option1), taxiParty.extra.detailedStartPlace)
                binding.textViewPartyOption2.text = String.format(getString(R.string.taxi_option2), taxiParty.extra.destination)
                binding.textPartyTime.text = String.format(getString(R.string.party_datetime), dateString, timeString)
            }
            PartyTypeNumber.Meal, PartyTypeNumber.NightMeal -> {
                val mealParty = partyInformation as MealParty
                val dateString = parseDateString(mealParty.extra.partyDate)
                val timeString = parseTimeString(mealParty.extra.partyTime)
                binding.textViewPartyOption1.text = String.format(getString(R.string.meal_option1), mealParty.extra.mealType)
                binding.textViewPartyOption2.text = String.format(getString(R.string.meal_option2), if (mealParty.extra.outside == 1) "나가서 먹기" else "배달 시키기")
                binding.textPartyTime.text = String.format(getString(R.string.party_datetime), dateString, timeString)
            }
            PartyTypeNumber.Study, PartyTypeNumber.Custom -> {
                binding.root.removeView(binding.linearOptions)
                binding.textPartyTime.visibility = View.INVISIBLE
            }
            else -> throw Error("Should not happen")
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

        binding.buttonConfirmEdit.setOnClickListener {
            // TODO: 요청 보내기
            if (userInputChecker(partyTypeNumber)) {
                // 매우 긴 코드 예정..
                when (partyTypeNumber) {
                    PartyTypeNumber.Taxi -> {
                        val maxCount = binding.edittextPartyMaximumCount.text.toString().toInt()
                        val newPartyInfo = TaxiParty(
                            common = CommonPartyAttributes(
                                partyId = partyInformation.common.partyId,
                                partyName = binding.edittextPartyName.text.toString(),
                                partyHead = partyInformation.common.partyHead,
                                place = Place(
                                    hasPlace = 1,
                                    place1 = binding.spinnerPlace1.selectedItem.toString(),
                                    place2 = "",
                                    place3 = binding.spinnerPlace3.selectedItem.toString(),
                                ),
                                currentCount = partyInformation.common.currentCount,
                                maximumCount = maxCount,
                                detailedDescription = binding.edittextPartyDescription.text.toString(),
                                countDifference = maxCount - partyInformation.common.currentCount
                            ),
                            extra = TaxiExtra(
                                detailedStartPlace = binding.edittextTaxiStart.text.toString(),
                                destination = binding.edittextTaxiDestination.text.toString(),
                                partyDate = makeDateString(dateString),
                                partyTime = makeTimeString()
                            ),
                            partyType = "taxi_party"
                        )
                        retrofitHandler.editTaxiParty(newPartyInfo).enqueue(
                            object: Callback<ResponseEditParty> {
                                override fun onResponse(
                                    call: Call<ResponseEditParty>,
                                    response: Response<ResponseEditParty>
                                ) {
                                    if (response.isSuccessful) {
                                        editing = false
                                        binding.constraintRoot.addView(binding.constraintView)
                                        binding.constraintRoot.removeView(binding.constraintEdit)

                                        // TODO: 뷰 업데이트 해주기

                                       Toast.makeText(applicationContext, "정보 수정 완료!", Toast.LENGTH_SHORT).show()
                                    }
                                    else {
                                        Toast.makeText(applicationContext, "정보 수정 실패..", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<ResponseEditParty>, t: Throwable) {
                                    Log.d("MANAGER", t.toString())
                                    Toast.makeText(applicationContext, "서버와 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    // TODO: 다른 파티 종류.. 추가.........
                    else -> Unit
                }
            }
            else {
                Toast.makeText(applicationContext, "올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonEdit.setOnClickListener {
            editing = true
            binding.constraintRoot.removeView(binding.constraintView)
            binding.constraintRoot.addView(binding.constraintEdit)

            binding.edittextPartyName.setText(partyInformation.common.partyName)
            binding.edittextPartyMaximumCount.setText(partyInformation.common.maximumCount.toString())
            // TODO: 첫 날짜 정해놓기, dateString 초기 값 주기
            binding.buttonPartyDate.text = "9월 9일"
            dateString = "2022년 9월 9일"

            // TODO: 오전/오후 시간대 스피너, 시-분 칸 초기 값 주기
            binding.edittextPartyTimeHour.setText("4")
            binding.edittextPartyTimeMinute.setText("0")

            binding.edittextPartyDescription.setText(partyInformation.common.detailedDescription)

            // TODO: 파티 타입에 따라 에딧창 기본으로 채우기
            when (partyTypeNumber) {
                PartyTypeNumber.Taxi -> {
                    binding.constraintEdit.removeView(binding.spinnerMealType)
                    binding.constraintEdit.removeView(binding.spinnerMealOutside)
                    binding.textPartyOption1.text = "출발점"
                    binding.textPartyOption2.text = "도착점"
                }
                PartyTypeNumber.Meal, PartyTypeNumber.NightMeal -> {
                    binding.constraintEdit.removeView(binding.edittextTaxiStart)
                    binding.constraintEdit.removeView(binding.edittextTaxiDestination)
                    binding.textPartyOption1.text = "음식 종류"
                    binding.textPartyOption2.text = "먹는 위치"
                }
                PartyTypeNumber.Custom, PartyTypeNumber.Study -> {
                    binding.constraintEdit.removeView(binding.edittextTaxiStart)
                    binding.constraintEdit.removeView(binding.edittextTaxiDestination)
                    binding.constraintEdit.removeView(binding.spinnerMealType)
                    binding.constraintEdit.removeView(binding.spinnerMealOutside)
                    binding.constraintEdit.removeView(binding.textPartyOption1)
                    binding.constraintEdit.removeView(binding.textPartyOption2)
                }
            }
        }


        binding.buttonUserList.setOnClickListener {
            val dialog = UserListDialog(this, partyInformation.common.partyId, partyHead)
            dialog.show()

            val screenSize = getScreenSize(this)
            dialog.window?.setLayout(screenSize.width * 4 / 5, screenSize.height * 3 / 5)

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

class UserListDialog(context: Context, private val partyId: Int, private val partyHead: String): Dialog(context) {
    private lateinit var binding: DialogUserListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userList = runBlocking {
            withContext(Dispatchers.IO) {
                val response = retrofitHandler.getPartyUserList(partyId).execute().body()
                response?: mutableListOf()
            }
        }

        val adapter = UserListAdapter(userList, partyHead)
        binding.recyclerUserList.adapter = adapter
        binding.recyclerUserList.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)
    }
}

class UserListAdapter(
    private val userList: MutableList<UserInformation>,
    private val partyHead: String
    ): RecyclerView.Adapter<UserListAdapter.MyViewHolder>() {
    private lateinit var binding: UserListItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        binding = UserListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, partyHead)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(userList[position].username)
    }

    inner class MyViewHolder(
        private val binding: UserListItemBinding,
        private val partyHead: String
        ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(username: String) {
            val context = binding.root.context
            if (username == partyHead) {
                binding.textUsername.setTextColor(ContextCompat.getColor(context, R.color.indigo1))
                binding.textUsername.text = String.format(context.getString(R.string.string_user_head), username)
            }
            else {
                binding.textUsername.text = username
            }
        }
    }


}