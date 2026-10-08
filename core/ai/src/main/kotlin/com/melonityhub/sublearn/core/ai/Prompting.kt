package com.melonityhub.sublearn.core.ai

import com.melonityhub.sublearn.core.model.Block
import java.util.Locale

/**
 * Builds the context sent with an AI question (AI-4): the previous N blocks, the current block with
 * timestamps, and optionally the film title. Pure function, unit-tested.
 */
object AiContextBuilder {
    fun build(
        blocks: List<Block>,
        currentIndex: Int,
        previousBlockCount: Int,
        filmTitle: String?,
        includeTimestamps: Boolean,
    ): String {
        require(currentIndex in blocks.indices) { "currentIndex $currentIndex is outside 0..${blocks.lastIndex}" }
        val lines = mutableListOf<String>()
        if (!filmTitle.isNullOrBlank()) lines += "Film: ${filmTitle.trim()}"
        val from = (currentIndex - previousBlockCount.coerceAtLeast(0)).coerceAtLeast(0)
        if (from < currentIndex) {
            lines += "Previous lines:"
            for (i in from until currentIndex) lines += formatLine(blocks[i], includeTimestamps)
        }
        lines += "Current line:"
        lines += formatLine(blocks[currentIndex], includeTimestamps)
        return lines.joinToString("\n")
    }

    internal fun formatLine(block: Block, includeTimestamps: Boolean): String =
        if (includeTimestamps) "[${timestamp(block.startMs)} - ${timestamp(block.endMs)}] ${block.text}" else block.text

    internal fun timestamp(ms: Long): String {
        val total = ms.coerceAtLeast(0L)
        val h = total / 3_600_000L
        val m = (total / 60_000L) % 60
        val s = (total / 1_000L) % 60
        return String.format(Locale.ROOT, "%02d:%02d:%02d", h, m, s)
    }
}

/** Fills the prompt template (AI-2, AI-3). Placeholders: `{{context}}`, `{{selection}}`. */
object AiPromptBuilder {
    fun render(template: String, context: String, selection: String): String {
        val withContext = if (template.contains("{{context}}")) template.replace("{{context}}", context)
        else "$template\n\n$context"
        return if (withContext.contains("{{selection}}")) withContext.replace("{{selection}}", selection)
        else "$withContext\n\nSelected text: $selection"
    }
}
