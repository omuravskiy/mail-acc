# mail-acc: Accumulating log4j smtp appender
This is an appender for log4j version 1.2.x that accumulates all log messages with log level ERROR and sends them in batches once a minute.

## Usage
To use it just replace the standard `org.apache.log4j.net.SMTPAppender` by this one. For example, using the .properties config file:

    log4j.appender.mail=log4j.appender.AccumulatingSmtpAppender
    log4j.appender.mail.SMTPHost=smtp.example.com
    log4j.appender.mail.From=me@example.com
    log4j.appender.mail.To=you@example.com
    log4j.appender.mail.Subject=An error has happened
    log4j.appender.mail.Threshold=ERROR
    log4j.appender.mail.layout=org.apache.log4j.PatternLayout
    log4j.appender.mail.layout.ConversionPattern=%d %p [%c] - <%m>%n

It is not (yet) configurable, so if you need different behaviour, you'll have to re-compile the source :)
