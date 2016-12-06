package org.interview.oauth.twitter.dto;

import org.joda.time.DateTime;

public class TwitterUserDto implements Comparable {
    private String id;
    private DateTime createdAt;
    private String name;
    private String screenName;

    public TwitterUserDto() {
    }

    public TwitterUserDto(final String id, final String name, final DateTime createdAt, final String screenName) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.screenName = screenName;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final DateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(final String screenName) {
        this.screenName = screenName;
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

    @Override
    public int compareTo(final Object obj) {
        TwitterUserDto user = (TwitterUserDto) obj;
        return this.createdAt.compareTo(user.getCreatedAt());
    }
}
