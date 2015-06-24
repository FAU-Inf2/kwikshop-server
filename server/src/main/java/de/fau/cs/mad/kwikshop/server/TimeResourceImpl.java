package de.fau.cs.mad.kwikshop.server;

import de.fau.cs.mad.kwikshop.common.Time;
import de.fau.cs.mad.kwikshop.common.TimeResource;

import java.util.Random;

/**
 * Created by Andreas Kumlehn on 3/30/15.
 */
public class TimeResourceImpl implements TimeResource {

    private static final Random RANDOM = new Random();

    @Override
    public Time now() {
        return new Time(System.currentTimeMillis());
    }

    @SuppressWarnings({"finally", "ReturnInsideFinallyBlock"})
    @Override
    public Time withDelay() {
        try {
            Thread.sleep(TimeResource.MIN_DELAY + RANDOM.nextInt(TimeResource.MAX_DELAY - TimeResource.MIN_DELAY));
        } catch (InterruptedException e) {
            System.out.println("Dead.");
        } finally {
            return new Time(System.currentTimeMillis());
        }
    }
}
