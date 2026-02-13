package io.github.wliamp.pro.vrf

import com.fasterxml.jackson.core.JsonProcessingException
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.net.ConnectException
import java.net.SocketTimeoutException

internal fun WebClient.fetchPayload(
    method: HttpMethod,
    uri: String,
    provider: String,
    headers: Map<String, String> = emptyMap(),
    queryParams: Map<String, String> = emptyMap(),
    body: Any? = null
): Mono<Map<String, Any>> =
    this.method(method)
        .uri {
            it.path(uri)
            queryParams.forEach { (k, v) -> it.queryParam(k, v) }
            it.build()
        }
        .apply { headers.forEach { (k, v) -> this.header(k, v) } }
        .apply { body?.let { this.bodyValue(it) } }
        .retrieve()
        .onStatus({ it.isError }) { resp ->
            resp.bodyToMono<String>()
                .flatMap { Mono.error(VerifyHttpException(provider, resp.statusCode().value(), it)) }
        }
        .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
        .onErrorMap {
            when (it) {
                is VerifyException -> it
                is ConnectException,
                is SocketTimeoutException,
                is WebClientRequestException -> VerifyNetworkException(provider, it)
                is JsonProcessingException -> VerifyParseException(provider, "Invalid JSON", it)
                is DecodingException -> {
                    val cause = it.cause
                    if (cause is JsonProcessingException)
                        VerifyParseException(provider, "Invalid JSON", cause)
                    else VerifyParseException(provider, "Invalid JSON", it)
                }
                else -> VerifyUnexpectedException(provider, it)
            }
        }

