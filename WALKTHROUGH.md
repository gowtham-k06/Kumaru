# Walkthrough - Kumaru V0.2.1 (Manual Voice Control + Text Input + Performance Stabilization)

Successfully engineered **Kumaru V0.2.1**, addressing voice listening behavior, manual start/stop continuous voice sessions, 15-second inactivity timeout, shared message pipeline for voice and text input, glassmorphic text field, and canvas orb stability.

---

## 1. Summary of Changes

### 1.1 Voice Listening Behavior & Multi-Segment Pause Support
- **Files Modified**:
  - [`VoiceInput.kt`](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/domain/voice/VoiceInput.kt)
  - [`PushToTalkVoiceInput.kt`](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/data/voice/PushToTalkVoiceInput.kt)
- **New Behavior**:
  - Voice input is **manually started** (tap Voice button) and **manually stopped** (tap Stop button) OR automatically submitted when **15 seconds of inactivity** occurs.
  - Normal silence (2s, 5s, 10s) does **NOT** trigger submission to Gemini.
  - `onEndOfSpeech()` and SpeechRecognizer internal timeouts (`ERROR_NO_MATCH`, `ERROR_SPEECH_TIMEOUT`, `ERROR_CLIENT`) no longer terminate the user's session. The session remains in `LISTENING` state and automatically restarts recognition on the single managed `SpeechRecognizer` instance.
  - Multi-segment recognition text is accumulated in `accumulatedTranscript` across pauses.
  - If the user taps Stop without speaking, it returns to `IDLE` with `"No speech detected."` without calling Gemini.

### 1.2 15-Second Inactivity Timer
- **Files Modified**:
  - [`AssistantViewModel.kt`](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/presentation/viewmodel/AssistantViewModel.kt)
- **Implementation**:
  - Lifecycle-aware coroutine timer managed via `viewModelScope`.
  - Starts 15s timer when voice listening begins.
  - Resets to 15s whenever speech activity is detected (`onBeginningOfSpeech`, `onPartialResults`, `onResults`, or RMS audio activity above threshold).
  - When 15 seconds elapse with no activity: stops listening and submits the accumulated transcript (or resets to `IDLE` with `"No speech detected."` if empty).
  - Automatically cancels on manual stop, Gemini start, error, or ViewModel destruction.

### 1.3 Text Input & Shared Message Pipeline
- **Files Created / Modified**:
  - [`ChatInputField.kt`](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/presentation/components/ChatInputField.kt) [NEW]
  - [`AssistantScreen.kt`](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/presentation/ui/AssistantScreen.kt)
  - [`AssistantViewModel.kt`](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/presentation/viewmodel/AssistantViewModel.kt)
- **Features**:
  - Glassmorphic text input bar matching the futuristic Kumaru aesthetic.
  - Keyboard enter/send action (`ImeAction.Send`).
  - Empty text is prevented from submitting.
  - Typing and sending text does NOT require microphone permission or instantiate `SpeechRecognizer`.
  - Shared message pipeline `submitUserMessage(text)` connects both Voice, Text, and Suggestions to the same Gemini memory & vocal synthesis flow.
  - Prevents duplicate submissions while `THINKING` or `SPEAKING`.

### 1.4 Central Orb Stability & UI Performance
- **Files Modified**:
  - [`GlowingOrb.kt`](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/presentation/components/GlowingOrb.kt)
  - [`TalkButton.kt`](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/presentation/components/TalkButton.kt)
- **Fixes**:
  - Anchored orbital rings and rotating celestial nodes to a constant base radius (`baseRadius * 1.35f`).
  - Removed eccentric wobble/shaking caused by dynamic radial scaling while rotating.
  - Breathing pulse is applied smoothly to the inner glowing core and atmospheric aura only.
  - Layout dimensions remain strictly fixed to prevent jitter and excessive recompositions.

### 1.5 Version & Header
- **Files Modified**:
  - [`AssistantHeader.kt`](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/presentation/components/AssistantHeader.kt): Updated badge to `V0.2.1`.
  - [`app/build.gradle.kts`](file:///c:/Users/hp/Desktop/Kumaru/app/build.gradle.kts): Updated `versionCode = 3`, `versionName = "0.2.1"`.

---

## 2. Build & Deployment Commands

In PowerShell:

```powershell
# Set Gradle User Home and assemble debug APK
$env:GRADLE_USER_HOME="C:\GradleUserHome"
cd C:\Users\hp\Desktop\Kumaru
.\gradlew.bat assembleDebug

# Output APK path:
# app\build\outputs\apk\debug\app-debug.apk

# Detect connected ADB device
$ADB = "C:\Users\hp\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $ADB devices

# Uninstall existing and install V0.2.1
& $ADB uninstall com.kumaru.assistant
& $ADB install app\build\outputs\apk\debug\app-debug.apk

# Launch Kumaru V0.2.1
& $ADB shell am start -n com.kumaru.assistant/.MainActivity
```

---

## 3. Git Status & Commit Commands

```powershell
cd C:\Users\hp\Desktop\Kumaru

# Verify ignored files remain clean (local.properties, app/build/, .gradle/)
git status

# Stage changes
git add .

# Commit V0.2.1
git commit -m "Kumaru V0.2.1 - manual voice and text input"

# Push to configured remote
git push
```
