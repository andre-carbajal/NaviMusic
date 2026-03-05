package net.andrecarbajal.naviMusic.util

import java.net.URI
import java.util.*
import java.util.regex.Pattern

object URLUtils {
    @JvmStatic
    fun getURLParam(url: String, param: String): Optional<String> {
        val regex = "$param=([^&]*)"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(url)

        return if (matcher.find()) Optional.of(matcher.group(1)) else Optional.empty()
    }

    @JvmStatic
    fun isURL(input: String?): Boolean {
        if (input == null) return false
        return try {
            URI(input)
            true
        } catch (_: Exception) {
            false
        }
    }
}
