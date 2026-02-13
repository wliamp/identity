package io.github.wliamp.pro.vrf

import io.github.wliamp.pro.vrf.Otp.*

class OtpProvider(
    val firebase: IOtp?
) {
    fun of(otp: Otp): IOtp? =
        when (otp) {
            FIREBASE -> firebase
        }
}
