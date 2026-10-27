package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ComponentEntity
import com.example.data.ComponentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ComponentRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        val componentDao = AppDatabase.getDatabase(application).componentDao()
        repository = ComponentRepository(componentDao, application)
        viewModelScope.launch {
            repository.populateDatabaseIfEmpty()
        }
    }

    val components: StateFlow<List<ComponentEntity>> = repository.allComponents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
