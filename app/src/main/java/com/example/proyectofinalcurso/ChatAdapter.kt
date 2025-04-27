package com.example.proyectofinalcurso

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChatAdapter(private val messages: List<ChatMessage>, private val currentUserId: String) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMessage: TextView = itemView.findViewById(R.id.textMessage)
        val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == 1) R.layout.item_message_sent else R.layout.item_message_received
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.textMessage.text = message.mensaje

        // Cargar la imagen de perfil con Glide
        if (!message.senderProfileImageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(message.senderProfileImageUrl)
                .placeholder(R.drawable.hb1) // Imagen por defecto si no hay URL
                .into(holder.profileImageView)
        } else {
            holder.profileImageView.setImageResource(R.drawable.hb2)
        }
    }

    override fun getItemCount(): Int = messages.size
}

