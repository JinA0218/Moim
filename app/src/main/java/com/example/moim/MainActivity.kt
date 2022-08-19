package com.example.moim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.moim.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedManager: SharedManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: 로그인 되어있는지 체크하자.
        //      로그인이 되어있지 않다면 LoginActivity 로 넘겨준다.

        sharedManager = SharedManager(this)

        // 환영 문구를 사용자의 이름에 맞춰서 적어보자.
        val username = sharedManager.getUsername()
        binding.mainWelcome.text = String.format(getString(R.string.main_welcome), username)

        val viewPager: ViewPager2 = binding.fragmentViewpager
        val viewpagerFragmentAdapter = MainActivityFragmentAdapter(this)

        val tabDescription = listOf("홈", "좋아요 누른 팟", "활동 중인 팟")

        viewPager.adapter = viewpagerFragmentAdapter

        val tabLayout: TabLayout = binding.tabLayout

        TabLayoutMediator(tabLayout, viewPager) {
                tab, position -> tab.text = tabDescription[position]
        }.attach()
    }
}

class MainActivityFragmentAdapter(fragmentActivity: FragmentActivity)
    : FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = listOf(MainFragment(), LikedFragment(), MyPartyFragment())

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}