package fr.labomg.biophonie.core.preferences.model

import fr.labomg.biophonie.core.model.User

data class PrefUser(val id: Int, val name: String, val password: String, val token: String)

fun PrefUser.toExternal() =
    User(id = this.id, name = this.name, password = this.password, token = this.token)

fun User.toPref(): PrefUser =
    PrefUser(id = this.id, name = this.name, password = this.password, token = this.token)
