package br.com.crisun.cleanarchitecture.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.crisun.architecture.data.repository.MessageRepository
import br.com.crisun.architecture.domain.Message
import br.com.crisun.architecture.domain.MessagesByHour
import br.com.crisun.architecture.domain.model.onFailure
import br.com.crisun.architecture.domain.model.onSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoLogger

class MainViewModel(private val repository: MessageRepository) : ViewModel(), AnkoLogger {
    val errorLiveData = MutableLiveData<String>()
    val messageLiveData = MutableLiveData<Message>()
    val messagesByHourLiveData = MutableLiveData<List<MessagesByHour>>()

    fun process() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                val result = repository.getMessage()

                result.onSuccess {
                    messageLiveData.value = it

                    withContext(Dispatchers.IO) {
                        repository.insert(it)
                    }
                }

                result.onFailure { errorLiveData.value = it.toString() }
            }

            messagesByHourLiveData.value = withContext(Dispatchers.IO) {
                repository.getMessagesByHour()
            }
        }
    }
}
