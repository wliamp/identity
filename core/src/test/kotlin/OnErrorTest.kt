package io.github.wliamp.kit.id.core

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.wliamp.kit.id.core.OauthProps.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okhttp3.mockwebserver.SocketPolicy.*
import org.junit.jupiter.api.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier
import reactor.test.StepVerifier.*

/**
 * Note: OnErrorTest uses GoogleProps + IGoogle as the default provider
 * only to test the generic onError mechanism (provider-agnostic)
 * */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OnErrorTest : ITestSetup<GoogleProps, IOauth> {
    override val server = MockWebServer()
    override lateinit var client: WebClient
    override lateinit var props: GoogleProps
    override lateinit var provider: IOauth
    override val mapper = ObjectMapper()

    override fun buildProps() =
        GoogleProps().apply {
            clientId = "test-client"
            baseUrl = ""
        }

    override fun buildProvider(props: GoogleProps, client: WebClient) =
        OauthGoogle(props, client)

    @BeforeAll
    fun beforeAll() = server.start()

    @AfterAll
    fun afterAll() = server.shutdown()

    @BeforeEach
    fun setup() = initServerAndClient()

    @Test
    fun `http error OauthHttpException`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error":"Bad Request"}""")
        )
        create(provider.verify("dummy-token"))
            .expectError(VerifyHttpException::class.java)
            .verify()
    }

    @Test
    fun `network error OauthNetworkException`() {
        server.enqueue(MockResponse().setSocketPolicy(NO_RESPONSE))
        create(provider.verify("dummy-token"))
            .expectError(VerifyNetworkException::class.java)
            .verify()
    }

    @Test
    fun `invalid JSON OauthParseException`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("not-a-json")
        )
        create(provider.getInfo("dummy-token"))
            .expectError(VerifyParseException::class.java)
            .verify()
    }

    @Test
    fun `getInfo returns payload`() {
        enqueueJson(server, mapOf("id" to "123", "name" to "William"))
        create(provider.getInfo("dummy-token"))
            .expectNextMatches { it["id"] == "123" && it["name"] == "William" }
            .verifyComplete()
    }
}
