package org.interview.oauth.twitter;

import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import org.interview.oauth.twitter.dto.TwitterUserDto;

public class MainClass {

    public static void main(String[] args) throws TwitterAuthenticationException, IOException, ParseException {
        final PrintStream out = new PrintStream(System.out);
        final String consumerKey = "vp8qXAMoZzy6jowJdtouPLUUb";
        final String consumerSecret = "IMx3eIRfXXbRimoIz7cNpZCl0dr9dYEdRuDVTr2C4LdResXjN7";

        TwitterAuthenticator twitterAuthenticator = new TwitterAuthenticator(out, consumerKey, consumerSecret);

        twitterAuthenticator.getAuthorizedHttpRequestFactory();

        twitterAuthenticator.getStream().entrySet().forEach(obj -> {
            System.out.println("----------------------------------------------");
            TwitterUserDto user = obj.getKey();
            System.out.println("USER : " + user.getScreenName());
            System.out.println("CREATED_AT : " + user.getCreatedAt().toString());
            System.out.println("MESSAGES : ");

            obj.getValue().forEach(message -> {
                System.out.println("date : " + message.getCreatedAt().toString());
                System.out.println("text : " + message.getText());
            });
        });
    }

}
