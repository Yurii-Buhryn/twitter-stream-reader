package org.interview.oauth.twitter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import org.interview.oauth.twitter.dto.TwitterMessageDto;
import org.interview.oauth.twitter.dto.TwitterUserDto;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TwitterJsonParser {

    private ObjectMapper mapper;
    private DateTimeFormatter formatter;

    public TwitterJsonParser() {
        this.mapper = new ObjectMapper();
        this.formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy").withLocale(Locale.ENGLISH);
    }

    public TwitterMessageDto getMessage(JsonNode messageJson) {
        String createdAt = messageJson.get("created_at").asText();
        String id = messageJson.get("id").asText();
        String text = messageJson.get("text").asText();

        return new TwitterMessageDto(id, formatter.parseDateTime(createdAt), text);
    }

    public TwitterUserDto getUser(JsonNode authorJson) {
        String authorId = authorJson.get("id").asText();
        String authorName = authorJson.get("name").asText();
        String authorCreatedAt = authorJson.get("created_at").asText();
        String authorScreenName = authorJson.get("screen_name").asText();

        return new TwitterUserDto(authorId, authorName, formatter.parseDateTime(authorCreatedAt), authorScreenName);
    }
}
