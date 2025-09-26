package net.andrecarbajal.naviMusic.util;

import lombok.experimental.UtilityClass;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class URLUtils {
    public Optional<String> getURLParam(String url, String param) {
        final String regex = param + "=([^&]*)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    public boolean isURL(String input) {
        try {
            new URI(input);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
