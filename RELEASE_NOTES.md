# Release Notes

- **Fixed Discord DAVE (E2EE) Protocol Integration:** Resolved the issue where the bot would connect but not play audio due to the MLS session not being established correctly.
- **Asynchronous Voice Connection:** Implemented Java 25 Virtual Threads for voice channel connection, preventing JDA event thread blocking and ensuring high scalability across multiple servers.
- **Enhanced User Experience:** Added automatic reply updates that transform the "Loading" message into a full RichResponse with track details once the audio is ready to play.
- **Robust Connection Handling:** Improved `connectToChannel` logic to wait for the DAVE session to initialize before starting audio transmission.
