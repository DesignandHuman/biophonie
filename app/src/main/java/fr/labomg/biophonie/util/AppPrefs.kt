package fr.labomg.biophonie.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object AppPrefs {

    private const val PREFS_NAME = "shared_prefs"
    private const val ENCRYPTED_PREFS_NAME = "encrypted_shared_prefs"

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var encryptedSharedPrefs: SharedPreferences

    fun setup(context: Context) {
        sharedPrefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        encryptedSharedPrefs = EncryptedSharedPreferences(context,
            ENCRYPTED_PREFS_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var userId: Int?
        get() = Key.USERID.getInt()
        set(value) = Key.USERID.setInt(value)

    var userName: String?
        get() = Key.USERNAME.getString()
        set(value) = Key.USERNAME.setString(value)

    var password: String?
        get() = EncryptedKey.PASSWORD.getString()
        set(value) = EncryptedKey.PASSWORD.setString(value)

    var token: String?
        get() = EncryptedKey.TOKEN.getString()
        set(value) = EncryptedKey.TOKEN.setString(value)

    private enum class EncryptedKey {
        TOKEN, PASSWORD;
        
        fun getString(): String? = if (exists()) encryptedSharedPrefs.getString(name, "") else null
        fun setString(value: String?) = value?.let { encryptedSharedPrefs.edit { putString(name, value) } } ?: remove()

        fun exists(): Boolean = encryptedSharedPrefs.contains(name)
        fun remove() = encryptedSharedPrefs.edit { remove(name) }
    }
    
    private enum class Key {
        USERNAME, USERID;
        
        fun getInt(): Int? = if (exists()) sharedPrefs.getInt(name, 0) else null
        fun setInt(value: Int?) = value?.let { sharedPrefs.edit { putInt(name, value) } } ?: remove()
        fun getString(): String? = if (exists()) sharedPrefs.getString(name, "") else null
        fun setString(value: String?) = value?.let { sharedPrefs.edit { putString(name, value) } } ?: remove()

        fun exists(): Boolean = sharedPrefs.contains(name)
        fun remove() = sharedPrefs.edit { remove(name) }
    }
}