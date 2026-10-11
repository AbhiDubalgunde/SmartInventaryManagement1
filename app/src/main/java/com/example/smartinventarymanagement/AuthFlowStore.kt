package com.example.smartinventarymanagement

import java.util.UUID

enum class AuthPurpose { LOGIN, PASSWORD_RESET }

data class PendingAuth(
    val id: String,
    val userId: Int,
    val phone: String,
    val fullName: String,
    val purpose: AuthPurpose,
    val expiresAt: Long,
    val verified: Boolean = false
)

/** In-memory authorizations: never persist passwords, SMS codes or reset grants.
 * Process death requires a fresh login/recovery. A Firebase currentUser is not proof.
 */
class AuthFlowStore(private val now: () -> Long = { System.nanoTime() / 1_000_000 }) {
    private var pending: PendingAuth? = null

    @Synchronized fun begin(userId: Int, phone: String, name: String, purpose: AuthPurpose): PendingAuth {
        return PendingAuth(UUID.randomUUID().toString(), userId, phone, name, purpose,
            now() + 10 * 60_000).also { pending = it }
    }

    @Synchronized fun get(id: String?): PendingAuth? {
        val current = pending ?: return null
        if (now() >= current.expiresAt) { pending = null; return null }
        return current.takeIf { it.id == id }
    }

    @Synchronized fun verified(id: String?, verifiedPhone: String?): PendingAuth? {
        val current = get(id) ?: return null
        if (current.verified || current.phone != verifiedPhone) return null
        return current.copy(verified = true, expiresAt = now() + 5 * 60_000).also { pending = it }
    }

    @Synchronized fun consume(id: String?, purpose: AuthPurpose): PendingAuth? {
        val current = get(id)?.takeIf { it.verified && it.purpose == purpose } ?: return null
        pending = null
        return current
    }

    @Synchronized fun cancel(id: String?) {
        if (pending?.id == id) pending = null
    }
}

object AuthFlows { val store = AuthFlowStore() }

object AuthValidation {
    fun isPhoneValid(phone: String) = phone.matches(Regex("^\\+[1-9][0-9]{1,14}$"))
    fun isPasswordValid(password: String) =
        password.length in 8..128 && password == password.trim() &&
            password.any { it.isLetter() } && password.any { it.isDigit() }
}
