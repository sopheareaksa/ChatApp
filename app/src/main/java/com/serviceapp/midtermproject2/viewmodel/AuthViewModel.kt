package com.serviceapp.midtermproject2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.serviceapp.midtermproject2.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel @JvmOverloads constructor(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    init {
        _user.value = repository.currentUser
    }
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.register(name, email, password)
            if (result.isSuccess) {
                _user.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
            _loading.value = false
        }
    }
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.login(email, password)
            if (result.isSuccess) {
                _user.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
            _loading.value = false
        }
    }
    fun logout() {
        repository.logout()
        _user.value = null
    }
    fun clearError() {
        _error.value = null
    }
}
