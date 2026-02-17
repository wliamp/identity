package io.github.wliamp.kit.id.core

import io.github.wliamp.kit.id.core.Oauth.*
import io.github.wliamp.kit.id.core.OauthProps.*
import org.springframework.http.HttpMethod.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.*

internal class OauthGoogle internal constructor(
    private val props: GoogleProps,
    private val webClient: WebClient
) : IOauth {
    private val oauth = GOOGLE.name

    override fun verify(token: String): Mono<Boolean> =
        props.takeIf { it.clientId.isNotBlank() }
            ?.let { p ->
                fetchGoogle(token)
                    .map {
                        p.clientId == (it["aud"]?.toString()
                            ?: throw VerifyParseException(oauth, "Missing 'aud' in response"))
                    }
            }
            ?: error(
                VerifyConfigException(
                    oauth,
                    "Missing " +
                        "'provider.oauth.google.client-id'"
                )
            )

    override fun getInfo(token: String): Mono<Map<String, Any>> =
        fetchGoogle(token)

    private fun fetchGoogle(token: String) =
        webClient.fetchPayload(
            GET,
            "${props.baseUrl}${props.uri}",
            oauth,
            queryParams = mapOf("id_token" to token)
        )
}
