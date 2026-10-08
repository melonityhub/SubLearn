package com.melonityhub.sublearn.core.player

import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import com.melonityhub.sublearn.core.settings.DecoderMode

/**
 * Maps the SW / HW / HW+ setting to real codec selection (ENG-4, D-012).
 * - SW: only software-only codecs.
 * - HW: only hardware-accelerated codecs; if none exist the player reports an error instead of hiding it.
 * - HW_PLUS: hardware codecs first, then software codecs as a fallback.
 */
object DecoderSelectors {
    fun forMode(mode: DecoderMode): MediaCodecSelector = MediaCodecSelector { mimeType, requiresSecure, requiresTunneling ->
        val all = MediaCodecUtil.getDecoderInfos(mimeType, requiresSecure, requiresTunneling)
        when (mode) {
            DecoderMode.SW -> all.filter { it.softwareOnly }
            DecoderMode.HW -> all.filter { it.hardwareAccelerated }
            DecoderMode.HW_PLUS -> all.filter { it.hardwareAccelerated } + all.filter { !it.hardwareAccelerated }
        }
    }
}
