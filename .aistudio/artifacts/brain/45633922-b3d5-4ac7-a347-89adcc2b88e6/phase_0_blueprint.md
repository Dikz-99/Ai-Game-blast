# AI Game Blast — Blueprint Arsitektur & Rencana Pelaksanaan (Phase 0)

## 1. Ringkasan Eksekutif & Visi Produk
**AI Game Blast** adalah asisten otomatisasi permainan berbasis AI cerdas (Universal AI Game Automation Assistant) untuk platform Android Native. Sistem ini mengadopsi pola **Universal AI Core** yang dipadukan dengan arsitektur **Game Adapter** yang dapat diperluas untuk aneka genre game (Turn-Based RPG, Idle Clicker, Match-3 Puzzle, hingga Custom Profile).

### Karakteristik Utama:
- **Zero Exploit & High Safety**: Menggunakan mekanisme resmi Android API (`MediaProjection` untuk capture layar, `AccessibilityService` untuk `dispatchGesture`, dan `ForegroundService` dengan notifikasi aktif). Bebas dari root exploit, memory tampering, atau anti-cheat bypass.
- **Hybrid AI Engine**: Pemrosesan lokal instan (ML Kit OCR, Computer Vision, Rule & State Machine, Safety Validator) dipadukan dengan Cloud AI opsional (Firebase AI Logic / Gemini) untuk penalaran strategi tingkat tinggi.
- **Combined Learning**: Universal Adaptive Learning (pola visual & feedback hasil) + Game Profile Learning (pemetaan koordinat, kontrol, dan taktik per game).
- **Futuristic Gaming UI**: Antarmuka futuristik berbasis Jetpack Compose dengan estetika gelap (Cyber Black `#0D0E15`), aksen Neon Crimson (`#FF2A55`), Cyan Circuit (`#00E5FF`), subtle glow, dan Floating Assistant overlay.

---

## 2. Struktur Modul & Paket (Clean Architecture + MVVM)

Aplikasi distrukturkan secara modular untuk memastikan pemisahan tanggung jawab (*Separation of Concerns*):

```
com.aistudio.aigameblast
│
├── core/                           # Komponen umum & utilitas sistem
│   ├── common/                     # Result, DispatcherProvider, Loggers
│   ├── model/                      # GameState, ActionType, Point, RectArea
│   └── constants/                  # AppConstants, ActionCooldowns
│
├── data/                           # Data layer & persistensi
│   ├── local/
│   │   ├── database/               # AppDatabase (Room)
│   │   ├── entity/                 # GameProfileEntity, GameSessionEntity,
│   │   │                           # GameObservationEntity, ActionHistoryEntity,
│   │   │                           # StrategyMemoryEntity, UserSettingsEntity
│   │   └── dao/                    # DAOs untuk semua entitas di atas
│   ├── datastore/                  # UserPreferencesDataStore
│   └── repository/                 # Implementasi repository (GameProfileRepository, dsb.)
│
├── domain/                         # Business logic & use cases murni
│   ├── model/                      # Domain models (GameProfile, Session, Strategy)
│   ├── repository/                 # Domain repository interfaces
│   └── usecase/                    # StartAutomationUseCase, StopAutomationUseCase,
│                                   # EvaluateActionOutcomeUseCase, SaveProfileUseCase
│
├── capture/                        # Sub-sistem MediaProjection (Screen Capture)
│   ├── service/                    # ScreenCaptureService (Foreground Service)
│   ├── engine/                     # MediaProjectionManager, VirtualDisplayHandler
│   └── pipeline/                   # ImageReaderProcessor, FrameBufferProvider
│
├── vision/                         # Computer Vision & OCR
│   ├── ocr/                        # Google ML Kit Text Recognition Engine
│   ├── cv/                         # ColorDetector, TemplateMatcher, ContourAnalyzer
│   ├── objectdetection/           # BoundingBoxTracker, GameElementClassifier
│   └── analyzer/                   # UnifiedFrameAnalyzer -> GameObservation
│
├── decision/                       # AI Decision Engine
│   ├── rule/                       # RuleEngine, HeuristicEvaluator
│   ├── state/                      # GameStateMachine, ConditionEvaluator
│   └── planner/                    # ActionPlanner, StrategySelector
│
├── automation/                     # Pengendalian Gesture & Accessibility
│   ├── service/                    # GameAutomationAccessibilityService
│   ├── dispatcher/                 # GestureDispatcher (Tap, Swipe, Multi-point, Drag)
│   └── path/                       # HumanizedPathGenerator (Jitter, Curve, Variable Speed)
│
├── safety/                         # Safety & Control Guardrails (Supreme Priority)
│   ├── controller/                 # SafetyController (Circuit Breaker)
│   ├── validator/                  # ActionValidator (Bounds, Rate-Limiter, Cooldown)
│   └── emergency/                  # EmergencyStopListener (Volume keys, floating kill-switch)
│
├── adapter/                        # Sistem Game Adapter
│   ├── core/                       # IGameAdapter interface, AdapterRegistry
│   └── presets/                    # TurnBasedRpgAdapter, IdleClickerAdapter,
│                                   # MatchPuzzleAdapter, CustomUserAdapter
│
├── learning/                       # Combined Learning & Evaluation
│   ├── evaluator/                  # ActionOutcomeEvaluator (Reward/Penalty metric)
│   ├── memory/                     # StrategyMemoryManager (Session experience)
│   └── optimizer/                  # StrategyOptimizer
│
├── cloud/                          # Cloud Gateway (Opsional & Opt-in)
│   ├── ai/                         # GeminiStrategyAdvisor (Firebase AI Logic)
│   └── sync/                       # ProfileCloudBackupManager
│
└── ui/                             # Jetpack Compose UI
    ├── theme/                      # Color, Theme, Typography, Shapes
    ├── components/                 # GlowingCard, NeonButton, StatusBadge, TopBar
    ├── screens/
    │   ├── splash/                 # SplashScreen dengan animasi inisialisasi AI
    │   ├── dashboard/              # Dashboard utama (status AI, quick start, telemetry)
    │   ├── gamemanager/            # Game list, profile picker
    │   ├── addgame/                # Form pembuatan profil game & wizard kalibrasi
    │   ├── aimonitor/              # Real-time visual overlay stream & telemetry log
    │   ├── floating/               # Floating Assistant Controller (Overlay Window)
    │   ├── history/                # Session logs, visual timeline, action history
    │   ├── settings/               # Safety limiters, AI sensitivity, cloud opt-in
    │   └── profiledetail/          # Edit aturan, target ROI, aksi custom
    └── viewmodel/                  # DashboardViewModel, MonitorViewModel, dsb.
```

