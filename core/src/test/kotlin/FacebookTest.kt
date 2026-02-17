package io.github.wliamp.kit.id.core

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.wliamp.kit.id.core.OauthProps.*
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier
import reactor.test.StepVerifier.*

internal class FacebookTest : ITestSetup<FacebookProps, IOauth> {
    override lateinit var server: MockWebServer
    override lateinit var client: WebClient
    override lateinit var props: FacebookProps
    override lateinit var provider: IOauth
    override val mapper = ObjectMapper()

    override fun buildProps() = FacebookProps().apply {
        appId = "test-app"
        accessToken = "test-access-token"
        baseUrl = ""
        fields = ""
        vrfUri = "/debug_token"
        infoUri = "/me"
    }

    override fun buildProvider(props: FacebookProps, client: WebClient) =
        OauthFacebook(props, client)

    @BeforeEach
    fun setup() {
        server = MockWebServer()
        server.start()
        initServerAndClient()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `verify returns true when app_id matches`() {
        enqueueJson(server, mapOf("data" to mapOf("app_id" to "test-app")))
        create(provider.verify("dummy-token"))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `verify returns false when app_id mismatches`() {
        enqueueJson(server, mapOf("data" to mapOf("app_id" to "other-app")))
        create(provider.verify("dummy-token"))
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `verify errors when data missing`() {
        enqueueJson(server, mapOf("no_data" to "oops"))
        create(provider.verify("dummy-token"))
            .expectError(VerifyParseException::class.java)
            .verify()
    }

    @Test
    fun `verify errors when app_id missing`() {
        enqueueJson(server, mapOf("data" to mapOf("id" to "123456")))
        create(provider.verify("dummy-token"))
            .expectError(VerifyParseException::class.java)
            .verify()
    }

    @Test
    fun `verify errors when config missing appId`() {
        val bad = FacebookProps().apply {
            appId = ""
            accessToken = "test-access-token"
            baseUrl = ""
            vrfUri = "/debug_token"
            infoUri = "/me"
        }
        val fb = OauthFacebook(bad, client)
        create(fb.verify("dummy-token"))
            .expectError(VerifyConfigException::class.java)
            .verify()
    }

    @Test
    fun `verify errors when config missing accessToken`() {
        val bad = FacebookProps().apply {
            appId = "test-app"
            accessToken = ""
            baseUrl = ""
            vrfUri = "/debug_token"
            infoUri = "/me"
        }
        val fb = OauthFacebook(bad, client)
        create(fb.verify("dummy-token"))
            .expectError(VerifyConfigException::class.java)
            .verify()
    }

    @Test
    fun `verify builds correct facebook uri`() {
        enqueueJson(server, mapOf("data" to mapOf("app_id" to "test-app")))
        create(provider.verify("dummy-token"))
            .expectNext(true)
            .verifyComplete()
        val recorded = server.takeRequest()
        assertEquals(
            "${props.vrfUri}?input_token=dummy-token&access_token=${props.accessToken}",
            recorded.path
        )
    }

    @Test
    fun `getInfo works when fields config empty`() {
        val customProps = FacebookProps().apply {
            appId = "test-app"
            accessToken = "test-access-token"
            baseUrl = ""
            fields = ""
            vrfUri = "/debug_token"
            infoUri = "/me"
        }
        val fb = OauthFacebook(customProps, client)
        enqueueJson(server, mapOf("id" to "456"))
        create(fb.getInfo("dummy-token"))
            .expectNextMatches { it["id"] == "456" }
            .verifyComplete()
        val recorded = server.takeRequest()
        assertEquals(
            "${customProps.infoUri}?access_token=dummy-token",
            recorded.path
        )
    }

    @Test
    fun `getInfo builds correct uri without fields`() {
        enqueueJson(server, mapOf("id" to "123", "name" to "Alice"))
        create(provider.getInfo("dummy-token"))
            .expectNextMatches { it["id"] == "123" && it["name"] == "Alice" }
            .verifyComplete()
        val recorded = server.takeRequest()
        assertEquals(
            "${props.infoUri}?access_token=dummy-token",
            recorded.path
        )
    }

    @Test
    fun `getInfo builds correct uri with fields`() {
        val customProps = FacebookProps().apply {
            appId = "test-app"
            accessToken = "test-access-token"
            baseUrl = ""
            fields = "id,name"
            vrfUri = "/debug_token"
            infoUri = "/me"
        }
        val fb = OauthFacebook(customProps, client)
        enqueueJson(server, mapOf("id" to "123", "name" to "Alice"))
        create(fb.getInfo("dummy-token"))
            .expectNextMatches { it["id"] == "123" && it["name"] == "Alice" }
            .verifyComplete()
        val recorded = server.takeRequest()
        assertEquals(
            "${customProps.infoUri}?access_token=dummy-token&fields=${customProps.fields}",
            recorded.path
        )
    }
}
