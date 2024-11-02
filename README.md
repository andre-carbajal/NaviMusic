# NaviMusic: Your Personal Music Bot for Discord

Welcome to NaviMusic, a highly user-friendly music bot for Discord. Developed using Discord4J, NaviMusic offers a
seamless music experience for your Discord server.

## Table of Contents

- [Getting Started](#getting-started)
- [Prerequisites](#prerequisites)
- [Running the Application](#running-the-application)
- [Running the Application with Docker](#running-the-application-with-docker)
- [License](#license)

## Getting Started

This section provides a step-by-step guide on how to get a copy of NaviMusic up and running on your local machine for
development and testing purposes.

## Prerequisites

Before you begin, ensure you have the following installed on your machine:

- Java 21
- Maven
- Docker (optional)

## Running the Application

Replace x.y with the version number of the last release.
{nogui} is an optional argument to run the bot without the GUI.

First execute the bot with all the environment variables(DISCORD_TOKEN, SPOTIFY_CLIENT_ID, SPOTIFY_SECRET)
Later follow the steps to get the YOUTUBE_OAUTH2_CODE
Finally you need to configure your YOUTUBE_OAUTH2_CODE on the environment variables

### With environment variables

- Set the following environment variables:
    - DISCORD_TOKEN: Your Discord bot token
    - SPOTIFY_CLIENT_ID: Your Spotify client ID
    - SPOTIFY_SECRET: Your Spotify secret
- Run the bot

```bash
java -jar NaviMusic-x.y.jar {nogui}
```

- Complete the steps to get the YOUTUBE_OAUTH2_CODE
- Set the following environment variables:
    - YOUTUBE_OAUTH2_CODE: Your Youtube OAuth2 code
- Run the bot again

```bash
java -jar NaviMusic-x.y.jar {nogui}
```

### With command line arguments

- Run the bot

```bash
java -jar NaviMusic-x.y.jar DISCORD_TOKEN=your_discord_token SPOTIFY_CLIENT_ID=your_spotify_client_id SPOTIFY_SECRET=your_spotify_secret {nogui}
```

- Complete the steps to get the YOUTUBE_OAUTH2_CODE
- Run the bot again

```bash
java -jar NaviMusic-x.y.jar DISCORD_TOKEN=your_discord_token SPOTIFY_CLIENT_ID=your_spotify_client_id SPOTIFY_SECRET=your_spotify_secret YOUTUBE_OAUTH2_CODE=your_youtube_oauth2_code {nogui}
```

## Running the Application with Docker

- Pull the Docker image from Docker Hub:

```bash
docker pull anvian/navi-music
```

- Run the Docker container, passing your Discord, Spotify Client ID, and Spotify Secret tokens as arguments:

```bash
docker run anvian/navi-music -e DISCORD_TOKEN=your_discord_token SPOTIFY_CLIENT_ID=your_spotify_client_id SPOTIFY_SECRET=your_spotify_secret
```

- Complete the steps to get the YOUTUBE_OAUTH2_CODE
- Run the Docker container again, passing your Youtube OAuth2 code as an argument:

```bash
docker run anvian/navi-music -e DISCORD_TOKEN=your_discord_token SPOTIFY_CLIENT_ID=your_spotify_client_id SPOTIFY_SECRET=your_spotify_secret YOUTUBE_OAUTH2_CODE=your_youtube_oauth2_code
```

## License

This project is licensed under the GNU GENERAL PUBLIC LICENSE Version 3
For more details, see the [LICENSE](LICENSE) file.
