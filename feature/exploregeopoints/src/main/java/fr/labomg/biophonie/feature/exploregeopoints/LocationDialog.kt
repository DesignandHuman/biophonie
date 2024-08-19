package fr.labomg.biophonie.feature.exploregeopoints

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class LocationDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Paramètres GPS")
            .setMessage("Le GPS n'est pas actif. Voulez-vous l'activer dans les menus ?")
            .setPositiveButton("Paramètres") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Annuler") { _, _ -> dismiss() }
            .create()
}
