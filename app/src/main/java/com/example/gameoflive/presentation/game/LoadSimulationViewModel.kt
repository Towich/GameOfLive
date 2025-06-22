package com.example.gameoflive.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameoflive.domain.model.SaveInfo
import com.example.gameoflive.data.repository.SimulationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoadSimulationViewModel @Inject constructor(
    private val repository: SimulationRepository
) : ViewModel() {
    private val _saves = MutableStateFlow<List<SaveInfo>>(emptyList())
    val saves: StateFlow<List<SaveInfo>> = _saves

    fun loadSaves() {
        viewModelScope.launch {
            _saves.value = repository.listSaves()
        }
    }

    fun deleteSave(fileName: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteSave(fileName)
            if (result) loadSaves()
            onResult(result)
        }
    }
} 