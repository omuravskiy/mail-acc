package log4j.appender;

import org.apache.log4j.spi.LoggingEvent;

import java.util.Date;

public class AccumulatorValue {
    private final long firstEventTimestamp;
    private final LoggingEvent lastEvent;
    private final long eventCount;

    public AccumulatorValue(LoggingEvent event) {
        this.lastEvent = event;
        firstEventTimestamp = event.getTimeStamp();
        eventCount = 1;
    }

    private AccumulatorValue(long firstEventTimestamp, LoggingEvent event, long eventCount) {
        this.firstEventTimestamp = firstEventTimestamp;
        this.lastEvent = event;
        this.eventCount = eventCount;
    }

    synchronized public AccumulatorValue update(LoggingEvent event) {
        return new AccumulatorValue(firstEventTimestamp, event, eventCount + 1);
    }

    public LoggingEvent getLastEvent() {
        return lastEvent;
    }

    public long getEventCount() {
        return eventCount;
    }

    public Date getFirstEventTimestamp() {
        return new Date(firstEventTimestamp);
    }
}
