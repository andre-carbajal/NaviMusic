# NaviMusic

NaviMusic is a music bot for Discord, developed in Java using the Java Discord API (JDA).

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.


### Prerequisites

- Java 17
- Maven

### Configuration

Before compiling the project, you need to set up your environment variables:

1. Navigate to the `src/main/resources` directory.
2. Rename the `.envexample` file to `.env`.
3. Open the `.env` file and replace `your_discord_token` with your actual Discord token.

The `.env` file is located in the `src/main/resources` directory and is used to safely store sensitive information such as tokens and passwords. Never share your `.env` file or any sensitive information from it.

### Installing
1. Clone the repository
    ```bash
    git clone https://github.com/andre-carbajal/NaviMusic.git
    ```
2. Navigate into the cloned repository
    ```bash
    cd NaviMusic
    ```
3. Build the project with Maven
    ```bash
    mvn clean compile
    ```

### Running the Application

You can run the application using the following command:
```bash
java -jar target/NaviMusic-1.0-jar-with-dependencies.jar
```

If you want to run the application in console mode without the GUI, you can use the nogui parameter:
```bash
java -jar target/NaviMusic-1.0-jar-with-dependencies.jar nogui
```
Please note that you'll need to implement the functionality to handle the `nogui` parameter in your Java application.

### License
This project is licensed under the GNU GENERAL PUBLIC LICENSE
Version 3 License—see the [LICENSE](LICENSE) file for details