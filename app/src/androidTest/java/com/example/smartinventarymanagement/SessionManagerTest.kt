package com.example.smartinventarymanagement

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SessionManagerTest {
    @Test fun accountSwitchAndResetCannotReuseAnotherAccountsBiometricOrTrust() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "session-test-${UUID.randomUUID()}"
        val isolated = object : ContextWrapper(target) {
            override fun getSharedPreferences(ignored: String, mode: Int): SharedPreferences =
                target.getSharedPreferences(name, mode)
        }
        try {
            val session = SessionManager(isolated)
            session.completeLogin("+15555550101", "First")
            session.trustDevice("+15555550101")
            session.setBiometricEnabled(true)
            session.completeLogin("+15555550102", "Second")
            assertFalse(session.isBiometricEnabled())
            assertTrue(session.isDeviceTrusted("+15555550101"))
            session.trustDevice("+15555550102")
            session.setBiometricEnabled(true)
            assertTrue(session.passwordWasReset("+15555550102"))
            assertFalse(session.isLoggedIn())
            assertFalse(session.isBiometricEnabled())
            assertFalse(session.isDeviceTrusted("+15555550102"))
            assertTrue(session.isDeviceTrusted("+15555550101"))
            assertFalse(isolated.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE).contains("savedPassword"))
        } finally {
            target.deleteSharedPreferences(name)
        }
    }
}
