package com.example.moim

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moim.databinding.FragmentLikedTabBinding
import com.example.moim.databinding.FragmentMyPartyBinding
import com.example.moim.databinding.PartyItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPartyFragment: Fragment() {
    private lateinit var binding: FragmentMyPartyBinding
    private lateinit var myPartyAdapter: MyPartyAdapter
    private lateinit var sharedManager: SharedManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMyPartyBinding.inflate(layoutInflater)

        sharedManager = SharedManager(binding.root.context)
        myPartyAdapter = MyPartyAdapter()

        myPartyAdapter.setItemClickListener (
            object: MyPartyAdapter.OnItemClickListener {
                override fun onClick(v: View, position: Int) {
                    val party = myPartyAdapter.partyList[position]
                    val intent = Intent(binding.root.context, PartyDescriptionActivity::class.java)
                    intent.putExtra("party_info", party)
                    intent.putExtra("party_type_number", partyTypeToNumber(party.partyType))
                    startActivity(intent)
                }
            }
        )

        binding.recyclerMyParty.adapter = myPartyAdapter
        binding.recyclerMyParty.layoutManager =
            LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        myPartyAdapter.partyList = updatePartyList()
        myPartyAdapter.notifyDataSetChanged()
    }

    private fun updatePartyList(): MutableList<Party> {
        return runBlocking {
            withContext(Dispatchers.IO) {
                val response = retrofitHandler.getMyPartyList(sharedManager.getUserId()).execute().body()
                if (response != null) {
                    mixedToList(response)
                }
                else {
                    mutableListOf()
                }
            }
        }
    }
}

class MyPartyAdapter: RecyclerView.Adapter<MyPartyAdapter.MyViewHolder>(){
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

            when (partyTypeToNumber(party.partyType)) {
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
                else -> throw Error("partyNumber error: not a valid partyNumber")
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