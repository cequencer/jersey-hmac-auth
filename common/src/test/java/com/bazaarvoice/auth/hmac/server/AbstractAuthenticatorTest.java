package com.bazaarvoice.auth.hmac.server;

import com.bazaarvoice.auth.hmac.common.Credentials;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.bazaarvoice.auth.hmac.common.TimeUtils.nowInUTC;
import static com.bazaarvoice.auth.hmac.server.TestCredentials.createCredentials;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class AbstractAuthenticatorTest {
    private static final String API_KEY = "api-key";
    private static final String SECRET_KEY = "secret-key";
    private static final String PRINCIPAL = "principal";

    private final AbstractAuthenticator<String> authenticator = createAuthenticator();

    @Test
    public void respondsToValidCredentialsWithPrincipal() {
        Credentials credentials = createCredentials(API_KEY, SECRET_KEY);
        String principal = authenticator.authenticate(credentials);
        assertNotNull(principal);
        assertEquals(PRINCIPAL, principal);
    }

    @Test
    public void respondsToExpiredPastTimestampWithNull() {
        DateTime requestTime = nowInUTC().minusMinutes(1);
        Credentials credentials = createCredentials(API_KEY, SECRET_KEY, requestTime);
        String principal = authenticator.authenticate(credentials);
        assertNull(principal);
    }

    @Test
    public void respondsToExpiredFutureTimestampWithNull() {
        DateTime requestTime = nowInUTC().plusMinutes(1);
        Credentials credentials = createCredentials(API_KEY, SECRET_KEY, requestTime);
        String principal = authenticator.authenticate(credentials);
        assertNull(principal);
    }

    @Test
    public void respondsToInvalidSignatureWithNull() {
        Credentials credentials = createCredentials(API_KEY, SECRET_KEY + "-invalid");
        String principal = authenticator.authenticate(credentials);
        assertNull(principal);
    }

    private AbstractAuthenticator<String> createAuthenticator() {
        // Implement an authenticator that allows a 30 second difference between client and server timestamps
        return new AbstractAuthenticator<String>(30, TimeUnit.SECONDS) {
            @Override
            protected String getPrincipal(Credentials credentials) {
                return PRINCIPAL;
            }

            @Override
            protected String getSecretKeyFromPrincipal(String s) {
                return SECRET_KEY;
            }
        };
    }
}
