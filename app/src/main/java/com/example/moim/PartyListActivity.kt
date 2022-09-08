package com.example.moim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moim.databinding.ActivityPartyListBinding
import com.example.moim.databinding.PartyItemBinding
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class PartyListActivity: AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityPartyListBinding
    private val partyTypeList = listOf("택시팟", "밥약팟", "야식팟", "공부/프로젝트팟", "나만의팟")

    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    private lateinit var partyAdapter: PartyAdapter
    private var partyTypeNumber = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPartyListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val partyType = intent.extras?.getString("party_type")!!

        partyTypeNumber = when (partyType) {
            in partyTypeList -> partyTypeList.indexOf(partyType)
            else -> throw Error("Invalid party: $partyType")
        }

        assert(partyTypeNumber != -1)

        binding.textPartyListTitle.text = partyType

        partyAdapter = PartyAdapter(partyTypeNumber)

        partyAdapter.setItemClickListener(
            object: PartyAdapter.OnItemClickListener{
                override fun onClick(v: View, position: Int) {
                    val party = partyAdapter.partyList[position]
                    val intent = Intent(binding.root.context, PartyDescriptionActivity::class.java)
                    intent.putExtra("party_info", party)
                    intent.putExtra("party_type_number", partyTypeNumber)
//                    partyDescriptionLauncher.launch(intent)
                    startActivity(intent)
                }
            }
        )
        binding.recyclerViewPartyList.adapter = partyAdapter
        binding.recyclerViewPartyList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        binding.buttonCreateParty.setOnClickListener {
            val intent = Intent(this, CreatePartyActivity::class.java)
            intent.putExtra("party_type_number", partyTypeNumber)

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        val partyList = getPartyList(partyTypeNumber)
        partyAdapter.partyList = partyList
        partyAdapter.notifyDataSetChanged()
    }

    private fun getPartyList(partyTypeNumber: Int): MutableList<Party> {
        val resultList = mutableListOf<Party>()
        val partyList = runBlocking(coroutineContext) {
            withContext(coroutineContext) {
                val response = when (partyTypeNumber) {
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

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    inner class MyViewHolder(private val binding: PartyItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(party: Party) {
            val context = binding.root.context

            when (partyNumber) {
                PartyTypeNumber.Taxi -> {  // 택시팟
                    val taxiParty = party as TaxiParty

                    val dateString = parseDateString(taxiParty.extra.partyDate)
                    val timeString = parseTimeString(taxiParty.extra.partyTime)

                    binding.root.removeView(binding.upperSimple)
                    binding.partyOption1.text = String.format(context.getString(R.string.taxi_option1), taxiParty.extra.detailedStartPlace)
                    binding.partyOption2.text = String.format(context.getString(R.string.taxi_option2), taxiParty.extra.destination)
                    binding.partyLowerOption.text = String.format(context.getString(R.string.party_datetime), dateString, timeString)
                }
                PartyTypeNumber.Meal, PartyTypeNumber.NightMeal -> {  // 밥약 + 야식팟
                    val mealParty = party as MealParty
                    val outside = if (mealParty.extra.outside == 1) {
                        "나가서 먹기"
                    } else {
                        "배달 시키기"
                    }

                    val dateString = parseDateString(mealParty.extra.partyDate)
                    val timeString = parseTimeString(mealParty.extra.partyTime)

                    binding.root.removeView(binding.upperSimple)
                    binding.partyOption1.text = String.format(context.getString(R.string.meal_option1), mealParty.extra.mealType)
                    binding.partyOption2.text = String.format(context.getString(R.string.meal_option2), outside)
                    binding.partyLowerOption.text = String.format(context.getString(R.string.party_datetime), dateString, timeString)
                }
                PartyTypeNumber.Study -> {  // 공부/프로젝트팟
                    val studyParty = party as StudyParty

                    binding.root.removeView(binding.upperComplex)
                    binding.partyNameSimple.text = studyParty.common.partyName
                    binding.partyLowerOption.text = studyParty.common.place.toWrittenString()
                }
                PartyTypeNumber.Custom -> {  // 나만의팟
                    val customParty = party as CustomParty

                    binding.root.removeView(binding.upperComplex)
                    binding.partyNameSimple.text = customParty.common.partyName
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
        }
    }
}