package io.github.wliamp.kit.id.core

class OtpProvider(
    val firebase: IOtp?
) {
    fun of(otp: Otp): IOtp? =
        when (otp) {
            Otp.FIREBASE -> firebase
        }
}
