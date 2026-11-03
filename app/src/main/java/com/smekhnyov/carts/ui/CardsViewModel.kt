package com.smekhnyov.carts.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smekhnyov.carts.data.CardDraft
import com.smekhnyov.carts.data.CardRepository
import com.smekhnyov.carts.data.LoyaltyCard
import kotlinx.coroutines.launch

class CardsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CardRepository(application)

    val cards = repository.cards
    val isLoading = repository.isLoading

    init {
        viewModelScope.launch {
            runCatching { repository.load() }
        }
    }

    fun saveCard(draft: CardDraft, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = runCatching { repository.save(draft) }.isSuccess
            onResult(success)
        }
    }

    fun deleteCard(card: LoyaltyCard, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = runCatching { repository.delete(card) }.isSuccess
            onResult(success)
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}
