# 📱 SkyPulse Android Frontend Documentation

This document provides a detailed breakdown of the frontend architecture, technologies, use cases, and design justifications for the **SkyPulse** Android client application.

---

## 🏗️ Architecture Overview

The SkyPulse Android frontend is built following **Modern Android Development (MAD)** standards:
* **Architecture Pattern**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF).
* **UI Paradigm**: Fully declarative using **Jetpack Compose (Material 3)**.
* **Asynchronous Execution**: **Kotlin Coroutines** with **StateFlow** & **SharedFlow**.
* **Design Aesthetic**: Tactical Deep Space / Slate Onyx HUD theme with adaptive gradients and glassmorphism.

---

## 🛠️ Frontend Technology Stack & Justification

| Technology / Library | SkyPulse Use Case | Why Selected Over Counterpart (One-Liner) |
| :--- | :--- | :--- |
| **Kotlin 2.4+** | Primary language for entire Android app (UI, ViewModels, networking, repositories). | **Over Java**: Native null safety, extension functions, and first-class Coroutines support eliminate boilerplate and NullPointerExceptions. |
| **Jetpack Compose (Material 3)** | Declarative UI toolkit for building adaptive space-themed screens, custom HUD cards, and animations. | **Over XML Layouts**: Unidirectional data flow and reactive state-driven rendering eliminate layout inflation overhead and fragile `findViewById` bindings. |
| **StateFlow & Coroutines** | Reactive state management and background asynchronous task orchestration across ViewModels. | **Over LiveData**: Cold stream execution, native Coroutine operators (`map`, `combine`, `filter`), and non-lifecycle dependency make StateFlow superior for modern reactive pipelines. |
| **Retrofit 3.0 & OkHttp 4** | Type-safe REST API client for communicating with OpenWeather API, DummyJSON, and Spring Boot backend. | **Over Volley / Ktor Client**: Industry-standard annotation-based interface definition with built-in OkHttp interceptor middleware for automated JWT header insertion. |
| **OkHttp WebSockets** | Persistent bidirectional socket connection for real-time live chat room messaging in Comms Channels (`RoomsScreen`). | **Over HTTP Long-Polling**: Real-time bi-directional low-latency socket communication with negligible bandwidth and CPU overhead compared to constant HTTP polling. |
| **Coil Compose** | Asynchronous image loading, memory caching, and disk caching for space feeds, catalogs, and avatar stickers. | **Over Glide / Picasso**: Built natively for Jetpack Compose and Kotlin Coroutines with minimal memory footprint and zero extra Compose wrapper overhead. |
| **Encrypted TokenManager (SharedPreferences)** | Hardware-encrypted storage for JWT authentication tokens, user sessions, and credentials. | **Over Plain SharedPreferences**: Hardware-backed Android KeyStore AES-256 encryption prevents session token extraction on rooted or compromised devices. |
| **Lottie Compose** | Render vector-based Lottie animations for loading screens and orbital launch status screens. | **Over Frame-by-Frame Bitmaps**: Vector-based JSON animation rendering that scales crisply across all screen resolutions without heavy image assets. |
| **Material 3 Navigation & Motion** | Screen transitions, floating pill bottom bar, bottom sheets, and crossfade page routing. | **Over Legacy Fragment Navigation**: Lightweight composable backstack management without Fragment lifecycle overhead or fragment transaction memory leaks. |

---

## 📱 Core UI Modules & Screens

### 1. **`HomeScreen` — Weather & Atmospheric Intelligence**
* **Responsibility**: Real-time meteorological telemetry, city search, sky clarity score calculation (0–100%), moon phase illumination tracking, and ISS pass countdowns.
* **Key Components**: `getWeatherGradient()`, `StargazerConditionsPanel()`, `CosmicEventsCard()`, `BarrenMarsErrorScreen()`.

### 2. **`HomeFeedScreen` — Cosmic Social Feed**
* **Responsibility**: Space news aggregator, mission updates (ISRO, NASA, JWST), interactive likes, comments, and custom post creation modal.
* **Key Components**: `HomeFeedViewModel`, `CreatePostDialog()`, `FeaturedStory()`, `FeedCard()`.

### 3. **`ShopScreen` & `CheckoutScreen` — Cosmic E-Commerce & Offers**
* **Responsibility**: E-commerce catalog, 5-second cycling animated search hint, multi-banner sales carousel (`STELLAR50`, `NEWMON30`, `WINTERSOL`, `ORBITLAUNCH`), catalog sorting, advanced filter modal, and a dedicated Zomato/Amazon style checkout screen with coupons (`NEWMON30`, `WINTERSOL`, `ORBITLAUNCH`, `ASTROCARD`, `YASHREF50`).
* **Key Components**: `ShopViewModel`, `MultiBannerSalesCarousel()`, `SearchBarAndFilter()`, `SortSelectionBottomSheet()`, `AdvancedFilterBottomSheet()`, `CartBottomSheet()`, `CheckoutScreen()`.

### 4. **`RoomsScreen` — HUD Telemetry Comms Channels**
* **Responsibility**: Real-time group discussion channels (`RF-01` to `RF-06`), custom 3D avatar spheres, encrypted private channel security passkey dialog, hovering oval FAB (`+ Create Channel`), and WebSocket live chat.
* **Key Components**: `RoomsViewModel`, `CommsHeaderDeck()`, `RoomCard()`, `RoomAvatarIcon()`, `PasskeyDialog()`, `CreateChannelDialog()`, `ChatRoomView()`.

### 5. **`ProfileScreen` — Slate Onyx Dashboard**
* **Responsibility**: Hero header with moonlit landscape backdrop, clearance stats, HUD impact metrics, active equipment list, and unlocked badges bento grid.
* **Key Components**: `SlateOnyxHeroHeader()`, `SlateHUDMetricsSection()`, `OnyxSegmentedTabs()`, `ObservationLogFeed()`, `ActiveEquipmentPanel()`, `UnlockedBadgesGrid()`.

---

## 🔐 Client Security Practices

1. **Automatic JWT Injection**: `AuthInterceptor` automatically intercepts outgoing HTTP requests and injects the `Authorization: Bearer <JWT>` header.
2. **Encrypted Token Storage**: Tokens are stored using AES-256 encryption via Android KeyStore.
3. **Session Auto-Validation**: App validates token expiration on launch and routes to `LoginScreen` if expired.
