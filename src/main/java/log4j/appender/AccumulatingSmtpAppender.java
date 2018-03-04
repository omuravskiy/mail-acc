package log4j.appender;

import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.LoggingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class AccumulatingSmtpAppender extends SMTPAppender {

    private final Map<AccumulatorKey, AccumulatorValue> buffer = new HashMap<AccumulatorKey, AccumulatorValue>();
    public static final long EMAIL_SEND_INTERVAL = TimeUnit.SECONDS.toMillis(61L);
    private EmailDispatcher emailDispatcher;
    private boolean activated = false;

    public AccumulatingSmtpAppender() {
        super();
    }

    @Override
    public void activateOptions() {
        super.activateOptions();
        super.cb = null;
        EmailSender emailSender = new EmailSender(super.layout, super.msg);
        emailDispatcher = new EmailDispatcher(emailSender);
        new Thread(emailDispatcher).start();
        activated = true;
    }

    @Override
    public void append(LoggingEvent event) {
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
        final AccumulatorKey key = new AccumulatorKey(event);
        synchronized (buffer) {
            if (buffer.containsKey(key)) {
                AccumulatorValue accumulatorValue = buffer.get(key);
                buffer.put(key, accumulatorValue.update(event));
            }
            else {
                final AccumulatorValue value = new AccumulatorValue(event);
                buffer.put(key, value);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    public void run() {
                        synchronized (buffer) {
                            AccumulatorValue accumulatorValue = buffer.remove(key);
                            emailDispatcher.add(accumulatorValue);
                        }
                    }
                }, EMAIL_SEND_INTERVAL);
            }
        }
    }

    @Override
    protected boolean checkEntryConditions() {
        return activated;
    }

    @Override
    public void close() {
        closed = true;
    }
}
