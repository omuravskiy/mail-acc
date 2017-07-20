package log4j.appender;

import java.util.TimerTask;

public class SendEmailTask extends TimerTask {
    private final AccumulatorValue accumulatorValue;
    private final AccumulatingSmtpAppender accumulatingSmtpAppender;

    public SendEmailTask(AccumulatorValue accumulatorValue,
                         AccumulatingSmtpAppender accumulatingSmtpAppender) {
        this.accumulatorValue = accumulatorValue;
        this.accumulatingSmtpAppender = accumulatingSmtpAppender;
    }

    public void run() {
        accumulatingSmtpAppender.removeValue(accumulatorValue);
        accumulatingSmtpAppender.emailDispatcher.add(accumulatorValue);
    }
}
