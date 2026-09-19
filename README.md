# Kumaru - Android Personal AI Assistant (V0.2A)

Kumaru is a personal AI assistant built natively for Android using Kotlin and Jetpack Compose.

## Current Version: V0.2A — Real AI Brain

In V0.2A, Kumaru is upgraded from the simulated development mock to a **real AI reasoning brain powered by Google Gemini 2.5 Flash Lite**.

### Key Features in V0.2A:
- **Google Gemini 2.5 Flash Lite Integration**: Real-time generative reasoning via asynchronous REST requests.
- **Session Conversation Context**: Retains conversational memory across multi-turn exchanges during the active session.
- **Kumaru Personality Engine**: Calm, concise, intelligent, slightly witty, understanding casual English and Tamil/Thanglish expressions (e.g., *"Enna Kumaru"*, *"Sollunga"*).
- **Strict Capability Honesty**: Explicitly states when capabilities (like device actions, alarms, phone calls) are not yet implemented.
- **Secure Secret Architecture**: API key is injected at build time from `local.properties` (never hardcoded in Kotlin code, never committed to Git, never logged).
- **Graceful Error Recovery**: Handles offline state, timeouts, rate limits, and invalid keys cleanly without crashing.
- **Development Fallback**: Seamless switching between `GeminiAiProvider` and `MockAiProvider`.

---

## Project Structure
- **Package**: `com.kumaru.assistant`
- **Minimum SDK**: 26 (Android 8.0 Oreo)
- **Target / Compile SDK**: 34 (Android 14)
- **Architecture**: Clean MVVM with modular domains (AI, Voice, Tools, Memory)

---

## API Key Configuration

To connect Kumaru to your Gemini API key:

1. Open `local.properties` in the project root directory (this file is ignored by Git).
2. Add your Gemini API key entry:
   ```properties
   GEMINI_API_KEY=your_actual_gemini_api_key_here
   ```
   *(Optional) You can also configure a custom Gemini model if desired:*
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   gemini.model=gemini-3.5-flash-lite
   ```
3. Save the file. Gradle will automatically inject the key into `BuildConfig.GEMINI_API_KEY` during compilation.

---

## How to Switch Back to MockAiProvider

If you want to test the UI or offline workflows without making live Gemini API calls:

- **Option A (Clear Key)**: Leave `GEMINI_API_KEY` empty or absent in `local.properties`. `AiProviderFactory` will automatically fall back to `MockAiProvider`.
- **Option B (Code Configuration)**: In `AssistantViewModel`, pass `forceMock = true` to `AiProviderFactory.create(memoryStore, forceMock = true)`.

---

## Building and Running

### Build via PowerShell / Terminal:
```powershell
# From the project root:
cd C:\Users\hp\Desktop\Kumaru

# Clean and build debug APK:
.\gradlew.bat assembleDebug
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Install and Launch on Pixel 8 Emulator / Device:
```powershell
# Install on connected device/emulator:
.\gradlew.bat installDebug

# Launch the app:
adb shell am start -n com.kumaru.assistant/.MainActivity
```
*(Or open the project in Android Studio and press **Run 'app'** (`Shift + F10` / Play button).)*

---

## Sample Test Questions for V0.2A

1. **Identity & Capabilities**:
   - *"Who are you and what can you do?"*
2. **Multi-turn Context Awareness**:
   - Query 1: *"What is the capital of Japan?"*
   - Query 2: *"How far is it from Bangalore?"* (Verify Kumaru understands "it" refers to Tokyo).
3. **Tamil / Thanglish & Tool Boundaries**:
   - *"Enna Kumaru, can you set an alarm for 7 AM?"* (Verify friendly Thanglish acknowledgment and honest statement that phone alarm control is coming in a future version).

