package com.example.moim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moim.databinding.FragmentMainTabBinding
import com.example.moim.databinding.PartyTypeItemBinding

class MainFragment: Fragment() {
    private lateinit var binding: FragmentMainTabBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMainTabBinding.inflate(layoutInflater)
        binding.mainRecyclerView.adapter = PartyTypeAdapter()
        binding.mainRecyclerView.layoutManager = GridLayoutManager(this.context, 2)

        return binding.root
    }
}

class PartyTypeAdapter: RecyclerView.Adapter<PartyTypeAdapter.MyViewHolder>(){
    private val nameList = listOf("택시팟", "밥약팟", "야식팟", "공부/프로젝트팟", "나만의팟")
    private lateinit var binding: PartyTypeItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        binding = PartyTypeItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = nameList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(nameList[position])
    }
    inner class MyViewHolder(private val binding: PartyTypeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(name: String) {
            val context = binding.root.context
            binding.partyTypeItem.text = name
            binding.partyTypeItem.setOnClickListener {
                val intent = Intent(context, PartyListActivity::class.java)
                intent.putExtra("party_type", name)
                context.startActivity(intent)
            }
        }
    }
}