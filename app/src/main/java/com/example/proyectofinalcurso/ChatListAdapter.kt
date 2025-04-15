package com.example.proyectofinalcurso

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ChatListAdapter(
    private var users: List<ChatUser>,
    private val onChatClick: (String) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.chatUserName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val user = users[position]
        holder.textView.text = user.name
        holder.itemView.setOnClickListener {
            onChatClick(user.uid)
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateChats(newUsers: List<ChatUser>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
