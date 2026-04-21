package com.serviceapp.midtermproject2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serviceapp.midtermproject2.data.model.Message
import com.serviceapp.midtermproject2.data.model.User
import com.serviceapp.midtermproject2.data.repository.ChatRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository = ChatRepository()) : ViewModel() {
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages
    fun getUsers() {
        viewModelScope.launch {
            repository.getUsers().collect { userList ->
                _users.value = userList
            }
        }
    }
    fun getMessages(otherUserId: String) {
        viewModelScope.launch {
            repository.getMessages(otherUserId).collect { messageList ->
                _messages.value = messageList
            }
        }
    }
    fun sendMessage(receiverId: String, messageText: String) {
        viewModelScope.launch {
            repository.sendMessage(receiverId, messageText)
        }
    }
}
