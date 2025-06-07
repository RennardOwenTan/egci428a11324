package com.egci428.a11324

import java.io.Serializable

data class FortuneCookie(
    val message: String,
    val time: String,
    val status: String // To determine message color
): Serializable