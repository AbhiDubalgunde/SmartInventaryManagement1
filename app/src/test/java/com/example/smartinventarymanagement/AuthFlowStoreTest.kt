package com.example.smartinventarymanagement

import org.junit.Assert.*
import org.junit.Test

class AuthFlowStoreTest {
    @Test fun pendingRequestCannotResetPassword() {
        val store = AuthFlowStore { 0 }
        val request = store.begin(1, "+919876543210", "User", AuthPurpose.PASSWORD_RESET)
        assertNull(store.consume(request.id, AuthPurpose.PASSWORD_RESET))
    }
    @Test fun proofMustMatchPhone() {
        val store = AuthFlowStore { 0 }
        val request = store.begin(1, "+919876543210", "User", AuthPurpose.PASSWORD_RESET)
        assertNull(store.verified(request.id, "+919876543211"))
        assertNull(store.consume(request.id, AuthPurpose.PASSWORD_RESET))
    }
    @Test fun resetProofIsSingleUseAndCannotLogin() {
        val store = AuthFlowStore { 0 }
        val request = store.begin(1, "+919876543210", "User", AuthPurpose.PASSWORD_RESET)
        assertNotNull(store.verified(request.id, request.phone))
        assertNull(store.consume(request.id, AuthPurpose.LOGIN))
        assertNotNull(store.consume(request.id, AuthPurpose.PASSWORD_RESET))
        assertNull(store.consume(request.id, AuthPurpose.PASSWORD_RESET))
    }
    @Test fun loginProofCannotResetPassword() {
        val store = AuthFlowStore { 0 }
        val request = store.begin(1, "+919876543210", "User", AuthPurpose.LOGIN)
        store.verified(request.id, request.phone)
        assertNull(store.consume(request.id, AuthPurpose.PASSWORD_RESET))
        assertNotNull(store.consume(request.id, AuthPurpose.LOGIN))
    }
    @Test fun grantsExpireAndCannotBeExtendedByReplayingVerification() {
        var time = 0L
        val store = AuthFlowStore { time }
        val request = store.begin(1, "+919876543210", "User", AuthPurpose.PASSWORD_RESET)
        store.verified(request.id, request.phone)
        time = 299_999
        assertNull(store.verified(request.id, request.phone))
        time = 300_000
        assertNull(store.consume(request.id, AuthPurpose.PASSWORD_RESET))
    }
    @Test fun startingAnotherAccountInvalidatesPreviousRequest() {
        val store = AuthFlowStore { 0 }
        val first = store.begin(1, "+919876543210", "First", AuthPurpose.LOGIN)
        val second = store.begin(2, "+919876543211", "Second", AuthPurpose.LOGIN)
        assertNull(store.verified(first.id, first.phone))
        assertNotNull(store.get(second.id))
    }
    @Test fun processRestartDoesNotRestoreAuthorization() {
        val first = AuthFlowStore { 0 }
        val request = first.begin(1, "+919876543210", "User", AuthPurpose.PASSWORD_RESET)
        first.verified(request.id, request.phone)
        assertNull(AuthFlowStore { 0 }.consume(request.id, AuthPurpose.PASSWORD_RESET))
    }
    @Test fun cancelledAndExpiredChallengesAreRejected() {
        var time = 0L
        val store = AuthFlowStore { time }
        val cancelled = store.begin(1, "+919876543210", "User", AuthPurpose.LOGIN)
        store.cancel(cancelled.id)
        assertNull(store.verified(cancelled.id, cancelled.phone))
        val expired = store.begin(1, "+919876543210", "User", AuthPurpose.LOGIN)
        time = 600_000
        assertNull(store.verified(expired.id, expired.phone))
    }
    @Test fun validatesInternationalPhoneAndResetPasswords() {
        assertTrue(AuthValidation.isPhoneValid("+919876543210"))
        listOf("", "9876543210", "+0123456789", "+91 9876543210", "+1234567890123456").forEach {
            assertFalse(AuthValidation.isPhoneValid(it))
        }
        assertTrue(AuthValidation.isPasswordValid("Inventory2026"))
        listOf("abc123", "abcdefgh", "12345678", " Inventory2026", "Inventory2026 ").forEach {
            assertFalse(AuthValidation.isPasswordValid(it))
        }
    }
}
