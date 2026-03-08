package fr.math.gymavout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MessageFragment : Fragment(R.layout.fragment_message) {

    private lateinit var rvConversations: RecyclerView
    private lateinit var conversationAdapter: ConversationAdapter
    private val conversationList = mutableListOf<Conversation>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvConversations = view.findViewById(R.id.rvMessages)
        rvConversations.layoutManager = LinearLayoutManager(context)
        conversationAdapter = ConversationAdapter(conversationList) { conversation ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("partnerId", conversation.partnerId)
            startActivity(intent)
        }
        rvConversations.adapter = conversationAdapter

        loadConversations()
    }

    private fun loadConversations() {
        if (currentUserId == null) return
        val database = FirebaseDatabase.getInstance("https://gymavout-app-default-rtdb.europe-west1.firebasedatabase.app/").reference
        
        database.child("chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                conversationList.clear()
                for (chatSnap in snapshot.children) {
                    val chatId = chatSnap.key ?: continue
                    if (chatId.contains(currentUserId)) {
                        val partnerId = chatId.replace(currentUserId, "").replace("_", "")
                        
                        // Récupérer le dernier message
                        val lastMsgSnap = chatSnap.children.lastOrNull()
                        val lastMsgText = lastMsgSnap?.child("message")?.value?.toString() ?: ""
                        val timestamp = lastMsgSnap?.child("timestamp")?.value as? Long ?: 0L

                        val conversation = Conversation(partnerId, lastMsgText, timestamp)
                        conversationList.add(conversation)
                    }
                }
                conversationList.sortByDescending { it.lastTimestamp }
                conversationAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    data class Conversation(
        val partnerId: String,
        val lastMessage: String,
        val lastTimestamp: Long
    )

    class ConversationAdapter(
        private val conversations: List<Conversation>,
        private val onClick: (Conversation) -> Unit
    ) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imgPartner: ImageView = view.findViewById(R.id.imgPartner)
            val txtName: TextView = view.findViewById(R.id.txtPartnerName)
            val txtLastMsg: TextView = view.findViewById(R.id.txtLastMessage)
            val txtTime: TextView = view.findViewById(R.id.txtTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val conv = conversations[position]
            val db = FirebaseDatabase.getInstance("https://gymavout-app-default-rtdb.europe-west1.firebasedatabase.app/").reference

            // Charger les infos du partenaire
            db.child("users").child(conv.partnerId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val prenom = snapshot.child("prenom").value?.toString() ?: ""
                    val nom = snapshot.child("nom").value?.toString() ?: ""
                    val url = snapshot.child("profileImageUrl").value?.toString()
                    
                    holder.txtName.text = "$prenom $nom"
                    Glide.with(holder.itemView.context)
                        .load(if (!url.isNullOrEmpty()) url else R.drawable.logo_gymavout)
                        .circleCrop()
                        .placeholder(R.drawable.logo_gymavout)
                        .into(holder.imgPartner)
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            holder.txtLastMsg.text = conv.lastMessage
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.txtTime.text = sdf.format(Date(conv.lastTimestamp))

            holder.itemView.setOnClickListener { onClick(conv) }
        }

        override fun getItemCount() = conversations.size
    }
}
