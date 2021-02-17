@echo on

java -Xmx512m -Dlog4j.configurationFile="log4j2.xml" -jar payment-gateway-taximaster-1.1.0.jar -config=application.conf %*
