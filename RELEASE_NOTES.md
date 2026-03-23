# Release Notes

- **Optional Spotify API:** The bot now starts normally even if Spotify credentials are not provided. A descriptive
  message is shown if a Spotify link is used without the API configured.
- **Mandatory YouTube OAuth:** YouTube OAuth 2.0 is now a strict requirement for the bot to function. The application
  will now fail to start if the OAuth token is missing, ensuring consistent playback and avoiding restricted content
  issues.
