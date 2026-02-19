package com.blackbox.connect.ui.screens.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackbox.connect.data.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                
                firestore.collection("chats")
                    .whereArrayContains("participants", userId)
                    .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            _uiState.update { it.copy(isLoading = false, error = error.message) }
                            return@addSnapshotListener
                        }
                        
                        val chats = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Chat::class.java)
                        } ?: emptyList()
                        
                        _uiState.update { it.copy(isLoading = false, chats = chats) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun createChat(participants: List<String>) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val chatId = UUID.randomUUID().toString()
                
                val chatData = hashMapOf(
                    "chatId" to chatId,
                    "participants" to (participants + userId).distinct(),
                    "isGroup" to (participants.size > 1),
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                
                firestore.collection("chats").document(chatId).set(chatData).await()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

data class ChatsUiState(
    val isLoading: Boolean = false,
    val chats: List<Chat> = emptyList(),
    val error: String? = null
)
