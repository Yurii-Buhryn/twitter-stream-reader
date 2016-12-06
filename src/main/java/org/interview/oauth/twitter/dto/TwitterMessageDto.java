package org.interview.oauth.twitter.dto;

import org.joda.time.DateTime;

public class TwitterMessageDto implements Comparable {
    private String id;
    private DateTime createdAt;
    private String text;

    public TwitterMessageDto() {
    }

    public TwitterMessageDto(final String id, final DateTime createdAt, final String text) {
        this.createdAt = createdAt;
        this.id = id;
        this.text = text;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final DateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    @Override
    public int compareTo(final Object obj) {
        TwitterMessageDto message = (TwitterMessageDto) obj;
        return this.createdAt.compareTo(message.getCreatedAt());
    }

    // ID should be unique identifier, so let's use if for equal objects
    @Override
    public boolean equals(final Object obj) {
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        TwitterUserDto user = (TwitterUserDto) obj;
        return this.getId().equals(user.getId());
    }

    // ID should be unique identifier, so let's use if for generate hashCode
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}