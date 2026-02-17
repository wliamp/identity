package io.github.wliamp.kit.id.core

import io.github.wliamp.kit.id.core.Otp.*

class OtpProvider(
    val firebase: IOtp?
) {
    fun of(otp: Otp): IOtp? =
        when (otp) {
            FIREBASE -> firebase
        }
}
