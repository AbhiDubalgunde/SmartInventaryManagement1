package com.example.smartinventarymanagement

import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreenTest {
    @Test fun loginPhoneIsEditableAndInvalidPhoneDoesNotProceed() {
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val phone = activity.findViewById<EditText>(R.id.etUsername)
                assertTrue(phone.isEnabled)
                phone.setText("123")
                activity.findViewById<View>(R.id.btnLogin).performClick()
                assertNotNull(phone.error)
                assertEquals(View.GONE, activity.findViewById<View>(R.id.authProgress).visibility)
            }
        }
    }
    @Test fun resetScreenRejectsIntentFlagWithoutVerifiedGrant() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<ResetPasswordActivity>(Intent(context, ResetPasswordActivity::class.java)
            .putExtra("verified", true).putExtra("phone", "+15555550101")).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.findViewById<View>(R.id.btnResetPassword).isEnabled)
                assertEquals(View.VISIBLE, activity.findViewById<TextView>(R.id.authError).visibility)
            }
        }
    }
    @Test fun otpScreenRejectsFirebaseFlowWithoutRoomAuthorization() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<OtpActivity>(Intent(context, OtpActivity::class.java)
            .putExtra("isFirebaseFlow", true).putExtra("is2faFlow", true)
            .putExtra("phone", "+15555550101")).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.findViewById<View>(R.id.btnVerifyOtp).isEnabled)
                assertFalse(activity.findViewById<View>(R.id.btnResendOtp).isEnabled)
            }
        }
    }
    @Test fun captureAuthLayoutsAndCheckRecreation() {
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->
            scenario.onActivity {
                it.findViewById<EditText>(R.id.etUsername).setText("")
                it.delegate.localNightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            }
            capture("login-light")
            scenario.recreate()
            scenario.onActivity {
                assertTrue(it.findViewById<EditText>(R.id.etUsername).isEnabled)
                it.findViewById<EditText>(R.id.etUsername).setText("")
                it.delegate.localNightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            }
            capture("login-dark")
        }
        ActivityScenario.launch(ForgotPasswordActivity::class.java).use {
            capture("forgot-password")
        }
        ActivityScenario.launch(RegisterActivity::class.java).use {
            capture("registration")
        }
    }

    private fun capture(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        Thread.sleep(400)
        val directory = java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "verification")
        directory.mkdirs()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        assertNotNull(bitmap)
        java.io.File(directory, "$name.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
