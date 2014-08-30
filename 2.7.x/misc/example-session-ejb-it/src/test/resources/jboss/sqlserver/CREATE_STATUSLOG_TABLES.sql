CREATE TABLE CMT_OABA_STATUS_LOG (
 JOB_ID NUMERIC(19) NOT NULL,
 INFO VARCHAR(255) NULL,
 JOB_TYPE VARCHAR(255) NULL,
 STATUS_ID INTEGER NULL,
 VERSION INTEGER NULL,
 PRIMARY KEY (JOB_ID)
);

INSERT INTO CMT_SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('OABA_STATUSLOG', 0);
