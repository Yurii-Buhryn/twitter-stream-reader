package org.interview.oauth.twitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth.*;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import java.io.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.interview.oauth.twitter.dto.TwitterMessageDto;
import org.interview.oauth.twitter.dto.TwitterUserDto;
import org.interview.oauth.twitter.util.TwitterJsonParser;

public class TwitterAuthenticator {

    private static final HttpTransport TRANSPORT = new NetHttpTransport();

    private static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
    private static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
    private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    private static final String STREAM_API_URL = "https://stream.twitter.com/1.1/statuses/filter.json";

    private static final String DELIMITED_API_PARAM_NAME = "delimited";
    private static final String DELIMITED_API_PARAM_VALUE = "length";

    private static final String TRACK_API_PARAM_NAME = "track";
    private static final String TRACK_API_PARAM_VALUE = "bieber";

    private final PrintStream out;
    private final String consumerKey;
    private final String consumerSecret;
    private HttpRequestFactory factory;

    public TwitterAuthenticator(final PrintStream out, final String consumerKey, final String consumerSecret) {
        this.out = out;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    public synchronized SortedMap<TwitterUserDto, TreeSet<TwitterMessageDto>> getStream() throws TwitterAuthenticationException, IOException {
        if (factory != null) {
            getAuthorizedHttpRequestFactory();
        }

        GenericUrl streamUrl = new GenericUrl(STREAM_API_URL)
                .set(DELIMITED_API_PARAM_NAME, DELIMITED_API_PARAM_VALUE)
                .set(TRACK_API_PARAM_NAME, TRACK_API_PARAM_VALUE);


        InputStream in = factory.buildGetRequest(streamUrl).execute().getContent();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;

        Boolean ifJson = false;
        Integer readCount = 0;
        LocalTime startTime = LocalTime.now();

        ObjectMapper mapper = new ObjectMapper();
        TwitterJsonParser twitterJsonParser = new TwitterJsonParser();
        SortedMap<TwitterUserDto, TreeSet<TwitterMessageDto>> results = new TreeMap<>((obj1, obj2) -> obj1.compareTo(obj2));

        while ((line = reader.readLine()) != null) {
            if (ifJson) {
                JsonNode messageJson = mapper.readTree(line);

                TwitterMessageDto message = twitterJsonParser.getMessage(messageJson);
                TwitterUserDto user = twitterJsonParser.getUser(messageJson.get("user"));

                if (results.containsKey(user)) {
                    results.get(user).add(message);
                } else {
                    TreeSet<TwitterMessageDto> messages = new TreeSet<>((obj1, obj2) -> obj1.compareTo(obj2));
                    messages.add(message);

                    results.put(user, messages);
                }

                readCount++;
                ifJson = false;
            } else {
                ifJson = true;
            }

            long runTime = startTime.until(LocalTime.now(), ChronoUnit.SECONDS);

            if (readCount >= 100 || runTime >= 30) {
                break;
            }

            out.println("Loading " + runTime + " seconds ...");
        }

        reader.close();

        return results;
    }

    public synchronized HttpRequestFactory getAuthorizedHttpRequestFactory() throws TwitterAuthenticationException {
        if (factory != null) {
            return factory;
        }

        OAuthHmacSigner signer = new OAuthHmacSigner();

        signer.clientSharedSecret = consumerSecret;

        OAuthGetTemporaryToken requestToken = new OAuthGetTemporaryToken(REQUEST_TOKEN_URL);
        requestToken.consumerKey = consumerKey;
        requestToken.transport = TRANSPORT;
        requestToken.signer = signer;

        OAuthCredentialsResponse requestTokenResponse;
        try {
            requestTokenResponse = requestToken.execute();
        } catch (IOException e) {
            throw new TwitterAuthenticationException("Unable to aquire temporary token: " + e.getMessage(), e);
        }

        out.println("Aquired temporary token...\n");

        signer.tokenSharedSecret = requestTokenResponse.tokenSecret;

        OAuthAuthorizeTemporaryTokenUrl authorizeUrl = new OAuthAuthorizeTemporaryTokenUrl(AUTHORIZE_URL);
        authorizeUrl.temporaryToken = requestTokenResponse.token;

        String providedPin;
        Scanner scanner = new Scanner(System.in);
        try {
            out.println("Go to the following link in your browser:\n" + authorizeUrl.build());
            out.println("\nPlease enter the retrieved PIN:");
            providedPin = scanner.nextLine();
        } finally {
            scanner.close();
        }

        if (providedPin == null) {
            throw new TwitterAuthenticationException("Unable to read entered PIN");
        }

        OAuthGetAccessToken accessToken = new OAuthGetAccessToken(ACCESS_TOKEN_URL);
        accessToken.verifier = providedPin;
        accessToken.consumerKey = consumerSecret;
        accessToken.signer = signer;
        accessToken.transport = TRANSPORT;
        accessToken.temporaryToken = requestTokenResponse.token;


        OAuthCredentialsResponse accessTokenResponse;
        try {
            accessTokenResponse = accessToken.execute();
        } catch (IOException e) {
            throw new TwitterAuthenticationException("Unable to authorize access: " + e.getMessage(), e);
        }
        out.println("\nAuthorization was successful");

        signer.tokenSharedSecret = accessTokenResponse.tokenSecret;

        OAuthParameters parameters = new OAuthParameters();
        parameters.consumerKey = consumerKey;
        parameters.token = accessTokenResponse.token;
        parameters.signer = signer;

        factory = TRANSPORT.createRequestFactory(parameters);

        return factory;
    }
}



