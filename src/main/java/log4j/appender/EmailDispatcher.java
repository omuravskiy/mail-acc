package log4j.appender;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EmailDispatcher implements Runnable {
    private final BlockingQueue<AccumulatorValue> queue = new LinkedBlockingQueue<AccumulatorValue>();
    private final Appender appender;
    private final Session session;

    public EmailDispatcher(Appender appender) {
        this.appender = appender;
        session = createSession();
    }

    private Session createSession() {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", "mailhost.ripe.net");
        props.setProperty("mail.from", "unread@ripe.net");
        return Session.getDefaultInstance(props);
    }

    public void add(AccumulatorValue value) {
        queue.add(value);
    }

    public void run() {
        boolean interrupted = false;
        while (!interrupted) {
            try {
                AccumulatorValue value = queue.take();
                Multipart mp = new MimeMultipart();

                if (value.getEventCount() > 1) {
                    StringBuilder aggregateText = prepareAggregateText(value);
                    MimeBodyPart eventPart = encodeMimePart(aggregateText);
                    mp.addBodyPart(eventPart);
                }

                StringBuilder eventText = prepareEventText(value.getLastEvent());
                MimeBodyPart eventPart = encodeMimePart(eventText);
                mp.addBodyPart(eventPart);

                Message message = new MimeMessage(session);
                message.setRecipient(Message.RecipientType.TO, new InternetAddress("oleg@ripe.net"));
                message.setContent(mp);
                message.setSubject("An error occured");
                message.setSentDate(new Date());
                Transport.send(message);
            } catch (InterruptedException e) {
                interrupted = true;
            } catch (MessagingException e) {
                LogLog.error("An error occured while sending e-mail notification", e);
            }
        }
    }

    private StringBuilder prepareAggregateText(AccumulatorValue value) {
        return new StringBuilder("The following error had occured ")
                .append(value.getEventCount())
                .append(" times.\r\n")
                .append("The first occurence was at ")
                .append(value.getFirstEventTimestamp())
                .append("\r\n");
    }

    private MimeBodyPart encodeMimePart(StringBuilder emailText) throws MessagingException {
        MimeBodyPart bodyPart;
        try {
            bodyPart = encodeEventPart(emailText.toString(), getLayout().getContentType());
        } catch (Exception e) {
            for (int i = 0; i < emailText.length(); i++) {
                if (emailText.charAt(i) >= 0x80) {
                    emailText.setCharAt(i, '?');
                }
            }
            bodyPart = new MimeBodyPart();
            bodyPart.setContent(emailText.toString(), getLayout().getContentType());
        }
        return bodyPart;
    }

    private MimeBodyPart encodeEventPart(String text, String contentType) throws MessagingException, IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(MimeUtility.encode(os, "8bit"), "UTF-8");
        try {
            writer.write(text);
        } finally {
            writer.close();
        }

        InternetHeaders headers = new InternetHeaders();
        headers.setHeader("Content-Type", contentType + "; charset=UTF-8");
        headers.setHeader("Content-Transfer-Encoding", "8bit");
        return new MimeBodyPart(headers, os.toByteArray());
    }

    private StringBuilder prepareEventText(LoggingEvent event) {
        StringBuilder stringBuilder = new StringBuilder();

        Layout layout = getLayout();
        String header = layout.getHeader();
        if (header != null) stringBuilder.append(header);

        stringBuilder.append(layout.format(event));

        if (layout.ignoresThrowable()) {
            String[] throwableStrRep = event.getThrowableStrRep();
            if (throwableStrRep != null) {
                for (String s : throwableStrRep) {
                    stringBuilder.append(s);
                    stringBuilder.append("\r\n");
                }
            }
        }

        String footer = layout.getFooter();
        if (footer != null) {
            stringBuilder.append(footer);
        }

        return stringBuilder;
    }

    private Layout getLayout() {
        return appender.getLayout();
    }
}
