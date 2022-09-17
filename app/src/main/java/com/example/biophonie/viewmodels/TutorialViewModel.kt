package com.example.biophonie.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.biophonie.network.ClientWeb
import com.example.biophonie.network.NetworkAddUser
import com.example.biophonie.network.asDomainModel
import com.example.biophonie.util.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection

class TutorialViewModel(application: Application) : AndroidViewModel(application) {

    private val _warning = MutableLiveData<String>()
    val warning: LiveData<String>
        get() = _warning

    private val _shouldStartActivity = MutableLiveData<Boolean>()
    val shouldStartActivity: LiveData<Boolean>
        get() = _shouldStartActivity

    data class User(val name: String?, val id: Int?, val password: String?)

    fun onClickEnter(name: String) {
        if (name.isBlank())
            _warning.value = "Le nom ne peut pas être nul"
        else
            viewModelScope.launch {
                val user = createUser(name)
                if (user != null) {
                    storeUser(user)
                    _shouldStartActivity.value = true
                }
            }
    }

    private fun storeUser(user: User){
        AppPrefs.userId = user.id
        AppPrefs.userName = user.name
        AppPrefs.password = user.password
    }

    //TODO deal with connection failure -> crash
    private suspend fun createUser(name: String): User? {
        return withContext(Dispatchers.IO) {
            val response = ClientWeb.webService.postUser(NetworkAddUser(name))
            if (response.isSuccessful) {
                return@withContext response.body()?.asDomainModel()
            } else {
                when (response.code()) {
                    HttpURLConnection.HTTP_CONFLICT -> _warning.postValue("Nom déjà utilisé")
                    HttpURLConnection.HTTP_INTERNAL_ERROR -> _warning.postValue("Service indisponible")
                    HttpURLConnection.HTTP_BAD_REQUEST -> _warning.postValue("Le nom doit faire entre 3 et 30 caractères")
                    else -> _warning.postValue("Quelque chose a mal tourné")
                }
                return@withContext null
            }
        }
    }

    class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TutorialViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TutorialViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown class name")
        }
    }
}