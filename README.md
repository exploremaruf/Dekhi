# Dekhi
A high-performance, minimalist IPTV player for Android built with Material Design 3.

## Features
- **Native M3U/M3U8 Parsing**: Robust, regex-based engine to extract channel names, logos, groups, and streaming metadata.
- **Dynamic Design System**: A strict flat-minimalist UI with seamless Light and Dark mode adaptation.
- **Advanced Media3 Playback**: Powered by ExoPlayer with support for HLS, DASH, and custom control overlays.
- **Intelligent Navigation**: Automated playback error recovery with "Next Channel" failover and index-protected queueing.
- **Local & Remote Imports**: Import playlists via remote URLs or through a system-integrated local file picker.
- **Dynamic Content Previews**: Automatically generates playlist descriptions by sampling the first five channels.
- **Gesture Controls**: Intuitive vertical swipes for brightness and volume, with double-tap control visibility.
- **Persistence Layer**: Fully reactive Room Database implementation for offline playlist management and watch history.

## Architecture & Tech Stack
- **Language**: 100% Java (No Kotlin dependency).
- **Architecture**: MVVM (Model-View-ViewModel) with the Repository pattern.
- **Jetpack Components**: 
    - **Room**: SQLite abstraction for channel/playlist persistence.
    - **LiveData & ViewModel**: Reactive UI updates and lifecycle-aware data management.
    - **WindowInsets**: Edge-to-edge layout management for modern Android displays.
- **Media**: Android Media3 (ExoPlayer) for high-performance low-latency streaming.
- **UI System**: Material Design 3 (M3) utilizing dynamic design tokens and flat minimalism.
- **Image Loading**: Custom native LRU-cache implementation for logo decoding without external libraries.

## Project Structure
```text
app/src/main/java/com/dekhi/dekhi/
├── data/
│   ├── dao/             # Room DAO interfaces (Channel, Playlist)
│   ├── entity/          # Room Entities (Channel, Playlist)
│   ├── AppDatabase.java # Database configuration & migrations
│   └── PlaylistRepository.java  # Data synchronization & network logic
├── ui/
│   ├── home/            # Dashboard, Favorites, and Settings fragments
│   ├── player/          # Media3 PlayerActivity & gesture logic
│   └── playlist/        # Channel list & category filtering
└── util/
    ├── M3UParser.java   # Regex-based playlist parsing engine
    ├── ThemeHelper.java # Dynamic Light/Dark theme manager
    └── NativeImageLoader.java # Custom bitmap cache & loader
```

## Quick Setup & Usage
1. **Clone the Repository**
   ```bash
   git clone https://github.com/exploremaruf/dekhi.git
   ```
2. **Sync & Build**
   Open the project in Android Studio (Ladybug or newer) and perform a **Gradle Sync**.
3. **Import a Playlist**
   - Click the **+** (Import) button on the dashboard.
   - **Remote**: Paste a valid `.m3u` or `.m3u8` URL.
   - **Local**: Select "Pick M3U File" to browse your device's storage.
4. **Watch & Navigate**
   - Tap any channel to start the cinematic player.
   - Swipe vertically on the left for brightness, and right for volume.
   - Use the Next/Previous buttons to navigate through your collection.

---
*Developed with a focus on speed, privacy, and minimalist aesthetics.*
