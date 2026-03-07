# NaviMusic: Your Personal Music Bot for Discord

Welcome to NaviMusic, a highly user-friendly music bot for Discord. Now rebuilt with **Kotlin** and **Spring Boot**, NaviMusic offers a seamless and robust music experience for your Discord server.

## Table of Contents

- [Getting Started](#getting-started)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Available Commands](#available-commands)
- [Running the Application](#running-the-application)
- [Running the Application with Docker](#running-the-application-with-docker)
- [Production Mode](#production-mode)
- [License](#license)

## Getting Started

This section provides a guide on how to get NaviMusic up and running on your local machine.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 25** (Required for DAVE/JDave encryption)
- **Maven**
- **Docker** (Optional)

## Configuration

NaviMusic uses environment variables for configuration. You can also set these in `src/main/resources/application.properties`.

### Environment Variables

| Variable | Description |
| :--- | :--- |
| `DISCORD_TOKEN` | Your Discord Bot Token. |
| `SPOTIFY_CLIENT_ID` | Your Spotify Application Client ID. |
| `SPOTIFY_CLIENT_SECRET` | Your Spotify Application Client Secret. |
| `YOUTUBE_POTOKEN` | YouTube PO Token. |
| `YOUTUBE_VISITOR` | YouTube Visitor ID. |
| `YOUTUBE_OAUTH2` | YouTube OAuth2 Code. |

> **Note:** For YouTube credentials, use a [PO Token generator](https://github.com/iv-org/youtube-trusted-session-generator). If `YOUTUBE_OAUTH2` is missing, follow the console instructions on the first run to authenticate.

## Available Commands

NaviMusic uses Slash Commands for easy interaction.

### Music Commands
- `/play [query]` - Play a song or playlist from YouTube/Spotify.
- `/skip` - Skip the current track.
- `/queue` - View the current music queue.
- `/nowplaying` - Show details of the currently playing track.
- `/pause` - Pause playback.
- `/resume` - Resume playback.
- `/clear` - Clear the queue.
- `/leave` - Make the bot leave the voice channel.
- `/repeat` - Enable track repetition.
- `/repeatoff` - Disable track repetition.
- `/shuffle` - Shuffle the current queue.

### General Commands
- `/help` - List all available commands.
- `/roll [sides]` - Rolls a die with a specified number of sides (default is 6).

## Running the Application

**Important for Java 25:** Due to the use of Discord's DAVE protocol (E2EE) through the JDave library, you **must** include the `--enable-native-access=ALL-UNNAMED` flag in your Java execution command.

1. Build the project:
   ```bash
   mvn clean package
   ```
2. Run the JAR:
   ```bash
   java --enable-native-access=ALL-UNNAMED -jar target/NaviMusic-x.x.x.jar {nogui}
   ```
   *`{nogui}` is an optional argument to disable the console GUI.*

## Running the Application with Docker

The Docker image includes the necessary Java 25 flags and environment variable support.

### Local Development (with logs)
```bash
docker build -t navimusic-bot .
docker run --env-file .env navimusic-bot
```

## Production Mode

For production environments, NaviMusic includes a `prod` profile that silences verbose "Song Added" logs, showing only Warnings and Errors to keep the console clean.

### Running in Production (Docker)
The `Dockerfile` automatically activates the `prod` profile. To run it:
```bash
docker run -d --restart always --env-file .env --name navimusic anvian/navi-music
```

### Running in Production (Manual)
If running the JAR manually, add the profile flag:
```bash
java --enable-native-access=ALL-UNNAMED -jar target/NaviMusic-x.x.x.jar nogui --spring.profiles.active=prod
```

## License

This project is licensed under the **GNU General Public License Version 3**. See the [LICENSE](LICENSE) file for details.
