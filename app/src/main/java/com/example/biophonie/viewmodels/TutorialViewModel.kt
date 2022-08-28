package com.example.biophonie.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.biophonie.ENCRYPTED_PREFS_NAME
import com.example.biophonie.PREFS_NAME
import com.example.biophonie.network.ClientWeb
import com.example.biophonie.network.NetworkAddUser
import com.example.biophonie.network.asDomainModel
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
        val prefs = getApplication<Application>().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with (prefs.edit()) {
            putString("username", user.name)
            user.id?.let { putInt("id",it) }
            apply()
        }

        val masterKey = MasterKey.Builder(getApplication())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            getApplication(),
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        with(sharedPreferences.edit()) {
            putString("password", user.password)
            apply()
        }
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