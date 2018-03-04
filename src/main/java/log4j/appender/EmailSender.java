package log4j.appender;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;

public class EmailSender {
    private final Layout layout;
    private final Message message;

    public EmailSender(Layout layout, Message message) {
        this.layout = layout;
        this.message = message;
    }

    public void sendEmail(AccumulatorValue value) throws MessagingException {
        Multipart mp = new MimeMultipart();

        if (value.getEventCount() > 1) {
            StringBuilder aggregateText = prepareAggregateText(value);
            MimeBodyPart eventPart = encodeMimePart(aggregateText);
            mp.addBodyPart(eventPart);
        }

        StringBuilder eventText = prepareEventText(value.getEvents());
        MimeBodyPart eventPart = encodeMimePart(eventText);
        mp.addBodyPart(eventPart);

        synchronized (message) {
            message.setContent(mp);
            message.setSentDate(new Date());
            Transport.send(message);
        }
    }

    private StringBuilder prepareAggregateText(AccumulatorValue value) {
        return new StringBuilder("The following error had occured ")
                .append(value.getEventCount())
                .append(" times.\r\n")
                .append("The first occurence was on ")
                .append(value.getFirstEventTimestamp())
                .append("\r\n\r\n");
    }

    private MimeBodyPart encodeMimePart(StringBuilder emailText) throws MessagingException {
        MimeBodyPart bodyPart;
        try {
            bodyPart = encodeEventPart(emailText.toString(), layout.getContentType());
        } catch (Exception e) {
            for (int i = 0; i < emailText.length(); i++) {
                if (emailText.charAt(i) >= 0x80) {
                    emailText.setCharAt(i, '?');
                }
            }
            bodyPart = new MimeBodyPart();
            bodyPart.setContent(emailText.toString(), layout.getContentType());
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

    private StringBuilder prepareEventText(List<LoggingEvent> events) {
        StringBuilder stringBuilder = new StringBuilder();

        String header = layout.getHeader();
        if (header != null) stringBuilder.append(header);

        for (LoggingEvent event : events) {
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
        }

        String footer = layout.getFooter();
        if (footer != null) {
            stringBuilder.append(footer);
        }

        return stringBuilder;
    }
}
