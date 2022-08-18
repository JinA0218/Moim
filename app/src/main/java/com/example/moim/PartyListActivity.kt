package com.example.moim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moim.databinding.ActivityPartyListBinding
import com.example.moim.databinding.PartyItemBinding
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.CoroutineContext

class PartyListActivity: AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityPartyListBinding
    private val partyTypeList = listOf("택시팟", "밥약팟", "야식팟", "공부/프로젝트팟", "나만의팟")
    private val partyTypeCommands = listOf("taxi-party", "meal-party", "night-meal-party", "study-party", "custom-party")

    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPartyListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val partyType = intent.extras?.getString("party_type")!!

        val partyNumber = when (partyType) {
            in partyTypeList -> partyTypeList.indexOf(partyType)
            else -> throw Error("Invalid party: $partyType")
        }

        assert(partyNumber != -1)

        binding.textPartyListTitle.text = partyType

        val partyList = getPartyList(partyNumber)

        val partyAdapter = PartyAdapter(partyNumber)
        partyAdapter.partyList = partyList

        binding.recyclerViewPartyList.adapter = partyAdapter
        binding.recyclerViewPartyList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun getPartyList(partyNumber: Int): MutableList<Party> {
        // TODO: HTTP 요청을 보낸다. Retrofit2 라이브러리를 활용하자.
        val partyList = runBlocking {
//            retrofitHandler.getPartyList(partyTypeCommands[partyNumber]).enqueue(
//                object: Callback<List<Party>> {
//                    override fun onResponse(call: Call<List<Party>>, response: Response<List<Party>>) {
//
//                        TODO("Not yet implemented")
//                    }
//
//                    override fun onFailure(call: Call<List<Party>>, t: Throwable) {
//                        TODO("Not yet implemented")
//                    }
//                }
//            )
            val response = retrofitHandler.getPartyList(partyTypeCommands[partyNumber])
            response.execute().body()
        }

        return partyList?.toMutableList() ?: mutableListOf()
    }
}

class PartyAdapter(private val partyNumber: Int): RecyclerView.Adapter<PartyAdapter.MyViewHolder>(){
    var partyList = mutableListOf<Party>()
    private lateinit var binding: PartyItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        binding = PartyItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = partyList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(partyList[position])
    }
    inner class MyViewHolder(private val binding: PartyItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(party: Party) {
            // TODO: Party 클래스를 완성하고, party_item.xml 파일 내용 채우기.
            //      party_item 은 파티를 소개하는데 필요한 정보가 들어가야 한다.

            // TODO: 파티는 종류에 따라 서로 다른 요소가 필요하다. 어떤 요소를 사용하는 것이 좋을까??
            //      EX) 택시팟은 시작 위치, 도착 위치, 날짜 및 시각, 만든이, 현재 인원, 최대 인원 등이 들어가야 한다.
            //      EX) 공부팟은 팟 이름, 위치, 만든이, 현재 인원, 최대 인원만 있어도 된다..
            //      해결책1: 다른 액티비티를 만든다.
            //      해결책2: 파티 종류에 따라 다르게 표시되도록 한다.
            val context = binding.root.context

            when (partyNumber) {
                0 -> {  // 택시
                    val taxiParty = party as TaxiParty

                    // TODO: 날짜 스트링, 시간 스트링
                    val dateString = ""
                    val timeString = ""

                    binding.root.removeView(binding.upperSimple)
                    binding.partyOption1.text = String.format(context.getString(R.string.taxi_option1), taxiParty.extra.detailedStartPlace)
                    binding.partyOption2.text = String.format(context.getString(R.string.taxi_option2), taxiParty.extra.destination)
                    binding.partyLowerOption.text = String.format(context.getString(R.string.party_datetime), dateString, timeString)
                }
                1, 2 -> {  // 밥약 + 야식
                    val mealParty = party as MealParty
                    val outside = if (mealParty.extra.outside) {
                        "나가서 먹기"
                    } else {
                        "배달"
                    }

                    // TODO: 날짜 스트링, 시간 스트링
                    val dateString = ""
                    val timeString = ""

                    binding.root.removeView(binding.upperSimple)
                    binding.partyOption1.text = String.format(context.getString(R.string.taxi_option1), mealParty.extra.mealType)
                    binding.partyOption2.text = String.format(context.getString(R.string.taxi_option2), outside)
                    binding.partyLowerOption.text = String.format(context.getString(R.string.party_datetime), dateString, timeString)
                }
                3 -> {  // 공부/프로젝트
                    val studyParty = party as StudyParty

                    binding.partyLowerOption.text = studyParty.common.place.toWrittenString()
                }
                4 -> {  // 나만의
                    val customParty = party as CustomParty

                    binding.partyLowerOption.text = customParty.common.place.toWrittenString()
                }
                else -> throw Error("partyNumber error: $partyNumber is not a valid partyNumber")
            }

            // 공통
            binding.partyNameComplex.text = party.common.partyName
            binding.partyCount.text = String.format(
                context.getString(R.string.party_count),
                party.common.currentCount,
                party.common.maximumCount
            )
//            binding.partyTypeItem.text = name
//            binding.partyTypeItem.setOnClickListener {
//                val intent = Intent(context, PartyListActivity::class.java)
//                intent.putExtra("party_type", name)
//                context.startActivity(intent)
//            }
        }
    }
}