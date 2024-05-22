package fr.labomg.biophonie.viewmodels

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.labomg.biophonie.data.BadRequestThrowable
import fr.labomg.biophonie.data.ConflictThrowable
import fr.labomg.biophonie.data.InternalErrorThrowable
import fr.labomg.biophonie.data.NoConnectionThrowable
import fr.labomg.biophonie.data.source.TutorialRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TutorialViewModel @Inject constructor(private val tutorialRepository: TutorialRepository) : ViewModel() {

    private val _warning = MutableLiveData<String>()
    val warning: LiveData<String>
        get() = _warning

    private val _shouldStartActivity = MutableLiveData<Boolean>()
    val shouldStartActivity: LiveData<Boolean>
        get() = _shouldStartActivity

    val name = ObservableField<String>()

    fun onClickEnter() {
        val nameEntered = name.get()
        Timber.d("$nameEntered nameEntered")
        when {
            nameEntered == null -> _warning.value = "Le nom ne peut pas être nul"
            nameEntered.length < MINIMUM_NAME_LENGTH ->
                _warning.value = "Le nom doit faire plus de 2 caractères"
            else ->
                viewModelScope.launch {
                    tutorialRepository
                        .postUser(nameEntered)
                        .onSuccess { _shouldStartActivity.value = true }
                        .onFailure {
                            when (it) {
                                is NoConnectionThrowable ->
                                    _warning.value = "Connexion au serveur échouée"
                                is BadRequestThrowable -> _warning.value = "Mauvais nom"
                                is ConflictThrowable -> _warning.value = "Le nom est déjà pris"
                                is InternalErrorThrowable ->
                                    _warning.value = "Le serveur n’a pas pu traiter la requête"
                                else -> _warning.value = "Oups, une erreur s’est produite"
                            }
                        }
                }
        }
    }

    companion object {
        private const val MINIMUM_NAME_LENGTH = 3
    }
}
