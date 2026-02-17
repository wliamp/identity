package io.github.wliamp.kit.id.core

import reactor.core.publisher.Mono

interface IOauth {
    fun verify(token: String): Mono<Boolean>
    fun getInfo(token: String): Mono<Map<String, Any>>
}

interface IOtp {
    fun verify(code: String): Mono<Boolean>
    fun getInfo(code: String): Mono<Map<String, Any>>
}
