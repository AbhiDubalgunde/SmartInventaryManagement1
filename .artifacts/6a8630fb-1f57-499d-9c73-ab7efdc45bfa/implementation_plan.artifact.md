# Comprehensive Authentication & Profile pass

This plan addresses the removal of OTP from the authentication flow, fixes profile photo persistence issues, and enhances the profile image viewer.

## User Review Required

> [!IMPORTANT]
> - OTP/2FA is being bypassed entirely in `LoginActivity`.
> - OTP-related demo entry points in `HomeFragment` and `SettingsActivity` will be hidden.
> - Session timeout is being reduced from 10 minutes to 5 minutes.

## Proposed Changes

### [Authentication & Session]

#### [MODIFY] [LoginActivity.kt](file:///D:/AndroidStudioProjects/SmartInventaryManagement/app/src/main/java/com/example/smartinventarymanagement/LoginActivity.kt)
- Bypass the `isDeviceTrusted` check in the login listener.
- Always call `navigateToMain()` upon successful password verification.
- Add comments to the bypassed code for future re-enablement.

#### [MODIFY] [SessionManager.kt](file:///D:/AndroidStudioProjects/SmartInventaryManagement/app/src/main/java/com/example/smartinventarymanagement/SessionManager.kt)
- Update `SESSION_TIMEOUT` from `10 * 60 * 1000L` to `5 * 60 * 1000L` (5 minutes).

#### [MODIFY] [RegisterActivity.kt](file:///D:/AndroidStudioProjects/SmartInventaryManagement/app/src/main/java/com/example/smartinventarymanagement/RegisterActivity.kt)
- Rename `checkUserAndSendOtp` to `registerUser` for clarity (since it no longer sends OTP).

### [Profile Photo Fix]

#### [MODIFY] [AccountFragment.kt](file:///D:/AndroidStudioProjects/SmartInventaryManagement/app/src/main/java/com/example/smartinventarymanagement/AccountFragment.kt)
- Change the profile photo saving logic to use a unique filename (e.g., `profile_photo_<timestamp>.jpg`).
- Implement cleanup logic to delete the previous profile photo file when a new one is saved.
- This ensures Glide and other image loaders do not serve stale cached images.

### [UI Cleanup & Enhancements]

#### [MODIFY] [fragment_home.xml](file:///D:/AndroidStudioProjects/SmartInventaryManagement/app/src/main/res/layout/fragment_home.xml)
- Set `actionAdmin` (Admin Verification) card visibility to `gone`.

#### [MODIFY] [SettingsActivity.kt](file:///D:/AndroidStudioProjects/SmartInventaryManagement/app/src/main/java/com/example/smartinventarymanagement/SettingsActivity.kt)
- Hide the `btnSmsRetrieverDemo` layout to ensure no OTP demo flow is accessible.

#### [MODIFY] [ProfilePhotoActivity.kt](file:///D:/AndroidStudioProjects/SmartInventaryManagement/app/src/main/java/com/example/smartinventarymanagement/ProfilePhotoActivity.kt)
- Ensure the `PhotoView` is correctly configured for a smooth full-screen experience. (Existing implementation already uses `PhotoView` which supports pinch-to-zoom and double-tap).

## Verification Plan

### Automated Tests
- Run `gradlew assembleDebug` to ensure the project builds correctly.

### Manual Verification
1.  **Registration**: Register a new user and verify immediate success without OTP.
2.  **Login**: Log in with username/password and verify direct navigation to Dashboard.
3.  **Session Timeout**: Wait for 5 minutes of inactivity and verify redirection to `LoginActivity`.
4.  **Biometric**: Verify biometric prompt appears on app launch if enabled, and fall back to password if cancelled.
5.  **Profile Photo**: Change the profile photo multiple times and verify the UI updates immediately and correctly across all screens (`HomeFragment`, `AccountFragment`).
6.  **Image Viewer**: Open the profile photo and verify pinch-to-zoom and double-tap functionality.
