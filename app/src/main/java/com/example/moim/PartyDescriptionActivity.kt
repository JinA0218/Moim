package com.example.moim

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.moim.databinding.ActivityPartyDescriptionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PartyDescriptionActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPartyDescriptionBinding
    private lateinit var partyInformation: Party
    private lateinit var roomDB: AppDB
    private lateinit var sharedManager: SharedManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPartyDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: intent 로 파티 오브젝트를 넘겨받지 말고, HTTP POST 요청을 보내 최신 정보를 가져오자.
        //      새로고침 버튼을 넣으면 해소 가능. (정보를 최신으로 유지하기 위해 필요, 후순위)

        partyInformation = intent.extras?.getSerializable("party_info")!! as Party
        val partyTypeNumber = intent.extras?.getInt("party_type_number")!!

        roomDB = AppDB.getInstance(this)!!
        sharedManager = SharedManager(this)

        // 내가 속한 파티인지 점검
        val isMyParty = runBlocking {
            withContext(Dispatchers.IO) {
                roomDB.MDao().getId(partyInformation.common.partyId)
            }
        }
        binding.buttonEnterParty.text = if (isMyParty == null) {
            getString(R.string.string_join)
        }
        else {
            getString(R.string.string_enter)
        }

        // 내가 좋아요 한 파티인지 점검
        val isLikedParty = runBlocking {
            withContext(Dispatchers.IO) {
                roomDB.LDao().getId(partyInformation.common.partyId)
            }
        }
        binding.buttonLike.background = if (isLikedParty == null) {
            AppCompatResources.getDrawable(this, R.drawable.like_white)
        }
        else {
            AppCompatResources.getDrawable(this, R.drawable.like_full)
        }


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
                val dateString = parseDateString(taxiParty.extra.partyDate)
                val timeString = parseTimeString(taxiParty.extra.partyTime)
                binding.textPartyOption1.text = String.format(getString(R.string.taxi_option1), taxiParty.extra.detailedStartPlace)
                binding.textPartyOption2.text = String.format(getString(R.string.taxi_option2), taxiParty.extra.destination)
                binding.textPartyTime.text = String.format(getString(R.string.party_datetime), dateString, timeString)
            }
            PartyTypeNumber.Meal, PartyTypeNumber.NightMeal -> {
                val mealParty = partyInformation as MealParty
                val dateString = parseDateString(mealParty.extra.partyDate)
                val timeString = parseTimeString(mealParty.extra.partyTime)
                binding.textPartyOption1.text = String.format(getString(R.string.meal_option1), mealParty.extra.mealType)
                binding.textPartyOption2.text = String.format(getString(R.string.meal_option2), if (mealParty.extra.outside == 1) "나가서 먹기" else "배달 시키기")
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

        binding.buttonRefresh.setOnClickListener {
            // TODO: retrofitHandler.getParty 해서 새로고침하기

            Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show()
        }

        binding.buttonLike.setOnClickListener {
            val like = runBlocking {
                withContext(Dispatchers.IO) {
                    val id = partyInformation.common.partyId
                    val dao = roomDB.LDao()
                    if (dao.getId(id) == null) {
                        dao.insert(LikedParty(id))
                        true
                    }
                    else {
                        dao.delete(LikedParty(id))
                        false
                    }
                }
            }

            binding.buttonLike.background =
                AppCompatResources.getDrawable(this, if (like) R.drawable.redheart else R.drawable.heart)

            val likeInfo = LikeInformation(
                sharedManager.getUserId(),
                sharedManager.getUsername(),
                partyInformation.common.partyId,
                if (like) 1 else 0,
                partyTypeString(partyTypeNumber).replace("-", "_")
            )

            retrofitHandler.likeParty(likeInfo).enqueue(
                object: Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        // Do nothing
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Log.e("LIKED", t.toString())
                    }
                }
            )
        }

        binding.buttonBack.setOnClickListener {
            if (!isFinishing) finish()
        }
        binding.buttonEnterParty.setOnClickListener {
            val sharedManager = SharedManager(context)
            val userId = sharedManager.getUserId()
            val username = sharedManager.getUsername()

            val intent = Intent(this, PartyChatActivity::class.java)
            intent.putExtra("party_info", partyInformation)
            intent.putExtra("party_type_number", partyTypeNumber)

            if (binding.buttonEnterParty.text.toString() == getString(R.string.string_join)) {
                // 새로 파티에 참가하는 경우, joinParty를 한다.
                val partyType = partyTypeString(partyTypeNumber)
                val partyId = partyInformation.common.partyId
                when (partyTypeNumber) {
                    PartyTypeNumber.Taxi -> {
                        retrofitHandler.joinTaxiParty(PartyJoinInformation(userId, partyId, username)).enqueue(
                            object: Callback<Unit> {
                                override fun onResponse(
                                    call: Call<Unit>,
                                    response: Response<Unit>
                                ) {
                                    Toast.makeText(context, "파티에 참가했습니다!", Toast.LENGTH_SHORT).show()
                                    runBlocking {
                                        withContext(Dispatchers.IO) {
                                            roomDB.MDao().insert(MyParty(partyId))
                                        }
                                    }
                                    startActivity(intent)
                                }

                                override fun onFailure(call: Call<Unit>, t: Throwable) {
                                    Toast.makeText(context, "서버와 통신에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
            else {
                // 이미 파티에 참가한 경우, 그냥 이동한다.
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val isMyParty = runBlocking {
            withContext(Dispatchers.IO) {
                roomDB.MDao().getId(partyInformation.common.partyId)
            }
        }
        binding.buttonEnterParty.text = if (isMyParty == null) {
            getString(R.string.string_join)
        }
        else {
            getString(R.string.string_enter)
        }
    }
}