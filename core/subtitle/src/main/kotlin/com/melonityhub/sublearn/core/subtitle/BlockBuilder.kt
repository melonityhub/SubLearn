package com.melonityhub.sublearn.core.subtitle

import com.melonityhub.sublearn.core.model.Block
import com.melonityhub.sublearn.core.model.Cue

/** Thresholds for merging cues into blocks (D-017). Defaults match the settings defaults. */
data class BlockOptions(
    val maxGapMs: Long = 1_200L,
    val maxDurationMs: Long = 6_000L,
    val maxChars: Int = 96,
)

/**
 * Merges consecutive cues into readable blocks (SUB-7: fixes sentences split across cues).
 * A new block starts when the gap is too long, the block is too long or too many characters, or the
 * previous cue ended a sentence. Cues are assumed to be per-track; sorting is done here.
 */
object BlockBuilder {
    fun build(cues: List<Cue>, options: BlockOptions = BlockOptions()): List<Block> {
        if (cues.isEmpty()) return emptyList()
        val sorted = cues.sortedBy { it.startMs }
        val blocks = mutableListOf<Block>()
        var group = mutableListOf<Cue>()

        for (cue in sorted) {
            if (group.isNotEmpty() && startsNewBlock(group, cue, options)) {
                blocks += toBlock(blocks.size, group)
                group = mutableListOf()
            }
            group += cue
        }
        if (group.isNotEmpty()) blocks += toBlock(blocks.size, group)
        return blocks
    }

    private fun startsNewBlock(group: List<Cue>, next: Cue, options: BlockOptions): Boolean {
        val last = group.last()
        val gap = next.startMs - last.endMs
        val spanEnd = maxOf(group.maxOf { it.endMs }, next.endMs)
        val spanMs = spanEnd - group.first().startMs
        val chars = group.sumOf { it.text.length } + next.text.length + group.size
        return gap > options.maxGapMs ||
            spanMs > options.maxDurationMs ||
            chars > options.maxChars ||
            TextNormalizer.endsSentence(last.text)
    }

    private fun toBlock(index: Int, group: List<Cue>): Block {
        val text = group.joinToString(" ") { it.text.replace('\n', ' ') }
            .replace(Regex("\\s+"), " ")
            .trim()
        return Block(
            index = index,
            startMs = group.first().startMs,
            endMs = group.maxOf { it.endMs },
            text = text,
            cueIds = group.map { it.id },
        )
    }
}
