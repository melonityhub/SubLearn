package com.melonityhub.sublearn.core.ai

import com.melonityhub.sublearn.core.model.AiProviderId
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ProvidersTest {
    private lateinit var server: MockWebServer
    private val client = OkHttpClient()
    private val base get() = server.url("/").toString()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun gemini_sendsKeyHeaderAndPathAndReadsCandidateText() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"candidates":[{"content":{"parts":[{"text":"Hello "},{"text":"there"}]}}]}""",
            ),
        )
        val provider = GeminiProvider(client, base)
        val answer = provider.complete(AiRequest(prompt = "Explain", model = "gemini-test", apiKey = "KEY-1"))

        val recorded = server.takeRequest()
        assertEquals("/models/gemini-test:generateContent", recorded.path)
        assertEquals("KEY-1", recorded.getHeader("x-goog-api-key"))
        assertTrue(recorded.body.readUtf8().contains("\"text\":\"Explain\""))
        assertEquals("Hello there", answer.text)
        assertEquals(AiProviderId.GEMINI, answer.provider)
    }

    @Test
    fun openAi_usesBearerAuthAndReadsChoiceMessage() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"choices":[{"message":{"role":"assistant","content":" Tone: formal "}}]}"""))
        val answer = OpenAiProvider(client, base).complete(AiRequest("Q", "gpt-test", "OAK"))

        val recorded = server.takeRequest()
        assertEquals("/chat/completions", recorded.path)
        assertEquals("Bearer OAK", recorded.getHeader("Authorization"))
        assertEquals("Tone: formal", answer.text)
    }

    @Test
    fun anthropic_usesApiKeyHeaderAndJoinsTextBlocks() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"content":[{"type":"thinking","thinking":"x"},{"type":"text","text":"Usage: common."}]}""",
            ),
        )
        val answer = AnthropicProvider(client, base).complete(AiRequest("Q", "claude-test", "ANT-KEY"))

        val recorded = server.takeRequest()
        assertEquals("/messages", recorded.path)
        assertEquals("ANT-KEY", recorded.getHeader("x-api-key"))
        assertEquals(AnthropicProvider.ANTHROPIC_VERSION, recorded.getHeader("anthropic-version"))
        assertEquals("Usage: common.", answer.text)
    }

    @Test
    fun httpErrors_mapToTypedExceptionWithProviderMessageButNoKey() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(403).setBody("""{"error":{"message":"API key not valid"}}"""))
        try {
            GeminiProvider(client, base).complete(AiRequest("Q", "m", "SECRET-KEY-VALUE"))
            fail("expected AiException.Http")
        } catch (e: AiException.Http) {
            assertEquals(403, e.code)
            assertTrue(e.detail.contains("API key not valid"))
            assertTrue("the key must never appear in an error", !e.message.orEmpty().contains("SECRET-KEY-VALUE"))
        }
    }

    @Test
    fun blankKey_failsBeforeAnyNetworkCall() = runBlocking {
        try {
            OpenAiProvider(client, base).complete(AiRequest("Q", "m", apiKey = " "))
            fail("expected MissingKey")
        } catch (_: AiException.MissingKey) {
            assertEquals(0, server.requestCount)
        }
    }

    @Test
    fun malformedBody_isBadResponse() = runBlocking {
        server.enqueue(MockResponse().setBody("<html>oops</html>"))
        try {
            AnthropicProvider(client, base).complete(AiRequest("Q", "m", "k"))
            fail("expected BadResponse")
        } catch (_: AiException.BadResponse) {
            // expected
        }
    }

    @Test
    fun contextBuilder_includesPreviousNBlocksTitleAndTimestamps() {
        val blocks = (0 until 15).map { i ->
            com.melonityhub.sublearn.core.model.Block(
                index = i,
                startMs = i * 10_000L,
                endMs = i * 10_000L + 4_000L,
                text = "line $i",
                cueIds = listOf(i.toLong()),
            )
        }
        val text = AiContextBuilder.build(blocks, currentIndex = 14, previousBlockCount = 10, filmTitle = "Sample Film", includeTimestamps = true)
        assertTrue(text.startsWith("Film: Sample Film"))
        assertTrue(text.contains("line 4"))
        assertTrue(!text.contains("line 3\n"))
        assertTrue(text.contains("[00:02:20 - 00:02:24] line 14"))
        assertEquals(11, text.lines().count { it.startsWith("[") }) // 10 previous blocks + the current block
    }

    @Test
    fun promptRender_fillsPlaceholdersAndAppendsSelectionWhenMissing() {
        val rendered = AiPromptBuilder.render("Explain.\n{{context}}\nSelected: {{selection}}", "CTX", "hello")
        assertEquals("Explain.\nCTX\nSelected: hello", rendered)
        val appended = AiPromptBuilder.render("Explain.", "CTX", "hello")
        assertTrue(appended.endsWith("Selected text: hello"))
    }
}
