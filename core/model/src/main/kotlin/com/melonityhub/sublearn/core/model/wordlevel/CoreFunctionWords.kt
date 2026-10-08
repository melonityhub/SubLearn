package com.melonityhub.sublearn.core.model.wordlevel

/**
 * Authored for SubLearn (Apache-2.0): English function words and the most basic everyday verbs. It is
 * not taken from any frequency list, so there is no third-party licence to carry (D-020).
 */
object CoreFunctionWords {
    val set: Set<String> = """
        a an the and or but if so as at by for from in into of on to with without about after before
        between during over under up down out off again then than that this these those there here
        i me my mine we us our you your he him his she her it its they them their what which who whom
        whose when where why how all any both each few more most other some such no nor not only own
        same too very can could will would shall should may might must do does did done doing have has
        had having be am is are was were been being get got go goes went come came make made say said
        see saw know knew think thought take took give gave want wants like look looks use find found
        tell told ask asked seem feel try put keep let begin show hear play run move live believe
        bring happen write provide sit stand lose pay meet include continue set learn change lead
        understand watch follow stop create speak read allow add spend grow open walk win offer
        remember love consider appear buy wait serve die send expect build stay fall cut reach kill
        remain yes no ok okay oh hey hello thanks please sorry one two three four five six seven eight
        nine ten
    """.trimIndent().split(Regex("\\s+")).toSet()
}
