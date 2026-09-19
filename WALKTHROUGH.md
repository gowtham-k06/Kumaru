# Walkthrough - Kumaru V0.2A (Real Gemini AI Brain)

Successfully implemented **Kumaru V0.2A**, upgrading Kumaru with a real AI brain powered by **Google Gemini 2.5 Flash Lite** while keeping the existing UI, push-to-talk state machine, and session memory intact.

---

## 1. What was Implemented

### 1.1 Secure API Key & Gradle Configuration
- Updated [app/build.gradle.kts](file:///c:/Users/hp/Desktop/Kumaru/app/build.gradle.kts):
  - Enabled `buildFeatures { buildConfig = true }`.
  - Configured build script to dynamically read `GEMINI_API_KEY` (and `gemini.model`) from `local.properties` (or environment variables).
  - Injected `BuildConfig.GEMINI_API_KEY` and `BuildConfig.GEMINI_MODEL` (`gemini-2.5-flash-lite`).
  - Guaranteed security: `local.properties` is listed in `.gitignore` and excluded from version control. No secret keys exist in any Kotlin source code.

### 1.2 Gemini AI Provider ([GeminiAiProvider.kt](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/data/ai/GeminiAiProvider.kt))
- Implements [AiProvider](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/domain/ai/AiProvider.kt).
- **Asynchronous Network Pipeline**:
  - Uses `HttpURLConnection` on `Dispatchers.IO` with zero bloated dependencies.
  - Generates JSON payload for Google Gemini REST API (`generateContent`).
- **Session Memory Integration**:
  - Injects [MemoryStore](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/domain/memory/MemoryStore.kt) to retrieve the recent turns (`user` and `model` roles) and maintains full conversational context during the active session.
- **Kumaru Personality & System Instructions**:
  - Calm, concise, intelligent, conversational tone tailored for spoken conversation.
  - Understands casual English and Tamil / Thanglish colloquialisms (*"Enna Kumaru"*, *"Sollunga"*, *"Vanakkam"*, *"Nandri"*).
  - Strict honesty on tool capabilities: politely clarifies that device tools/alarms are coming in future stages.
- **Robust Error Handling**:
  - Catches offline states (`UnknownHostException`), timeouts (`SocketTimeoutException`), invalid/expired API keys (HTTP 400/401/403), rate limits (HTTP 429), and safety filtering (`finishReason: SAFETY`) without crashing.

### 1.3 Provider Selection Mechanism ([AiProviderFactory.kt](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/data/ai/AiProviderFactory.kt))
- Dynamically selects [GeminiAiProvider](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/data/ai/GeminiAiProvider.kt) when `GEMINI_API_KEY` is present.
- Seamlessly falls back to [MockAiProvider](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/data/ai/MockAiProvider.kt) when the key is missing or when mock mode is forced.

### 1.4 AssistantViewModel Integration ([AssistantViewModel.kt](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/presentation/viewmodel/AssistantViewModel.kt))
- Connected `aiProvider` to `AiProviderFactory.create(memoryStore)`.
- Preserved the existing UI state machine: `IDLE` -> `THINKING` -> `SPEAKING` -> `IDLE`.

---

## 2. File Change Summary

| File | Status | Description |
|---|---|---|
| [`app/build.gradle.kts`](file:///c:/Users/hp/Desktop/Kumaru/app/build.gradle.kts) | Modified | Enabled BuildConfig, injected `GEMINI_API_KEY` & `GEMINI_MODEL` from `local.properties` |
| [`app/.../data/ai/GeminiAiProvider.kt`](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/data/ai/GeminiAiProvider.kt) | Created | Google Gemini 2.5 Flash Lite provider with session context & error handling |
| [`app/.../data/ai/AiProviderFactory.kt`](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/data/ai/AiProviderFactory.kt) | Created | Provider selector between Gemini and Mock fallback |
| [`app/.../presentation/viewmodel/AssistantViewModel.kt`](file:///c:/Users/hp/Desktop/Kumaru/app/src/main/java/com/kumaru/assistant/presentation/viewmodel/AssistantViewModel.kt) | Modified | Injected factory-created AI provider with session memory |
| [`README.md`](file:///c:/Users/hp/Desktop/Kumaru/README.md) | Modified | Added V0.2A features, API key config, build/run steps, and mock toggle |
| [`IMPLEMENTATION_PLAN.md`](file:///c:/Users/hp/Desktop/Kumaru/IMPLEMENTATION_PLAN.md) | Modified | Updated plan for V0.2A |

---

## 3. How to Configure the API Key

1. Open `local.properties` at the root of `Kumaru`.
2. Add your Gemini API key:
   ```properties
   GEMINI_API_KEY=YOUR_GEMINI_API_KEY_VALUE
   ```
3. Save the file.

---

## 4. How to Build & Run in PowerShell

```powershell
cd C:\Users\hp\Desktop\Kumaru
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
adb shell am start -n com.kumaru.assistant/.MainActivity
```
*(Or press Run in Android Studio).*

---

## 5. Three Suggested Test Questions

1. **Identity & Scope**:
   > *"Who are you and what can you do?"*
2. **Multi-turn Contextual Memory**:
   > Turn 1: *"What is the capital of Japan?"*
   > Turn 2: *"How far is it from Bangalore?"*
3. **Tamil/Thanglish & Tool Boundaries**:
   > *"Enna Kumaru, can you turn on my flashlight?"*

---

## Project Directory Structure

The project has been scaffolded as a native Android Kotlin application with a clean, extensible MVVM architecture:

```
c:\Users\hp\Desktop\Kumaru/
├── settings.gradle.kts                   # Project configuration & Maven/Google repository setup
├── build.gradle.kts                      # Root Gradle script (AGP 8.5.1, Kotlin 2.0.0, Compose Plugin)
├── gradle.properties                     # JVM memory & AndroidX flags
├── gradlew.bat                           # Gradle wrapper batch script for Windows
├── gradle/
│   ├── libs.versions.toml                # Version catalog with Compose BOM, Lifecycle, Coroutines
│   └── wrapper/
│       └── gradle-wrapper.properties     # Gradle 8.7 distribution
├── app/
│   ├── build.gradle.kts                  # Android app module config (compileSdk 34, minSdk 26, Java 17)
│   ├── proguard-rules.pro                # ProGuard rules
│   └── src/main/
│       ├── AndroidManifest.xml           # Permissions (INTERNET, RECORD_AUDIO), Application & Activity
│       ├── res/values/
│       │   ├── strings.xml               # UI text resources
│       │   ├── colors.xml                # Color tokens
│       │   └── themes.xml                # Edge-to-edge dark theme
│       └── java/com/kumaru/assistant/
│           ├── KumaruApplication.kt      # Application entry
│           ├── MainActivity.kt           # Edge-to-edge ComponentActivity
│           ├── core/
│           │   ├── state/
│           │   │   └── AssistantState.kt # IDLE, LISTENING, THINKING, SPEAKING, ERROR
│           │   └── model/
│           │       └── ConversationMessage.kt # Message data model (role, text, timestamp)
│           ├── domain/
│           │   ├── ai/
│           │   │   └── AiProvider.kt     # AI generation contract
│           │   ├── voice/
│           │   │   ├── VoiceInput.kt     # Speech-to-text contract
│           │   │   └── VoiceOutput.kt    # Speech synthesis contract
│           │   ├── tools/
│           │   │   ├── ToolExecutor.kt   # Extensible tool execution contract
│           │   │   └── Tool.kt           # Tool definitions (openApp, setAlarm, etc.)
│           │   └── memory/
│           │       └── MemoryStore.kt    # Contextual memory contract
│           ├── data/
│           │   ├── ai/
│           │   │   └── MockAiProvider.kt # Conversational mock for V0.1 (Gemini-ready for V0.2)
│           │   ├── voice/
│           │   │   ├── PushToTalkVoiceInput.kt # Push-to-talk coordinator
│           │   │   └── SystemVoiceOutput.kt    # Speech synthesis coordinator
│           │   ├── tools/
│           │   │   └── DefaultToolExecutor.kt  # Non-fake tool registry
│           │   └── memory/
│           │       └── InMemoryMemoryStore.kt  # Ephemeral context buffer
│           └── presentation/
│               ├── theme/
│               │   ├── Color.kt          # Obsidian void, neon cyan, cyber violet, amber
│               │   ├── Type.kt           # Modern sans-serif typography tokens
│               │   └── Theme.kt          # KumaruTheme composable
│               ├── components/
│               │   ├── GlowingOrb.kt     # Animated multi-layered Canvas AI orb
│               │   ├── AssistantHeader.kt# Brand title, version badge, live status chip, reset action
│               │   ├── TranscriptView.kt # Fluid non-boxy message stream with suggestions
│               │   └── TalkButton.kt     # Large tactile push-to-talk button with ambient glow
│               ├── viewmodel/
│               │   ├── AssistantUiState.kt    # UI state model
│               │   └── AssistantViewModel.kt  # State coordinator (IDLE -> LISTENING -> THINKING -> SPEAKING -> IDLE)
│               └── ui/
│                   └── AssistantScreen.kt     # Top-level screen composition
```

---

## 2. Key Architectural Features

### Core State Machine
The assistant reacts dynamically across 5 distinct states:
- `IDLE`: Assistant is dormant and listening for interaction. The orb breathes gently with cyan and violet gradients.
- `LISTENING`: Triggered via Push-to-Talk. The orb pulses rapidly and projects expanding acoustic shockwave rings.
- `THINKING`: User query is being reasoned over by `AiProvider`. The orb displays dual counter-rotating orbital rings with an amber-violet core.
- `SPEAKING`: Assistant delivers response via `VoiceOutput`. The orb pulses with vocal modulation waveforms.
- `ERROR`: Alert state displaying amber-crimson warning glow and allowing a retry.

### Future-Proof Abstractions (No Fake Logic)
- **`AiProvider`**: Simple contract (`suspend fun generateResponse(input: String): String`). `MockAiProvider` provides immediate responses in V0.1 so you can test without an API key, ready to be swapped with Google Gemini in V0.2.
- **`VoiceInput`**: Push-to-talk in V0.1, structured to seamlessly connect native speech recognition or the `"Enna Kumaru"` wake phrase in future phases.
- **`VoiceOutput`**: Contract for TTS playback.
- **`ToolExecutor`**: Registers future capabilities (`openApp`, `setAlarm`, `createReminder`, `makeCall`, `openBrowser`, `getWeather`, `searchWeb`). Per design instructions, it cleanly acknowledges the registered tool specifications without pretending real actions happened.
- **`MemoryStore`**: Thread-safe in-memory buffer in V0.1, ready for persistent Room / vector storage later.

---

## 3. How to Open, Build, and Launch Kumaru

### Option A: Via Android Studio (Recommended)
1. Open **Android Studio**.
2. Click **File > Open...** (or **Open** from the Welcome screen).
3. Navigate to and select: `C:\Users\hp\Desktop\Kumaru`.
4. Click **OK**.
5. Android Studio will automatically recognize the Gradle build files and sync the project dependencies.
6. Select an Android Emulator or a connected Android device (running Android 8.0 / API 26 or higher).
7. Click the **Run** button (green play icon `▶`) or press **Shift + F10**.

### Option B: Via Terminal / Command Line
If you want to build APKs directly from PowerShell or Command Prompt:

```powershell
cd C:\Users\hp\Desktop\Kumaru
.\gradlew.bat assembleDebug
```

The compiled APK will be located at:
`app\build\outputs\apk\debug\app-debug.apk`

To install directly to a connected device or running emulator:
```powershell
.\gradlew.bat installDebug
```

---

## 4. Antigravity IDE Terminal Runner Note
If you want Antigravity IDE to execute terminal commands internally, Windows requires permissions for the folder `C:\Users\hp\.gemini\antigravity-ide\bin`. 
You can resolve this anytime by creating the folder in PowerShell:
```powershell
New-Item -ItemType Directory -Path "C:\Users\hp\.gemini\antigravity-ide\bin" -Force
```
