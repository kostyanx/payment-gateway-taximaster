@echo on

java -Xmx512m -Dlog4j.configurationFile="log4j2.xml" -Dfile.encoding=UTF-8 -Dlog4j.console.appender=stdout_win -jar payment-gateway-taximaster-1.1.1.jar -config=application.conf %*
