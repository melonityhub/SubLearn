package com.melonityhub.sublearn.core.subtitle

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

/**
 * Turns subtitle bytes into text (D-015): BOM detection, then strict UTF-8, then the configured
 * legacy charset (Windows-1256 by default, for Persian/Arabic files saved by older tools).
 */
object SubtitleDecoder {
    val DEFAULT_LEGACY_CHARSET: Charset = Charset.forName("windows-1256")

    fun decode(bytes: ByteArray, legacyCharset: Charset = DEFAULT_LEGACY_CHARSET): String {
        if (bytes.startsWith(0xEF, 0xBB, 0xBF)) return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        if (bytes.startsWith(0xFF, 0xFE)) return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        if (bytes.startsWith(0xFE, 0xFF)) return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        return try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (_: CharacterCodingException) {
            String(bytes, legacyCharset)
        }
    }

    private fun ByteArray.startsWith(vararg prefix: Int): Boolean {
        if (size < prefix.size) return false
        return prefix.indices.all { this[it] == prefix[it].toByte() }
    }
}
