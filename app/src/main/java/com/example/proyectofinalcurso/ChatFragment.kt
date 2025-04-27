package com.example.proyectofinalcurso

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectofinalcurso.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatListAdapter: ChatListAdapter
    private val chatUsers = mutableListOf<ChatUser>()
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        // Configurar RecyclerView
        binding.recyclerViewChats.layoutManager = LinearLayoutManager(context)
        chatListAdapter = ChatListAdapter(chatUsers) { receiverId ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("receiverId", receiverId)
            startActivity(intent)
        }
        binding.recyclerViewChats.adapter = chatListAdapter

        loadChats()

        return binding.root
    }

    private fun loadChats() {
        val chatIdSet = mutableSetOf<String>()

        // Obtener mensajes donde el usuario es emisor
        db.collection("mensajes")
            .whereEqualTo("senderId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val receiverId = doc.getString("receiverId")
                    if (!receiverId.isNullOrEmpty() && receiverId != currentUserId) {
                        chatIdSet.add(receiverId)
                    }
                }

                // Obtener mensajes donde el usuario es receptor
                db.collection("mensajes")
                    .whereEqualTo("receiverId", currentUserId)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (doc in documents) {
                            val senderId = doc.getString("senderId")
                            if (!senderId.isNullOrEmpty() && senderId != currentUserId) {
                                chatIdSet.add(senderId)
                            }
                        }

                        fetchUserNames(chatIdSet.toList())
                    }
            }
    }

    private fun fetchUserNames(userIds: List<String>) {
        chatUsers.clear()
        var loadedCount = 0

        for (uid in userIds) {
            db.collection("usuarios").document(uid).get().addOnSuccessListener { document ->
                val name = document.getString("nombre") ?: "Usuario desconocido"
                val profileImageUrl = document.getString("profileImageUrl") // Obtener URL de la imagen

                chatUsers.add(ChatUser(uid, name, profileImageUrl)) // Añadir la imagen a ChatUser
                loadedCount++

                if (loadedCount == userIds.size) {
                    chatListAdapter.updateChats(chatUsers)
                }
            }
        }
    }

}
