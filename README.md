# payment-gateway-taximaster - платежный шлюз для программы Такси Мастер

Позволяет принимать платежи через Сбербанк (и далее список будет расширяться)
в программу Такси Мастер через их открытое API http://help.taximaster.ru/index.php/TM_API

Сайт разработчика Такси Мастер https://www.taximaster.ru/

Работает как сервис, достаточно настроить и прокинуть соотвествующий порт,
не нужно промежуточных серверов apache/nginx/php и прочей нечисти

Написан целиком на Kotlin, работает на JVM (Java), т.е. может запускаться
и успешно работать на любой операционной системе - Windows/Linux/Mac

Для запуска необходима установленная Java 15 (Java Runtime Environment, JRE версии 15 и выше).
Для запуска необходимо запустить файл run (для Linux) или run.bat (для Windows).
Настройки находятся в файлах application.conf (настройки веб сервера) и application.yaml.
В application.yaml необходимо указать secret для доступа к API такси мастера,
а также в application.conf необходимо указать настройки для загрузки сертификата для общения
со сбербанком, секция ktor.security.ssl

Настройка SSL здесь https://ktor.io/docs/ssl.html


Для запуска задач по расписанию используется библиотека https://github.com/shyiko/skedule,
поэтому формат настроек расписания такой же, как и в этой библиотеке.