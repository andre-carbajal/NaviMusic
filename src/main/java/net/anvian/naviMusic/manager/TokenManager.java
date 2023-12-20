package net.anvian.naviMusic.manager;

public class TokenManager {
    public String getToken(String[] args) {
        String token = System.getenv("DISCORD_TOKEN");

        if (token == null || token.isEmpty()) {
            if (args.length > 0) {
                token = args[0];
            } else {
                System.out.println("Please provide the Discord token as an environment variable or as a command line argument.");
                System.exit(1);
            }
        }

        return token;
    }
}
