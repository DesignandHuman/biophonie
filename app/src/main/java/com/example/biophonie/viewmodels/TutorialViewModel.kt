package com.example.biophonie.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.biophonie.domain.*
import com.example.biophonie.network.ClientWeb
import com.example.biophonie.network.NetworkAddUser
import com.example.biophonie.network.NetworkUser
import com.example.biophonie.network.asDomainModel
import com.example.biophonie.repositories.TutorialRepository
import com.example.biophonie.util.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.HttpURLConnection.*

private const val TAG = "TutorialViewModel"
class TutorialViewModel(application: Application) : AndroidViewModel(application) {

    private val _warning = MutableLiveData<String>()
    val warning: LiveData<String>
        get() = _warning

    private val _shouldStartActivity = MutableLiveData<Boolean>()
    val shouldStartActivity: LiveData<Boolean>
        get() = _shouldStartActivity

    fun onClickEnter(name: String) {
        when {
            name.isBlank() -> _warning.value = "Le nom ne peut pas être nul"
            name.length < 3 -> _warning.value = "Le nom doit faire plus de 2 caractères"
            else -> viewModelScope.launch {
                TutorialRepository().postUser(name)
                    .onSuccess {
                        _shouldStartActivity.value = true
                    }
                    .onFailure {
                        when (it) {
                            is NoConnectionThrowable -> _warning.value = "Connexion au serveur échouée"
                            is BadRequestThrowable -> _warning.value = "Mauvais nom"
                            is ConflictThrowable -> _warning.value = "Le nom est déjà pris"
                            is InternalErrorThrowable -> _warning.value = "Le serveur n’a pas pu traiter la requête"
                            else -> _warning.value = "Oups, une erreur s’est produite"
                        }
                    }
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