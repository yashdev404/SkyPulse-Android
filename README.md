<div align="center">

# 🌌 SkyPulse

**A Next-Generation Space, Weather, and Cosmic E-Commerce Experience on Android**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android_SDK-37-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-Spring_Boot_3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

*SkyPulse seamlessly connects space enthusiasts, stargazers, and tech explorers by combining real-time weather & space atmospheric intelligence, an interactive social feed, live chat lounges, and an integrated cosmic marketplace—all in a high-performance Material 3 Jetpack Compose application.*

</div>

---

## 📖 Overview & Detailed Docs

**SkyPulse** evolved from a hyper-focused atmospheric weather utility into a feature-rich, multi-domain Android ecosystem. Built with modern Android development standards, SkyPulse features a reactive, state-driven UI powered by **Jetpack Compose (Material 3)**, asynchronous data streams with **Kotlin Coroutines & Flow**, and a polyglot persistence **Spring Boot** backend.

For full architectural deep-dives, tech justifications, and use cases:
* 📱 **[Android Frontend Architecture Documentation (FRONTEND.md)](FRONTEND.md)**
* ⚙️ **[Spring Boot Backend Architecture Documentation (BACKEND.md)](BACKEND.md)**

---

## ⚡ Key Features

### 🌤️ Weather & Atmospheric Intelligence (`HomeScreen`)
* **Real-time Meteorological Data:** Live temperature, humidity, wind conditions, UV index, and weather forecasts.
* **Dynamic Sky Gradients:** Ambient background gradients and UI themes that adapt smoothly to current weather conditions (e.g., Clear Sky, Rain, Thunderstorms, Night/Space).
* **Location Search:** Global city search with instant meteorological data fetching.

### 🚀 Cosmic Social Feed (`HomeFeedScreen`)
* **Space & Astronomy Aggregator:** Dynamic feed featuring space missions, astronomical photography, and tech discoveries from space agencies (ISRO, NASA) and space tech sources.
* **Interactive Community Engagement:** Express reactions (Likes, Comments, Bookmarks) and share content with fellow space enthusiasts.

### 🛍️ Cosmic Shop & E-Commerce (`ShopScreen` & `CartBottomSheet`)
* **Space Gear & Merchandise:** Curated catalog of telescopes, astrophotography gear, apparel, and space memorabilia.
* **Smart Shopping Cart:** Interactive slide-over cart bottom sheet supporting real-time quantity adjustments and total calculation.
* **Zomato / Amazon-Style Checkout:** Dedicated `CheckoutScreen` supporting custom promo codes, Zomato/Amazon style coupon cards (`NEWMON30`, `WINTERSOL`, `ORBITLAUNCH`, `ASTROCARD`, `YASHREF50`), referral discounts, and instant card offers.
* **Order History:** Track current and past order status linked to user accounts.

### 💬 Live Chat Lounges (`RoomsScreen`)
* **HUD Telemetry Comms Channels:** Real-time channels with frequency codes (`RF-01` to `RF-06`), radar search, encrypted private channels, and custom 3D avatar spheres.
* **Real-time Messaging:** Persistent message sync and instant chat feeds powered by WebSockets and Redis Pub/Sub.

### 👤 Profile & Authentication (`LoginScreen` & `ProfileScreen`)
* **Slate Onyx Profile Variant:** Hero header with moonlit landscape backdrop, clearance stats, HUD telemetry metrics, active equipment list, and unlocked badges grid.
* **JWT-Based Authentication:** Secure Login and Registration backed by Spring Security and `TokenManager`.

---

## 🛠️ Technology Stack Overview

Detailed technology breakdowns and one-line architectural justifications are available in **[FRONTEND.md](FRONTEND.md)** and **[BACKEND.md](BACKEND.md)**.

### **Android Frontend** *(See [FRONTEND.md](FRONTEND.md))*
| Component | Technology / Library | Why Used Over Counterpart |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.4+ | Native null safety, extension functions, and first-class Coroutines support eliminate boilerplate and NPEs compared to Java. |
| **UI Framework** | Jetpack Compose (Material 3) | Unidirectional data flow and reactive state-driven rendering eliminate layout inflation overhead compared to XML views. |
| **Architecture** | MVVM + StateFlow | Cold stream execution, native Coroutine operators, and non-lifecycle dependency make StateFlow superior to LiveData. |
| **Networking** | Retrofit 3.0 & OkHttp 4 | Industry-standard annotation-based interface definition with built-in OkHttp interceptor middleware for JWT injection. |
| **WebSockets** | OkHttp WebSockets | Real-time bi-directional low-latency socket communication with negligible bandwidth and CPU overhead compared to HTTP polling. |
| **Image Loading** | Coil Compose | Native Jetpack Compose & Coroutines integration with minimal memory footprint compared to Glide/Picasso. |

