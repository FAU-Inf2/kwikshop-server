package de.fau.cs.mad.kwikshop.server;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.math.BigInteger;
import java.security.SecureRandom;

import io.dropwizard.jackson.Jackson;

public class TokenHandler {

    private SecureRandom random = new SecureRandom();

    // TODO: change client_id
    private static String client_id = "974373376910-pp2n1j7jd93evqpljt47s8r2k2a6rkha.apps.googleusercontent.com";

    // Google OAuth validation result
    private static class GoogleOAuthResult {
        public String issuer;
        public String issued_to;
        public String audience;
        public String user_id;
        public int expires_in;
        public long issued_at;
        public String email;
        public boolean email_verified;
    }

    /* Validate the token */
    public static String TokenCheck(String tokenString) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("https://www.googleapis.com/oauth2/v1/tokeninfo?id_token="+tokenString);
        GoogleOAuthResult result;

        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            result = Jackson.newObjectMapper().readValue(json, GoogleOAuthResult.class);

            // Expired token?
            if(result.expires_in < 0)
                return null;

            // issued_to must match
            if(!result.issued_to.equals(client_id))
                return null;
        } catch (Exception e) {
            // Invalid token / check failed
            return null;
        }

        return result.user_id;
    }

    // Generates a Kwik Shop session token
    public String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }

}
