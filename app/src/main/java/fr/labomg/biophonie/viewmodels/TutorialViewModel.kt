package fr.labomg.biophonie.viewmodels

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import fr.labomg.biophonie.data.domain.BadRequestThrowable
import fr.labomg.biophonie.data.domain.ConflictThrowable
import fr.labomg.biophonie.data.domain.InternalErrorThrowable
import fr.labomg.biophonie.data.domain.NoConnectionThrowable
import fr.labomg.biophonie.data.source.TutorialRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class TutorialViewModel(
    private val tutorialRepository: TutorialRepository
) : ViewModel() {

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
            nameEntered.length < 3 -> _warning.value = "Le nom doit faire plus de 2 caractères"
            else -> viewModelScope.launch {
                tutorialRepository.postUser(nameEntered)
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

    class ViewModelFactory(private val tutorialRepository: TutorialRepository) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TutorialViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TutorialViewModel(tutorialRepository) as T
            }
            throw IllegalArgumentException("Unknown class name")
        }
    }
}