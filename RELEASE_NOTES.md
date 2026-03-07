# Release Notes

- **Fixed Discord DAVE (E2EE) Protocol Integration:** Resolved the issue where the bot would connect but not play audio due to the MLS session not being established correctly.
- **Asynchronous Voice Connection & Scalability:** Implemented Java 25 Virtual Threads for voice channel connection and Spotify data fetching. This prevents JDA event thread blocking and ensures high performance when used in multiple servers simultaneously.
- **Unified Response System:** Consolidated `Response` and `RichResponse` into a single, streamlined `RichResponse` class, simplifying the codebase and ensuring all bot replies use a polished, consistent Embed format.
- **Interactive Playlists:** Titles for Spotify and YouTube playlists/albums in the bot's responses are now clickable links, allowing users to jump directly to the source.
- **Optimized Spotify Loading:** Playlists and albums are now processed in parallel with the voice connection, significantly reducing the wait time before music starts playing.
- **Improved Music Commands UI:** Upgraded `nowplaying`, `skip`, `clear`, `pause`, `resume`, and `leave` commands with detailed information, including thumbnails, duration bars, and playback status.
- **Code Quality & Maintenance:** Refactored `MusicService` to eliminate duplicated logic and improved the `VideoInfo` utility with centralized time formatting.
- **Robust Connection Handling:** Improved `connectToChannel` logic to wait for the DAVE/MLS session to initialize before starting audio transmission.
