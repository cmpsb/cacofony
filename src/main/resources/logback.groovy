import ch.qos.logback.classic.encoder.PatternLayoutEncoder

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "[%date{yyyy-MM-dd HH:mm:ss XX}] [%logger{40}] [%level] %message\n"
    }
}
root(INFO, ["STDOUT"])

logger("net.wukl.cacofony", DEBUG)
