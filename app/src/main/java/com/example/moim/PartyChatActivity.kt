package com.example.moim

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moim.databinding.ActivityPartyChatBinding
import com.example.moim.databinding.ChatItemBinding
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URISyntaxException
import kotlin.coroutines.CoroutineContext

class PartyChatActivity: AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityPartyChatBinding

    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job
    private val gson = Gson()

    private lateinit var socket: Socket
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var sharedManager: SharedManager

    private var offset = 0
    private var partyId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPartyChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val partyInformation: Party = intent.extras?.getSerializable("party_info")!! as Party

        partyId = partyInformation.common.partyId

        chatAdapter = ChatAdapter()
        sharedManager = SharedManager(this)

        binding.textChatName.text = partyInformation.common.partyName

        val userid = sharedManager.getUserId()
        val username = sharedManager.getUsername()

        val chatList = runBlocking(coroutineContext) {
            withContext(coroutineContext) {
                val response = retrofitHandler.getPartyChatList(partyInformation.common.partyId, offset)
                response.execute().body()?:mutableListOf()
            }
        }

        try {
            socket = IO.socket("http://$ipAddress")
            socket.connect()
            val me = JSONObject()
            me.put("userid", sharedManager.getUserId())
            me.put("party_id", partyId)

            socket.emit("iAm", me)
            socket.on("chatMessage", onChatMessage)
        }
        catch (e: URISyntaxException) {
            e.printStackTrace()
            Log.d("SOCKET", "연결 실패")
            Toast.makeText(this, "연결 실패", Toast.LENGTH_SHORT).show()
            binding.buttonSend.isEnabled = false
            binding.edittextChatMain.isEnabled = false
        }


        chatAdapter.chatList = chatList
        binding.recyclerChat.adapter = chatAdapter
        binding.recyclerChat.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)

        binding.buttonSend.setOnClickListener {
            val date = getCurrentDateTime()
            val dateString = date.toString("yyyy-MM-dd hh:mm:ss")
            val dateList = dateString.split(" ")

            val chatItem = ChatItem(
                chatId = -1,
                chatType = "chat",
                partyId = partyInformation.common.partyId,
                userid = userid,
                username = username,
                chatContent = binding.edittextChatMain.text.toString(),
                chatTime = dateList[1],
                chatDate = dateList[0],
            )
            Log.d("EMIT", binding.edittextChatMain.text.toString())
            socket.emit("chatMessage", gson.toJson(chatItem))
            binding.edittextChatMain.setText("")
        }

        binding.buttonMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            menuInflater.inflate(R.menu.popup, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_menu_info -> {
                        val intent = Intent(this, PartyManagerActivity::class.java)
                    }
                    R.id.action_menu_leave -> {
                        // TODO: 경고 팝업 보이게 하기
                    }
                    else -> throw Error("Impossible case!!")
                }

                false
            }
        }
    }

    private val onChatMessage = Emitter.Listener { args ->
        val chatItem = gson.fromJson(gson.toJson(args[0]), ChatItem::class.java)
        Log.d("RECEIVE", "$chatItem")
        this.runOnUiThread {
            chatAdapter.addChat(chatItem)
        }
//        chatAdapter.addChat(chatItem)
    }
}

class ChatAdapter: RecyclerView.Adapter<ChatAdapter.MyViewHolder>(){
    var chatList = mutableListOf<ChatItem>()
    private lateinit var binding: ChatItemBinding
    private lateinit var sharedManager: SharedManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        sharedManager = SharedManager(binding.root.context)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = chatList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    fun addChat(chatItem: ChatItem) {
        this.chatList.add(0, chatItem)
        this.notifyItemInserted(0)
    }

    inner class MyViewHolder(private val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val username = sharedManager.getUsername()
        private val userid = sharedManager.getUserId()

        fun bind(chatItem: ChatItem) {
            val context = binding.root.context

            // 챗의 종류에 따라 보이는 요소 바꾸기
            when (chatItem.chatType) {
                "chat" -> {
                    if (username == chatItem.username && userid == chatItem.userid) {
                        // 내 채팅이다.
                        binding.root.removeView(binding.constraintOthersChat)
                        binding.myTextTime.text = parseTimeString(chatItem.chatTime)
                        binding.myTextChatContent.text = chatItem.chatContent
                    }
                    else {
                        // 남의 채팅이다.
                        binding.root.removeView(binding.constraintMyChat)
                        binding.othersTextUsername.text = chatItem.username
                        binding.othersTextTime.text = parseTimeString(chatItem.chatTime)
                        binding.othersTextChatContent.text = chatItem.chatContent
                    }
                }
                "join" -> {
                    // TODO: Join 메시지의 경우.
                }
                "leave" -> {
                    // TODO: Leave 메시지의 경우. 두 경우 모두 뷰 추가가 필요하다.
                }
                "new_date" -> {
                    Unit
                }
                // 이 외의 경우에는 아무것도 하지 않는다.
                else -> Unit
            }



            // TODO: 날짜가 달라졌을 때 날짜 블럭 추가하기.. 후순위로.
        }
    }
}