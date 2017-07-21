package log4j.appender;

import org.apache.log4j.helpers.LogLog;

import javax.mail.MessagingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EmailDispatcher implements Runnable {
    private final BlockingQueue<AccumulatorValue> queue = new LinkedBlockingQueue<AccumulatorValue>();
    private final EmailSender emailSender;

    public EmailDispatcher(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void add(AccumulatorValue value) {
        queue.add(value);
    }

    public void run() {
        boolean interrupted = false;
        while (!interrupted) {
            try {
                AccumulatorValue value = queue.take();
                emailSender.sendEmail(value);
            } catch (InterruptedException e) {
                interrupted = true;
            } catch (MessagingException e) {
                LogLog.error("An error occured while sending e-mail notification", e);
            }
        }
    }
}
