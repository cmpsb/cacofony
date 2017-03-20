import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import ch.qos.logback.classic.Level

import java.text.SimpleDateFormat

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "[%date{yyyy-MM-dd HH:mm:ss XX}] [%logger{40}] [%level] %message\n"
    }
}
root(Level.INFO, ["STDOUT"])

logger("net.cmpsb.cacofony", Level.DEBUG)
