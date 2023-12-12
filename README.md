# NaviMusic: Your Personal Music Bot for Discord

Welcome to NaviMusic, a highly user-friendly music bot for Discord. Developed using the robust Java Discord API (JDA), NaviMusic offers a seamless music experience for your Discord server.

## Table of Contents
- [Getting Started](#getting-started)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [License](#license)

## Getting Started

This section provides a step-by-step guide on how to get a copy of NaviMusic up and running on your local machine for development and testing purposes.

## Prerequisites

Before you begin, ensure you have the following installed on your machine:

- Java 17
- Maven

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
3. **Set up your environment variables**

   Before compiling the project, you need to set up your environment variables:

   1. Navigate to the `src/main/resources` directory.
   2. Rename the `.envexample` file to `.env`.
   3. Open the `.env` file and replace `your_discord_token` with your actual Discord token.

   > ⚠️ **Security Notice:** The `.env` file is used to safely store sensitive information such as tokens and passwords. Never share your `.env` file or any sensitive information from it.

4. **Build the project with Maven**
    ```bash
    mvn clean compile
    ```

## Running the Application

You can run NaviMusic using the following command:

```bash
java -jar target/NaviMusic-1.0-jar-with-dependencies.jar
```

For a console-only experience, you can run NaviMusic with the `nogui` parameter:
```bash
java -jar target/NaviMusic-1.0-jar-with-dependencies.jar nogui
```

## License
This project is licensed under the GNU GENERAL PUBLIC LICENSE Version 3
For more details, see the [LICENSE](LICENSE) file.