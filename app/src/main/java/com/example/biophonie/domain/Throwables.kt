package com.example.biophonie.domain

class BadRequestThrowable(override val message: String) : Throwable()
class ConflictThrowable(override val message: String): Throwable()
class NotFoundThrowable(override val message: String): Throwable()
class UnauthorizedThrowable(override val message: String): Throwable()
class InternalErrorThrowable(override val message: String): Throwable()
class UnexpectedThrowable(override val message: String = "something unexpected happened"): Throwable()
class NoConnectionThrowable(override val message: String = "could not connect to server"): Throwable()