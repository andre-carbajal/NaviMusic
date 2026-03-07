# Release Notes

- **Production-Optimized Logging:** Added a specialized `prod` Spring profile and updated the `Dockerfile` to suppress verbose song-loading logs in containerized environments, focusing on warnings and errors for cleaner production output.
- **Automated Command Registration:** Introduced a new `CommandManager` that automatically detects and registers all slash commands marked as Spring `@Component`, eliminating manual registration and reducing boilerplate code.
- **Architecture Decoupling (Presenter Pattern):** Extracted UI response logic into a dedicated `MusicPresenter` component, ensuring `MusicService` remains focused on core audio logic while the presenter handles the visual formatting of `RichResponse` embeds.
- **Enhanced Spotify & YouTube Link Handling:** Implemented a robust `SpotifyResource` parser for safer URL handling and fixed a logic error in `AudioResultHandler` to ensure YouTube playlist links in Discord responses correctly point to the full playlist rather than individual tracks.
- **Spring-Native UI Management:** Refactored `UIManager` as a managed Spring component, improving the application's lifecycle management and ensuring proper initialization during the Spring Boot startup sequence.
- **Fixed Discord DAVE (E2EE) Protocol Integration:** Resolved the issue where the bot would connect but not play audio due to the MLS session not being established correctly.
- **Asynchronous Voice Connection & Scalability:** Implemented Kotlin Coroutines for voice channel connection and Spotify data fetching. This prevents JDA event thread blocking and ensures high performance when used in multiple servers simultaneously.
- **Unified Response System:** Consolidated `Response` and `RichResponse` into a single, streamlined `RichResponse` class, simplifying the codebase and ensuring all bot replies use a polished, consistent Embed format.
- **Interactive Playlists:** Titles for Spotify and YouTube playlists/albums in the bot's responses are now clickable links, allowing users to jump directly to the source.
- **Optimized Spotify Loading:** Playlists and albums are now processed in parallel with the voice connection, significantly reducing the wait time before music starts playing.
- **Improved Music Commands UI:** Upgraded `nowplaying`, `skip`, `clear`, `pause`, `resume`, and `leave` commands with detailed information, including thumbnails, duration bars, and playback status.
- **Code Quality & Maintenance:** Refactored `MusicService` to eliminate duplicated logic and improved the `VideoInfo` utility with centralized time formatting.
- **Robust Connection Handling:** Improved `connectToChannel` logic to wait for the DAVE/MLS session to initialize before starting audio transmission.
