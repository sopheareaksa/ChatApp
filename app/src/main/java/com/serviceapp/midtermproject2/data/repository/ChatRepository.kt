package com.serviceapp.midtermproject2.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.serviceapp.midtermproject2.data.model.Message
import com.serviceapp.midtermproject2.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    fun getUsers(): Flow<List<User>> = callbackFlow {
        val usersRef = database.getReference("users")
        val lastMsgsRef = database.getReference("last_messages").child(currentUserId)
        
        var currentUsers = listOf<User>()
        var lastMessagesMap = mapOf<String, Pair<String, Long>>()

        fun emitUpdatedList() {
            val updatedList = currentUsers.map { user ->
                val lastMsgData = lastMessagesMap[user.uid]
                user.copy(
                    lastMessage = lastMsgData?.first ?: "",
                    lastMessageTimestamp = lastMsgData?.second ?: 0L
                )
            }.sortedByDescending { it.lastMessageTimestamp }
            trySend(updatedList)
        }

        val usersListener = usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUsers = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                    .filter { it.uid != currentUserId }
                emitUpdatedList()
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })

        val lastMsgsListener = lastMsgsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMap = mutableMapOf<String, Pair<String, Long>>()
                snapshot.children.forEach { child ->
                    val text = child.child("text").value?.toString() ?: ""
                    val time = child.child("timestamp").value?.toString()?.toLongOrNull() ?: 0L
                    newMap[child.key!!] = Pair(text, time)
                }
                lastMessagesMap = newMap
                emitUpdatedList()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        awaitClose { 
            usersRef.removeEventListener(usersListener)
            lastMsgsRef.removeEventListener(lastMsgsListener)
        }
    }

    fun getMessages(otherUserId: String): Flow<List<Message>> = callbackFlow {
        val chatRoomId = getChatRoomId(currentUserId, otherUserId)
        val messagesRef = database.getReference("chats").child(chatRoomId)
        val listener = messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                trySend(messages)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    suspend fun sendMessage(receiverId: String, messageText: String) {
        val myId = currentUserId
        if (myId.isEmpty()) return
        
        val chatRoomId = getChatRoomId(myId, receiverId)
        val messagesRef = database.getReference("chats").child(chatRoomId)
        val messageId = messagesRef.push().key ?: return
        
        val timestamp = System.currentTimeMillis()
        val message = Message(
            messageId = messageId,
            senderId = myId,
            receiverId = receiverId,
            message = messageText,
            timestamp = timestamp
        )
        
        // Save the message
        messagesRef.child(messageId).setValue(message).await()

        // Update last message metadata for both sender and receiver
        val lastMsgData = mapOf(
            "text" to messageText,
            "timestamp" to timestamp
        )
        val lastMsgUpdates = mapOf(
            "last_messages/$myId/$receiverId" to lastMsgData,
            "last_messages/$receiverId/$myId" to lastMsgData
        )
        database.reference.updateChildren(lastMsgUpdates).await()
    }

    private fun getChatRoomId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }
}
