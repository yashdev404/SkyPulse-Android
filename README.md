# SkyPulse: Cosmic Ecosystem Prototype 🌌

SkyPulse is a high-fidelity front-end prototype for a space-centric social and utility ecosystem. It demonstrates advanced Android development patterns, focusing on modular UI design, reactive state management, and immersive user experiences.

## 🚀 Technical Highlights

This project was built to showcase modern Android engineering standards:

*   **Jetpack Compose:** The entire UI is built declaratively using Material 3 guidelines, moving away from legacy XML layouts.
*   **MVVM Architecture:** Business logic is decoupled from the UI. The app utilizes `ViewModel` and `StateFlow` to ensure the UI reacts predictably to state changes (Loading, Success, Error).
*   **Advanced Navigation:** Implemented a custom nested-scroll dynamic bottom navigation bar ("Floating Pill") to maximize screen real estate for content-heavy feeds.
*   **Polymorphic UI:** The `HomeFeedScreen` uses `LazyColumn` to seamlessly render different data types (Live Streams, Videos, Articles) within a unified feed structure.
*   **Secure Configuration:** Sensitive credentials (API keys for external data) are secured using Gradle `BuildConfig` and `local.properties`, ensuring they are kept out of version control.
*   **Robust Error Handling:** Designed custom thematic empty/error states (e.g., "Barren Mars" connection loss) to maintain user immersion even during network failures.

## 📱 Modules Built (Phase 1)

1.  **Cosmic Feed:** A scrolling content hub featuring async image loading (via Coil) and dynamic card layouts.
2.  **Mission Control (Rooms):** A complex, nested hierarchy UI inspired by Discord, designed for future real-time public/private chat integration.
3.  **Stargazer:** A live astronomical utility (currently hooked into OpenWeather) featuring custom data-driven views and event placeholders.
4.  **Exquisite Space Shop:** An e-commerce grid utilizing `LazyVerticalGrid`, demonstrating standard retail UX patterns (strikethrough pricing, ratings, 'Add to Cart' hierarchy).
5.  **Explorer Profile:** A YouTube-style profile layout with horizontal scrolling tabs and an interactive "Empty State" that uses State Recomposition to simulate data loading.

## 🛠️ Tech Stack
*   **Language:** Kotlin
*   **UI Toolkit:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Concurrency:** Kotlin Coroutines & Flows (`StateFlow`)
*   **Networking:** Retrofit & Gson
*   **Image Loading:** Coil
*   **Animations:** Compose Animations & Lottie (Prepared)

## 🔜 Phase 2 Roadmap
*   Migration of local mock data to a custom backend / Firebase Firestore.
*   Implementation of the Repository pattern for sophisticated data caching.
*   Integration of WebSockets for real-time "Mission Control" chat functionality.
