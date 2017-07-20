package log4j.appender;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.ConcurrentHashMap;

public class AccumulatingSmtpAppender extends AppenderSkeleton {

    private ConcurrentHashMap<AccumulatorKey, AccumulatorValue> buffer =
            new ConcurrentHashMap<AccumulatorKey, AccumulatorValue>();

    public final EmailDispatcher emailDispatcher;

    public AccumulatingSmtpAppender() {
        super();
        emailDispatcher = new EmailDispatcher(this);
        new Thread(emailDispatcher).start();
    }

    protected void append(LoggingEvent event) {
        if (!checkEntryConditions()) {
            return;
        }

        event.getThreadName();
        event.getNDC();
        event.getMDCCopy();
        event.getLocationInformation();
        event.getRenderedMessage();
        event.getThrowableStrRep();

        accumulateEvent(event);
    }

    private void accumulateEvent(LoggingEvent event) {
        AccumulatorKey key = new AccumulatorKey(event);
        if (buffer.containsKey(key)) buffer.get(key).update(event);
        else buffer.put(key, new AccumulatorValue(event, this));
    }

    private boolean checkEntryConditions() {
        return true;
    }

    public void close() {
        closed = true;
    }

    public boolean requiresLayout() {
        return true;
    }

    public void removeValue(AccumulatorValue accumulatorValue) {
        AccumulatorKey key = new AccumulatorKey(accumulatorValue.getLastEvent());
        boolean removed = buffer.remove(key, accumulatorValue);
        if (!removed) throw new IllegalStateException("Couldn't remove event " + accumulatorValue + " with key " + key);
    }
}
