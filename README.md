# NaviMusic: Your Personal Music Bot for Discord

Welcome to NaviMusic, a highly user-friendly music bot for Discord. Developed using the robust Java Discord API (JDA), NaviMusic offers a seamless music experience for your Discord server.

## Table of Contents
- [Getting Started](#getting-started)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Docker Usage](#docker-usage)
- [License](#license)

## Getting Started

This section provides a step-by-step guide on how to get a copy of NaviMusic up and running on your local machine for development and testing purposes.

## Prerequisites

Before you begin, ensure you have the following installed on your machine:

- Java 17
- Maven
- Docker (optional)

## Installation

Follow these steps to get a copy of NaviMusic on your local machine:

1. **Clone the repository**
    ```bash
    git clone https://github.com/andre-carbajal/NaviMusic.git
    ```
2. **Navigate into the cloned repository**
    ```bash
    cd NaviMusic
    ```
3. **Build the project with Maven**
    ```bash
    mvn clean compile
    ```

## Running the Application

You can run NaviMusic using the following commands:
Replace x.y with the version number of the last release.

### With DISCORD_TOKEN in your system variables:

With GUI:
```bash
java -jar target/NaviMusic-x.y.jar
```

Without GUI:
```bash
java -jar target/NaviMusic-x.y.jar nogui
```

### With DISCORD_TOKEN as a command line argument:

With GUI:
```bash
java -jar target/NaviMusic-x.y <your_discord_token>
```

Without GUI:
```bash
java -jar target/NaviMusic-x.y <your_discord_token> nogui
```

## Docker Usage

You have two options to run NaviMusic using Docker:

1. **Pull the Docker image from Docker Hub:**

    ```bash
    docker pull anvian/navi-music
    docker run -e DISCORD_TOKEN=your_discord_token anvian/navi-music
    ```

2. **Or, build and run the Docker image locally:**

   First, build the Docker image:

    ```bash
    docker build -t navimusic .
    ```

   Then, run the Docker container, passing your Discord token as an argument:

    ```bash
    docker run -e DISCORD_TOKEN=your_discord_token navimusic
    ```

## License
This project is licensed under the GNU GENERAL PUBLIC LICENSE Version 3
For more details, see the [LICENSE](LICENSE) file.