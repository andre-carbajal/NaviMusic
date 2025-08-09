# NaviMusic: Your Personal Music Bot for Discord

Welcome to NaviMusic, a highly user-friendly music bot for Discord. Developed using JDA, NaviMusic offers a
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

First you need to get the YOUTUBE_PO_TOKEN, YOUTUBE_VISITOR. You can get them with [this generator](https://github.com/iv-org/youtube-trusted-session-generator)
Then you need to get the YOUTUBE_OAUTH2_CODE. You can get it by running the bot and following the steps.
  - Execute the bot with the environment variables(DISCORD_TOKEN, SPOTIFY_CLIENT_ID, SPOTIFY_CLIENT_SECRET, YOUTUBE_POTOKEN, YOUTUBE_VISITOR) and YOUTUBE_OAUTH2 set to null
  - Follow the steps in the console
Finally execute the bot with all the environment variables(DISCORD_TOKEN, SPOTIFY_CLIENT_ID, SPOTIFY_CLIENT_SECRET, YOUTUBE_POTOKEN, YOUTUBE_VISITOR, YOUTUBE_OAUTH2)

### With environment variables

- Set the following environment variables:
    - DISCORD_TOKEN: Your Discord bot token
    - SPOTIFY_CLIENT_ID: Your Spotify client ID
    - SPOTIFY_CLIENT_SECRET: Your Spotify secret
    - YOUTUBE_POTOKEN: Your Youtube PO token
    - YOUTUBE_VISITOR: Your Youtube visitor
    - YOUTUBE_OAUTH2: Your Youtube OAuth2 code
- Run the bot

```bash
java -jar NaviMusic-x.y.jar {nogui}
```

## Running the Application with Docker

- Pull the Docker image from Docker Hub:

```bash
docker pull anvian/navi-music
```

- Run the Docker container, passing your Discord, Spotify Client ID, and Spotify Secret tokens as arguments:

```bash
docker run anvian/navi-music -e DISCORD_TOKEN=your_discord_token SPOTIFY_CLIENT_ID=your_spotify_client_id SPOTIFY_CLIENT_SECRET=your_spotify_secret YOUTUBE_POTOKEN=your_youtube_potoken YOUTUBE_VISITOR=your_youtube_visitor YOUTUBE_OAUTH2=your_youtube_oauth2_code
```

## License

This project is licensed under the GNU GENERAL PUBLIC LICENSE Version 3
For more details, see the [LICENSE](LICENSE) file.
