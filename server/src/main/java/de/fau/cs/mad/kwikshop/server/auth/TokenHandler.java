package de.fau.cs.mad.kwikshop.server.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

public class TokenHandler {

    private SecureRandom random = new SecureRandom();

    // TODO: change client_id
    private static String ANDROID_CLIENT_ID = "974373376910-pp2n1j7jd93evqpljt47s8r2k2a6rkha.apps.googleusercontent.com";
    private static String SERVER_CLIENT_ID = "974373376910-mg6fm7feie2rn0v9qj2nmi1jpeftr47u.apps.googleusercontent.com";

    /* Validate the token, return user_id if the token is valid */
    public static String TokenCheck(String tokenString) {
        if(tokenString == null)
            return null;

        ApacheHttpTransport transport = new ApacheHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Arrays.asList(SERVER_CLIENT_ID))
                        .build();

        GoogleIdToken idToken = null;

        try {
            idToken = verifier.verify(tokenString); // "The GoogleIdTokenVerifier.verify() method verifies the JWT signature, the aud claim, the iss claim, and the exp claim."
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (idToken != null) {
            Payload payload = idToken.getPayload();
            if (Arrays.asList(ANDROID_CLIENT_ID).contains(payload.getAuthorizedParty())) {
                System.out.println("User ID: " + payload.getSubject());
                return payload.getSubject();
            } else {
                System.out.println("Invalid ID token.");
            }
        } else {
            System.out.println("ID token is null.");
        }

        return null;
    }

    // Generates a Kwik Shop session token
    public String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }

}