### **Backend Architecture** *(See [BACKEND.md](BACKEND.md))*
| Layer | Technology | Why Used Over Counterpart |
| :--- | :--- | :--- |
| **Framework** | Java 17/21 / Spring Boot 3 | Enterprise-grade dependency injection, robust multi-threading, and rich Spring ecosystem compared to Express.js or Django. |
| **Security** | Spring Security + JWT | Stateless authentication allows seamless horizontal scaling across server instances without session replication overhead. |
| **Relational DB** | PostgreSQL & Spring Data JPA | Superior support for complex relational joins, JSONB column indexing, strict ACID consistency compared to MySQL/SQLite. |
| **Document DB** | MongoDB | Schema-less document flexibility enables fast insertions and dynamic array queries for dynamic social content and chat history. |
| **In-Memory Caching** | Redis & Redis Pub/Sub | Ultra-fast sub-millisecond in-memory message routing with minimal setup complexity compared to RabbitMQ/Kafka. |
| **Real-time Protocol** | WebSockets (STOMP) | Full bi-directional client-server messaging allowing clients to send and receive chat packets over a single connection. |

---

## 🏗️ System Architecture

```mermaid
flowchart TD
    subgraph "Android Application (SkyPulse Client)"
        UI[Jetpack Compose UI - Material 3]
        VM[ViewModels - StateFlow]
        REPO[Repository Layer]
        INTERCEPTOR[AuthInterceptor / TokenManager]
        NET[Retrofit REST & WebSockets]
        
        UI --> VM
        VM --> REPO
        REPO --> INTERCEPTOR
        INTERCEPTOR --> NET
    end

    subgraph "Spring Boot Backend"
        SEC[Spring Security + JWT Filter]
        REST[REST Controllers]
        WS[WebSocket Handler]
        
        SEC --> REST
        SEC --> WS
    end

    subgraph "Data & Persistence Layer"
        PG[(PostgreSQL - ACID Data)]
        MONGO[(MongoDB - Documents & Feed)]
        REDIS[(Redis - Pub/Sub & Cache)]
        SMTP[SMTP Mail Server]
    end

    NET <-->|HTTPS / REST| SEC
    NET <-->|WSS / WebSockets| WS

    REST --> PG
    REST --> MONGO
    REST --> SMTP
    WS --> REDIS
```

---

## 📁 Project Structure

```
SkyPulse/
├── FRONTEND.md                                   # Detailed Android Frontend Tech Specs
├── BACKEND.md                                    # Detailed Spring Boot Backend Tech Specs
├── README.md                                     # Main Project Overview & Roadmap
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/com/yash/skypulse/
│       │       ├── MainActivity.kt               # App Entry Point & Navigation
│       │       ├── MainScreen.kt                 # Bottom Navigation Container
│       │       ├── HomeScreen.kt                 # Weather & Atmospheric Dashboard
│       │       ├── HomeFeedScreen.kt             # Social Space Feed Screen
│       │       ├── ShopScreen.kt                 # E-Commerce Catalog Screen
│       │       ├── CartBottomSheet.kt            # Cart Slide-Over Sheet
│       │       ├── CheckoutScreen.kt             # Full Checkout & Coupon Offers
│       │       ├── RoomsScreen.kt                # HUD Telemetry Comms Channels
│       │       ├── ProfileScreen.kt              # Slate Onyx Profile Variant
│       │       ├── LoginScreen.kt                # Auth Screen (Login & Register)
│       │       ├── WeatherApi.kt                 # Retrofit Interface for Weather Data
│       │       ├── SkyPulseApi.kt                # Retrofit Interface for Spring Backend
│       │       ├── TokenManager.kt               # Encrypted JWT Token Persistence
│       │       ├── AuthInterceptor.kt            # OkHttp JWT Header Interceptor
│       │       └── ui/theme/                     # Material 3 Color Palette, Type & Theme
│       └── res/                                  # Drawables, Mipmaps, Values & XML
├── gradle/
│   └── libs.versions.toml                        # Version Catalog Dependencies
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Getting Started

### **Prerequisites**
* **Android Studio:** Android Studio Ladybug (2024.2.1) or newer recommended.
* **JDK:** Java 11 or higher (Java 17/21 recommended).
* **Android SDK:** Target SDK `37`, Minimum SDK `24` (Android 7.0+).

### **Installation & Setup**

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/yashdev404/SkyPulse-Android.git
   cd SkyPulse-Android
   ```

2. **Configure API Keys:**
   SkyPulse utilizes a Weather API for live meteorological data. Add your API Key to `local.properties` in the project root:
   ```properties
   WEATHER_API_KEY=your_weather_api_key_here
   ```

3. **Build & Run:**
   * Open the project in Android Studio.
   * Allow Gradle to sync dependencies.
   * Select an Android Emulator or physical device and press **Run** (`Shift + F10`).

---

## 📜 License

This project is licensed under the MIT License.

<div align="center">
  <sub>Built with ❤️ using Kotlin & Jetpack Compose</sub>
</div>
