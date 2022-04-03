package com.example.aikataulusuunnitteluapp.util

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import com.example.aikataulusuunnitteluapp.data.EventsRepository
import com.example.aikataulusuunnitteluapp.data.model.CalendarEntity
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle.MEDIUM

data class GenericViewState(
    val entities: List<CalendarEntity> = emptyList()
)

sealed class GenericAction {
    data class ShowSnackbar(val message: String, val undoAction: () -> Unit) : GenericAction()
}

class GenericViewModel(
    private val eventsRepository: EventsRepository
) : ViewModel() {

    private val _viewState = MutableLiveData<GenericViewState>()
    val viewState: LiveData<GenericViewState> = _viewState

    private val _actions = MutableLiveData<Event<GenericAction>>()
    val actions: LiveData<Event<GenericAction>> = _actions

    private val currentEntities: List<CalendarEntity>
        get() = _viewState.value?.entities.orEmpty()

    fun fetchEvents(yearMonths: List<YearMonth>) {
        eventsRepository.fetch(yearMonths = yearMonths) { entities ->
            val existingEntities = _viewState.value?.entities.orEmpty()
            _viewState.value = GenericViewState(entities = existingEntities + entities)
        }
    }




    class Factory(private val eventsRepository: EventsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GenericViewModel::class.java)) {
                return GenericViewModel(eventsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class ${modelClass.simpleName}")
        }
    }
}

fun ComponentActivity.genericViewModel(): Lazy<GenericViewModel> {
    val factoryPromise = {
        GenericViewModel.Factory(eventsRepository = EventsRepository(context = this))
    }
    return ViewModelLazy(GenericViewModel::class, { viewModelStore }, factoryPromise)
}


