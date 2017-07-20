package log4j.appender;

import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

public class AccumulatorKey {

    private String name;
    private String lineNumber;

    public AccumulatorKey(LoggingEvent event) {
        LocationInfo locationInfo = event.getLocationInformation();
        name = locationInfo.getFileName();
        lineNumber = locationInfo.getLineNumber();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccumulatorKey that = (AccumulatorKey) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return lineNumber != null ? lineNumber.equals(that.lineNumber) : that.lineNumber == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (lineNumber != null ? lineNumber.hashCode() : 0);
        return result;
    }
}
