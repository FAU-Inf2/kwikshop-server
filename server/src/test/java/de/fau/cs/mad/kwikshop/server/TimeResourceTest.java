package de.fau.cs.mad.kwikshop.server;

import de.fau.cs.mad.kwikshop.common.Time;
import de.fau.cs.mad.kwikshop.common.TimeResource;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import java.io.IOException;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class TimeResourceTest {
    @ClassRule
    public static final DropwizardClientRule RULE = new DropwizardClientRule(new TimeResourceImpl());

    private static TimeResource getEndpoint() {
        WebTarget target = ClientBuilder.newClient().register(JacksonJsonProvider.class).target(RULE.baseUri());
        return WebResourceFactory.newResource(TimeResource.class, target);
    }

    @Test(timeout = 500)
    public void timeNow() throws IOException {
        TimeResource endpoint = getEndpoint();
        long before = System.currentTimeMillis();
        Time t = endpoint.now();
        long after = System.currentTimeMillis();
        assertTrue(before <= t.getTimestamp() && t.getTimestamp() <= after);
    }

    @Test(timeout = 12500)
    public void timeWithDelay() throws IOException {
        TimeResource endpoint = getEndpoint();
        long before = System.currentTimeMillis();
        Time t = endpoint.withDelay();
        long after = System.currentTimeMillis();
        assertTrue(before <= t.getTimestamp() && t.getTimestamp() <= after && after - before >= TimeResourceImpl.MIN_DELAY);
    }
}