---

## 3. Konfigurasi SDK & Gradle

- **Compile SDK**: `36` (Android 16 release 36, minor 1)
- **Target SDK**: `36`
- **Min SDK**: `26` (Android 8.0 Oreo) — Memastikan ketersediaan penuh `AccessibilityService.dispatchGesture`, Notification Channels untuk foreground service, dan API `ImageReader` & `HardwareBuffer`.
- **Java / Kotlin Target**: Java 11 / Kotlin 2.2.10
- **Jetpack Compose Compiler**: Built-in Kotlin 2.2.10 Compose plugin
- **KSP Version**: `2.3.5` (sejalan dengan Kotlin 2.2.10)

---

## 4. Matriks Dependency

1. **Jetpack Compose & Material 3**:
   - `androidx.compose.material3`, `material-icons-extended`
   - `androidx.lifecycle.runtime.compose`, `lifecycle-viewmodel-compose`
2. **Database & Preferensi**:
   - Room Database: `androidx.room:room-runtime:2.7.0`, `room-ktx:2.7.0`, `room-compiler:2.7.0` (KSP)
   - DataStore: `androidx.datastore:datastore-preferences:1.1.7`
3. **Computer Vision & OCR**:
   - ML Kit Text Recognition: `com.google.android.gms:play-services-mlkit-text-recognition:19.0.1`
   - On-device lightweight image processing & color/template analysis
4. **Networking & Cloud AI (Opt-in)**:
   - Moshi & Retrofit untuk REST
   - Firebase BoM & `firebase-ai` (Firebase AI Logic / Gemini API)
5. **Testing**:
   - Robolectric 4.16.1, JUnit 4, Coroutines Test, Compose UI Test

---

## 5. Aliran Data & Interaksi Subsistem

