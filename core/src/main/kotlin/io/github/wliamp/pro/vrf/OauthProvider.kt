package io.github.wliamp.pro.vrf

import io.github.wliamp.pro.vrf.Oauth.*

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
