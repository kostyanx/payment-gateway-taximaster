-- language=H2
CREATE TABLE CAR_DRIVER_INFO (
    CREW_ID INT NOT NULL PRIMARY KEY,
    CAR_ID INT NOT NULL,
    DRIVER_ID INT NOT NULL,
    CODE VARCHAR(10) NOT NULL,
    FIO VARCHAR(100) NOT NULL,
    BALANCE DECIMAL(18, 2) NOT NULL DEFAULT 0.00
);
-- language=H2
CREATE INDEX CAR_DRIVER_INFO_INDEX_CODE ON CAR_DRIVER_INFO(CODE);
-- language=H2
CREATE SEQUENCE PAYMENTS_SEQUENCE_ID;
-- language=H2
CREATE TABLE PAYMENTS (
    ID INT NOT NULL DEFAULT PAYMENTS_SEQUENCE_ID.NEXTVAL PRIMARY KEY,
    SOURCE_TYPE ENUM('SBERBANK_CASH', 'SBERBANK_CASHLESS') NOT NULL,
    PAY_ID VARCHAR(100) NOT NULL,
    RECEIVER VARCHAR(20) NOT NULL,
    AMOUNT DECIMAL(18, 2) NOT NULL,
    PAY_TIMESTAMP TIMESTAMP NOT NULL DEFAULT NOW(),
    REQUEST_ID VARCHAR(40) NOT NULL,
    OPER_ID INT,
    STATUS ENUM('NEW', 'SUCCESS', 'RETRY', 'ERROR') DEFAULT 'NEW',
    COUNTER INT NOT NULL DEFAULT 0,
    ERROR_MESSAGE TEXT,
    INSERTED TIMESTAMP NOT NULL DEFAULT NOW(),
    UPDATED TIMESTAMP NOT NULL DEFAULT NOW()
);
-- language=H2
CREATE UNIQUE INDEX PAYMENTS_INDEX_SOURCE_PAY_ID ON PAYMENTS(PAY_ID, SOURCE_TYPE);
-- language=H2
CREATE INDEX PAYMENTS_INDEX_STATUS ON PAYMENTS(STATUS);
-- language=H2
CREATE SEQUENCE REQUEST_LOG_SEQUENCE_ID;
-- language=H2
CREATE TABLE REQUEST_LOG (
    ID INT NOT NULL DEFAULT REQUEST_LOG_SEQUENCE_ID.NEXTVAL PRIMARY KEY,
    REQUEST_ID VARCHAR(40) NOT NULL,
    REQUEST_URL VARCHAR(255),
    REQUEST_BODY TEXT,
    RESPONSE_CODE SMALLINT,
    RESPONSE_BODY TEXT,
    INSERTED TIMESTAMP NOT NULL DEFAULT NOW()
);
-- language=H2
CREATE INDEX REQUEST_LOG_REQUEST_ID ON REQUEST_LOG(REQUEST_ID);
-- language=H2
CREATE TABLE DRIVERS (
    DRIVER_ID INT NOT NULL PRIMARY KEY,
    FIO VARCHAR(100) NOT NULL,
    BALANCE DECIMAL(18, 2) NOT NULL DEFAULT 0.00
);


