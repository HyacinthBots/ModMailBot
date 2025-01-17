import ch.qos.logback.core.joran.spi.ConsoleTarget

def environment = System.getenv().getOrDefault("ENVIRONMENT", "production")

def defaultLevel = INFO
def defaultTarget = ConsoleTarget.SystemOut
def loggers = ["CONSOLE"]

if (environment == "development") {
    defaultLevel = DEBUG
    defaultTarget = ConsoleTarget.SystemErr

    // Silence warning about missing native PRNG
    logger("io.ktor.util.random", ERROR)
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%boldMagenta(%d{dd-MM-yyyy HH:mm:ss}) %gray(|) %boldCyan(%-30.-30thread) %gray(|) %highlight(%-5level) %gray(|) %boldGreen(%-40.40logger{40}) %gray(|) %msg%n"

        withJansi = true
    }

    target = defaultTarget
}

root(defaultLevel, loggers)
