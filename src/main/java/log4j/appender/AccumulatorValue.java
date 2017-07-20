package log4j.appender;

import org.apache.log4j.spi.LoggingEvent;

import java.util.Date;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class AccumulatorValue {
    private final long firstEventTimestamp;
    private AtomicReference<LoggingEvent> lastEvent;
    private AtomicLong eventCount = new AtomicLong(1L);

    public AccumulatorValue(LoggingEvent event, AccumulatingSmtpAppender accumulatingSmtpAppender) {
        this.lastEvent = new AtomicReference<LoggingEvent>(event);
        firstEventTimestamp = event.getTimeStamp();
        Timer timer = new Timer();
        timer.schedule(new SendEmailTask(this, accumulatingSmtpAppender), TimeUnit.SECONDS.toMillis(5L));
    }

    public synchronized void update(LoggingEvent event) {
        lastEvent.set(event);
        eventCount.incrementAndGet();
    }

    public LoggingEvent getLastEvent() {
        return lastEvent.get();
    }

    public Long getEventCount() {
        return eventCount.get();
    }

    public Date getFirstEventTimestamp() {
        return new Date(firstEventTimestamp);
    }
}
