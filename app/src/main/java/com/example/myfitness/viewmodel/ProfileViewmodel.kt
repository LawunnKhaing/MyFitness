package com.example.myfitness.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitness.database.User
import com.example.myfitness.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            _user.value = repository.getUserData()
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        val userId = repository.getCurrentUserId() ?: return
        repository.deleteAccount { success ->
            if (success) {
                onComplete()
            }
        }
    }

    fun logout() {
        repository.logout()
    }
}
