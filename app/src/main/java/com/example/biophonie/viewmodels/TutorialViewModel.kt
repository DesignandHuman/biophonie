package com.example.biophonie.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.*
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.biophonie.network.ClientWeb
import com.example.biophonie.network.asDomainModel
import com.example.biophonie.ui.activities.TutorialActivity
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
        viewModelScope.launch {
            val user = createUser(name)
            if (user != null) {
                storeUser(user)
                _shouldStartActivity.value = true
            }
        }
    }

    private fun storeUser(user: User){
        val prefs = getApplication<Application>().getSharedPreferences(null, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("name", user.name)
        user.id?.let { editor.putInt("id", it) }
        editor.apply()

        val masterKey = MasterKey.Builder(getApplication())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            getApplication(),
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val encryptedEditor = sharedPreferences.edit()
        encryptedEditor.putString("password", user.password)
        encryptedEditor.apply()
    }

    private suspend fun createUser(name: String): User? {
        return withContext(Dispatchers.IO) {
            val request = ClientWeb.webService.postUser(name)
            if (request.isSuccessful) {
                return@withContext request.body()?.asDomainModel()
            } else {
                when (request.code()) {
                    HttpURLConnection.HTTP_CONFLICT -> _warning.postValue("Nom déjà utilisé")
                    HttpURLConnection.HTTP_INTERNAL_ERROR -> _warning.postValue("Service indisponible")
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