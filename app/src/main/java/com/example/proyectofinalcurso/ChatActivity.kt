package com.example.proyectofinalcurso

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messageList: MutableList<ChatMessage>

    private lateinit var imageViewProfile: ImageView
    private lateinit var textViewUserName: TextView

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    private lateinit var receiverId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_chat)

        receiverId = intent.getStringExtra("receiverId").orEmpty()

        recyclerView = findViewById(R.id.recyclerViewChat)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        imageViewProfile = findViewById(R.id.imageViewProfile)
        textViewUserName = findViewById(R.id.textViewUserName)

        messageList = mutableListOf()
        chatAdapter = ChatAdapter(messageList, currentUserId)
        recyclerView.adapter = chatAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        buttonSend.setOnClickListener {
            val msg = editTextMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                sendMessage(msg)
            }
        }

        listenForMessages()

        // Botón para volver al ChatFragment
        val buttonBack: Button = findViewById(R.id.buttonBack)
        buttonBack.setOnClickListener {
            onBackPressed() // Vuelve al fragmento anterior
        }

        // Cargar información del usuario receptor
        loadReceiverInfo()

        imageViewProfile.setOnClickListener {
            val fragment = UserDetailFragment.newInstance(receiverId)
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun sendMessage(text: String) {
        val message = ChatMessage(
            senderId = currentUserId,
            receiverId = receiverId,
            mensaje = text
        )

        db.collection("mensajes")
            .add(message)
            .addOnSuccessListener {
                editTextMessage.text.clear()
            }
    }

    private fun listenForMessages() {
        db.collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    messageList.clear()
                    for (doc in snapshots.documents) {
                        val message = doc.toObject(ChatMessage::class.java)
                        if (message != null &&
                            ((message.senderId == currentUserId && message.receiverId == receiverId) ||
                                    (message.senderId == receiverId && message.receiverId == currentUserId))
                        ) {
                            messageList.add(message)
                        }
                    }
                    chatAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messageList.size - 1)
                }
            }
    }

    private fun loadReceiverInfo() {
        db.collection("usuarios").document(receiverId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("nombre") ?: "Nombre desconocido"
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""

                    // Establecer el nombre del receptor
                    textViewUserName.text = userName

                    // Cargar la imagen de perfil con Glide
                    Glide.with(this)
                        .load(profileImageUrl)
                        .transform(RoundedCorners(30))
                        .placeholder(R.drawable.hb1)  // Imagen por defecto mientras carga
                        .into(imageViewProfile)
                }
            }
    }
}
