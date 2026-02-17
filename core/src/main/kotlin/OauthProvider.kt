package io.github.wliamp.kit.id.core

import io.github.wliamp.kit.id.core.Oauth.*

class OauthProvider(
    val facebook: IOauth?,
    val google: IOauth?,
    val zalo: IOauth?
) {
    fun of(oauth: Oauth): IOauth? =
        when (oauth) {
            FACEBOOK -> facebook
            GOOGLE -> google
            ZALO -> zalo
        }
}
