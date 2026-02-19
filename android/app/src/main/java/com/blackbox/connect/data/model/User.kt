package com.blackbox.connect.data.model

import java.util.Date

data class User(
    val uid: String = "",
    val emailHash: String = "",
    val deviceKey: String = "",
    val status: UserStatus = UserStatus.OFFLINE,
    val lastActive: Date? = null,
    val createdAt: Date? = null,
    val fcmToken: String? = null
)

enum class UserStatus {
    ONLINE, OFFLINE, AWAY, BUSY
}

data class PreKeyBundle(
    val identityKey: String = "",
    val signedPreKey: SignedPreKey = SignedPreKey(),
    val oneTimePreKeys: List<OneTimePreKey> = emptyList()
)

data class SignedPreKey(
    val keyId: Int = 0,
    val publicKey: String = "",
    val signature: String = ""
)

data class OneTimePreKey(
    val keyId: Int = 0,
    val publicKey: String = ""
)
