package log4j.appender;

import org.apache.log4j.spi.LoggingEvent;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class AccumulatorValue {
    private final long firstEventTimestamp;
    private final List<LoggingEvent> events = new LinkedList<LoggingEvent>();
    private Long eventCount;

    public AccumulatorValue(LoggingEvent event) {
        events.add(event);
        firstEventTimestamp = event.getTimeStamp();
        eventCount = 1L;
    }

    synchronized public void update(LoggingEvent event) {
        events.add(event);
        eventCount++;
    }

    public List<LoggingEvent> getEvents() {
        return events;
    }

    public long getEventCount() {
        return eventCount;
    }

    public Date getFirstEventTimestamp() {
        return new Date(firstEventTimestamp);
    }
}
