package com.example.moim

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
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

        binding.buttonCreateParty.setOnClickListener {
            val intent = Intent(this, CreatePartyActivity::class.java)
            intent.putExtra("party_type", partyType)

            startActivity(intent)
        }
    }

    private fun getPartyList(partyNumber: Int): MutableList<Party> {
        val resultList = mutableListOf<Party>()
        val partyList = runBlocking(coroutineContext) {
            withContext(coroutineContext) {
                val response = when (partyNumber) {
                    0 -> retrofitHandler.getTaxiPartyList()
                    1 -> retrofitHandler.getMealPartyList()
                    2 -> retrofitHandler.getNightMealPartyList()
                    3 -> retrofitHandler.getStudyPartyList()
                    4 -> retrofitHandler.getCustomPartyList()
                    else -> throw Error("Unlikely to happen")
                }
                response.execute().body()
            }
        }

        if (partyList != null) {
            for (elem in partyList) {
                resultList.add(elem)
            }
        }

        return resultList
    }
}

class PartyAdapter(private val partyNumber: Int): RecyclerView.Adapter<PartyAdapter.MyViewHolder>(){
    var partyList = mutableListOf<Party>()
    private lateinit var binding: PartyItemBinding
    private lateinit var itemClickListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        binding = PartyItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = partyList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
        holder.bind(partyList[position])
    }

    // TODO: 클릭 이벤트 처리!
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    inner class MyViewHolder(private val binding: PartyItemBinding, ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(party: Party) {
            val context = binding.root.context

            when (partyNumber) {
                0 -> {  // 택시팟
                    val taxiParty = party as TaxiParty

                    // TODO: 날짜 스트링, 시간 스트링
                    val dateString = ""
                    val timeString = ""

                    binding.root.removeView(binding.upperSimple)
                    binding.partyOption1.text = String.format(context.getString(R.string.taxi_option1), taxiParty.extra.detailedStartPlace)
                    binding.partyOption2.text = String.format(context.getString(R.string.taxi_option2), taxiParty.extra.destination)
                    binding.partyLowerOption.text = String.format(context.getString(R.string.party_datetime), dateString, timeString)
                }
                1, 2 -> {  // 밥약 + 야식팟
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
                3 -> {  // 공부/프로젝트팟
                    val studyParty = party as StudyParty

                    binding.partyLowerOption.text = studyParty.common.place.toWrittenString()
                }
                4 -> {  // 나만의팟
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
            binding.root.setOnClickListener {
                val intent = Intent(context, PartyDescriptionActivity::class.java)
                intent.putExtra("party_info", party)
                intent.putExtra("party_type_number", partyNumber)
            }
        }
    }
}