```
[Game Screen Display]
         │
         ▼ (MediaProjection API)
[ScreenCaptureService + ImageReader]
         │
         ▼ (Bitmap Frame throttled @ 5-15 FPS)
[VisionEngine]
   ├── ML Kit OCR (Ekstraksi Teks / Angka HP, Skor, Tombol)
   ├── Color / Contour Matcher (Deteksi Warna Indikator)
   └── Template / Object Detector (Ikon Tombol, Karakter)
         │
         ▼ (GameObservation)
[GameAdapter]
   └── Menginterpretasi state spesifik game (LOBBY, IN_BATTLE, VICTORY, DEFEAT)
         │
         ▼ (Normalized Game State)
[AI DecisionEngine]
   ├── Rule Engine & Heuristic State Machine
   └── Strategy Selector (Memilih GameAction: TAP / SWIPE / WAIT)
         │
         ▼ (Proposed GameAction)
[SafetyEngine (Supreme Priority Guardrail)]
   ├── Rate Limiter (Max N action/sec)
   ├── Emergency Stop State Check
   ├── Safe Screen Boundary Validator
   └── Humanized Cooldown Enforcer
         │
         ▼ (Validated GameAction)
[AutomationEngine (AccessibilityService)]
   └── dispatchGesture(GestureDescription) -> [Game Screen Input]
         │
         ▼ (Action Result)
[LearningEngine] ──► [Room DB: StrategyMemory & ActionHistory]
```

---

## 6. Rencana Tahapan Eksekusi (10 Fase)

- **PHASE 0 (Saat Ini)**: Project Preparation & Persetujuan Spesifikasi
- **PHASE 1**: Android Foundation, Clean Architecture, Theme Futuristik, Room DB Entity & DAO, Permission Manager, Dashboard Compose UI
- **PHASE 2**: Screen Capture Module (MediaProjection, Foreground Service, VirtualDisplay, Frame throttling)
- **PHASE 3**: Vision Engine (ML Kit OCR, Color/Template Analyzer, GameObservation builder)
- **PHASE 4**: Automation Engine (AccessibilityService, GestureDescription generator, Humanized Path generator)
- **PHASE 5**: Safety Engine & Decision Engine (Circuit breaker, rate limiter, emergency kill switch, heuristic rule evaluator)
- **PHASE 6**: Game Adapter System (Turn-based RPG, Idle Clicker, Match-3, Custom profile editor)
- **PHASE 7**: Hybrid AI & Cloud Gateway (Firebase AI Logic / Gemini integration untuk strategy optimization)
- **PHASE 8**: Learning Engine & History Analytics (Feedback loop, session recording, strategy adjustment)
- **PHASE 9**: Comprehensive Testing (Robolectric CUJ tests), Performance profiling, Final APK verification

---

## 7. Analisis Risiko Teknis & Mitigasi

1. **Foreground Service Type & MediaProjection di Android 14+ / 16**:
   - *Risiko*: Android mewajibkan `foregroundServiceType="mediaProjection"` dan inisiasi servis sebelum meminta token proyeksi.
   - *Mitigasi*: Deklarasikan tipe layanan secara presisi di `AndroidManifest.xml` dan ikuti siklus resmi Intent `createScreenCaptureIntent()`.
2. **Konsumsi CPU/Baterai saat OCR & Analisis Frame**:
   - *Risiko*: Menjalankan OCR pada resolusi penuh 60 FPS akan menyebabkan bottleneck dan throttling CPU.
   - *Mitigasi*: Adaptive sampling rate (2–5 FPS saat idle, dinaikkan saat event aktif), Region-of-Interest (ROI) cropping sehingga OCR hanya membaca zona teks relevan.
3. **Emergency Stop & Deteksi Pembatalan Izin**:
   - *Risiko*: Otomatisasi berjalan tak terkendali saat pengguna berpindah aplikasi.
   - *Mitigasi*: Floating Emergency Stop button yang selalu visible di atas layar, sensor tombol volume fisik untuk instant abort, dan deteksi otomatis saat target game kehilangan fokus.
4. **Kebijakan Google Play & Anti-Cheat Compliance**:
   - *Risiko*: Terdeteksi sebagai exploit atau bot ilegal.
   - *Mitigasi*: Tidak ada injeksi memori atau hooking file binary game; murni pengenalan visual eksternal dan simulasi sentuhan yang diizinkan sistem Android. Desain menyertakan variasi sentuhan manusia (*micro-jitter* dan durasi dinamis).

---

## 8. Strategi Pengujian

- **JVM Robolectric Tests**: Menguji logika `DecisionEngine`, algoritma `SafetyController`, validasi `GameAdapter`, serta operasi CRUD Room Database secara independen tanpa bergantung emulator hardware.
- **Mock Frame Injection Test**: Pipeline visi diuji menggunakan Bitmap sintetis untuk memverifikasi akurasi OCR dan deteksi batas elemen game.
- **Safety Boundary Stress Test**: Menguji skenario overflow frekuensi aksi untuk menjamin *Rate Limiter* memblokir eksekusi di atas batas aman.
