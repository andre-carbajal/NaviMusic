# Release Notes

### Release pipeline recovery

- Versioned the Maven Wrapper properties required by GitHub Actions, allowing release builds to download the pinned
  Maven 3.9.12 distribution.
- Bumped the recovery release to `4.2.1`; the previously published `4.2.0` tag remains unchanged.

### Improved music queue concurrency

- Added the optional `/play` boolean option `shuffle`. With `shuffle:true`, the bot adds the requested track, playlist,
  or album and then shuffles every pending track without interrupting the one currently playing.
- Added isolated, serialized music state per Discord guild. Simultaneous commands in one guild now update the queue in
  arrival order, while separate guilds continue independently.
- Added an ordered per-guild load pipeline so concurrent `/play` requests cannot be enqueued according to whichever
  provider search finishes first.
- YouTube playlists are enqueued as a contiguous block. Spotify playlists and albums now resolve their YouTube matches
  sequentially, preserving playlist order and avoiding a burst of provider requests.
- Made playback lifecycle events and queue operations (`skip`, `clear`, `shuffle`, repeat, pause, resume, leave, and
  queue inspection) coordinate through the guild's serialized state.
- Added configurable safeguards in `application.properties`:
    - `app.music.max-pending-loads=20`
    - `app.music.max-queue-size=500`
    - `app.music.max-playlist-tracks=100`
- Released per-guild audio state and executors when the bot leaves, is left alone in a voice channel, or disconnects
  after inactivity.
- Kept Spring virtual threads disabled; queue ordering is enforced by the per-guild serialization mechanism rather than
  thread type.
