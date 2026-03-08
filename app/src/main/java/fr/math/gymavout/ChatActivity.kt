package fr.math.gymavout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var partnerId: String
    private lateinit var currentUserId: String
    private lateinit var database: DatabaseReference
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        partnerId = intent.getStringExtra("partnerId") ?: return
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance("https://gymavout-app-default-rtdb.europe-west1.firebasedatabase.app/").reference

        val btnBack = findViewById<ImageView>(R.id.btnChatBack)
        val imgPartner = findViewById<ImageView>(R.id.imgChatPartner)
        val txtPartnerName = findViewById<TextView>(R.id.txtChatPartnerName)
        val rvMessages = findViewById<RecyclerView>(R.id.rvMessages)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<FloatingActionButton>(R.id.btnSendMessage)

        btnBack.setOnClickListener { finish() }

        // Charger infos partenaire
        database.child("users").child(partnerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val prenom = snapshot.child("prenom").value?.toString() ?: ""
                val nom = snapshot.child("nom").value?.toString() ?: ""
                val url = snapshot.child("profileImageUrl").value?.toString()
                txtPartnerName.text = "$prenom $nom"
                Glide.with(this@ChatActivity)
                    .load(if (!url.isNullOrEmpty()) url else R.drawable.logo_gymavout)
                    .circleCrop()
                    .into(imgPartner)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Setup RecyclerView - On retire stackFromEnd pour que les messages commencent en haut
        messageAdapter = MessageAdapter(messageList, currentUserId)
        rvMessages.layoutManager = LinearLayoutManager(this)
        rvMessages.adapter = messageAdapter

        // Charger messages
        val chatId = if (currentUserId < partnerId) "${currentUserId}_${partnerId}" else "${partnerId}_${currentUserId}"
        database.child("chats").child(chatId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (msgSnap in snapshot.children) {
                    val msg = msgSnap.getValue(ChatMessage::class.java)
                    if (msg != null) messageList.add(msg)
                }
                messageAdapter.notifyDataSetChanged()
                // On scrolle vers le bas uniquement s'il y a des messages
                if (messageList.isNotEmpty()) rvMessages.scrollToPosition(messageList.size - 1)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val messageData = ChatMessage(currentUserId, text, System.currentTimeMillis())
                database.child("chats").child(chatId).push().setValue(messageData)
                etMessage.text.clear()
            }
        }
    }

    data class ChatMessage(
        val senderId: String = "",
        val message: String = "",
        val timestamp: Long = 0
    )

    class MessageAdapter(private val messages: List<ChatMessage>, private val currentUserId: String) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val msg = messages[position]
            val vh = holder as MessageViewHolder
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = sdf.format(Date(msg.timestamp))

            if (msg.senderId == currentUserId) {
                vh.layoutMy.visibility = View.VISIBLE
                vh.layoutPartner.visibility = View.GONE
                vh.txtMyMsg.text = msg.message
                vh.txtMyTime.text = time
            } else {
                vh.layoutMy.visibility = View.GONE
                vh.layoutPartner.visibility = View.VISIBLE
                vh.txtPartnerMsg.text = msg.message
                vh.txtPartnerTime.text = time
            }
        }

        override fun getItemCount() = messages.size

        class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val layoutPartner = view.findViewById<View>(R.id.layoutPartnerMessage)
            val layoutMy = view.findViewById<View>(R.id.layoutMyMessage)
            val txtPartnerMsg = view.findViewById<TextView>(R.id.txtPartnerMessage)
            val txtPartnerTime = view.findViewById<TextView>(R.id.txtPartnerTime)
            val txtMyMsg = view.findViewById<TextView>(R.id.txtMyMessage)
            val txtMyTime = view.findViewById<TextView>(R.id.txtMyTime)
        }
    }
}
