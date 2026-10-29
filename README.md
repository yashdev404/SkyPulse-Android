# SkyPulse

SkyPulse is an Android application evolving from a weather utility into a comprehensive, multi-module social e-commerce platform. Built with modern Android development practices, SkyPulse leverages Jetpack Compose for a reactive UI and integrates a robust backend architecture to support social feeds, real-time chat, and an integrated shopping experience.

## 📱 Features

Currently implemented and in active development:

*   **Atmospheric & Space Data (Current):** Real-time weather and astronomical data fetching utilizing Retrofit, Coroutines, and a reactive UI state. Data is aggregated from trusted sources like **NASA** and **ISRO** APIs.
*   **Space & Tech Social Feed:** A dynamic home feed aggregating space news, astronomical photos, and articles from leading space agencies and tech sources.
*   **Real-time Chat Rooms:** Themed discussion rooms enabling real-time communication between space enthusiasts and users.
*   **Integrated Shop:** A full e-commerce module allowing users to browse space-themed merchandise, manage a shopping cart, and complete checkouts with automated email confirmations.
*   **User Profiles:** Comprehensive user management including profile customization and authentication.

## 🛠️ Technology Stack

SkyPulse demonstrates proficiency in both modern Android frontend development and scalable backend architecture.

### Android Client (Frontend)
*   **Language:** Kotlin
*   **UI Toolkit:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM (Model-View-ViewModel), Clean Architecture principles
*   **Network:** Retrofit, OkHttp
*   **Asynchrony:** Kotlin Coroutines & Flow (StateFlow)
*   **Image Loading:** Coil
*   **Local Storage (Planned):** Room Database for Offline-First caching
*   **Dependency Injection (Planned):** Dagger Hilt

### Backend Service (Architecture)
The backend is structured using a microservices-inspired monolithic architecture built with Spring Boot.

*   **Framework:** Java / Spring Boot
*   **Authentication:** Spring Security with JWT (JSON Web Tokens)
*   **Databases (Polyglot Persistence):**
    *   **PostgreSQL:** Relational data management for Users, Products, Carts, and Orders. Ensures strict ACID compliance for transactions.
    *   **MongoDB:** Document-based storage for the Home Feed. Provides flexibility for diverse post types (text, images, external articles).
    *   **Redis:** In-memory data structure store used for Pub/Sub in real-time Chat Rooms and fast session/cart caching.
*   **Real-time Communication:** WebSockets (Spring WebSocket) for chat rooms.
*   **Email Service:** JavaMailSender integrated with SMTP for transactional emails (Checkout Confirmations).

## 🏗️ System Architecture (High-Level Design)

1.  **Client-Server Interaction:** The Android Compose app communicates with the Spring Boot backend via REST APIs for the Shop, Feed, and Profile modules.
2.  **Real-Time Data:** The Chat Room module utilizes WebSockets for bi-directional, low-latency messaging, orchestrated via Redis Pub/Sub to allow horizontal scaling of the backend.
3.  **Checkout Workflow:** When a user initiates a checkout, the Spring Boot application processes the transaction against the PostgreSQL database, clears the user's cart, and triggers an asynchronous email confirmation via JavaMailSender.
4.  **Content Delivery:** Feed content is aggregated from MongoDB, allowing for flexible schemas to accommodate evolving media types.

## 🚀 Future Roadmap & Active Development

*   **Phase 1:** Complete migration to Dagger Hilt for robust dependency injection across all ViewModels and Repositories.(DONE)
*   **Phase 2:** Implement Room Database caching to support an Offline-First experience for the Shop and Feed modules.
*   **Phase 3:** Finalize Spring Boot API endpoints for the Shop module and integrate the Android client checkout flow.
*   **Phase 4:** Establish WebSocket connections for the Chat Rooms feature.
*   **Phase 5:** Expand unit testing coverage utilizing JUnit 5 and Turbine for Kotlin Flows.
