package fr.labomg.biophonie.core.utils

interface TokenProvider {
    suspend fun getToken(): Result<String>

    suspend fun refreshToken(): Result<String>
}
