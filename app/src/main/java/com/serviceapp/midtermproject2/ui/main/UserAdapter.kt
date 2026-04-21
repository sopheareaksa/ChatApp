package com.serviceapp.midtermproject2.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.serviceapp.midtermproject2.R
import com.serviceapp.midtermproject2.data.model.User
import com.serviceapp.midtermproject2.databinding.ItemUserBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserAdapter(private val onUserClick: (User) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var users = listOf<User>()

    fun setUsers(usersList: List<User>) {
        users = usersList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvUserName.text = user.name
            
            // Show last message if available, otherwise show status
            binding.tvUserStatus.text = if (user.lastMessage.isNotEmpty()) {
                user.lastMessage
            } else {
                user.status
            }

            // Format timestamp
            if (user.lastMessageTimestamp > 0) {
                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                binding.tvTime.text = sdf.format(Date(user.lastMessageTimestamp))
            } else {
                binding.tvTime.text = ""
            }

            // Set profile image based on email
            val imageRes = when (user.email.lowercase()) {
                "reaksa13@gmail.com" -> R.drawable.boy
                "sokchanleap@gmail.com" -> R.drawable.girl
                else -> android.R.drawable.ic_menu_gallery // Default image
            }
            binding.ivUserImage.setImageResource(imageRes)

            binding.root.setOnClickListener { onUserClick(user) }
        }
    }
}
