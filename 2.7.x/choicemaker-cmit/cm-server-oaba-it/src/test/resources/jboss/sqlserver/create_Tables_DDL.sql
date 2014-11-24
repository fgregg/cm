CREATE TABLE CMT_OABA_BATCHJOB (
 ID NUMERIC(19) NOT NULL,
 TYPE VARCHAR(31) NULL,
 BPARENT_ID NUMERIC(19) NULL,
 DESCRIPTION VARCHAR(255) NULL,
 EXTERNAL_ID VARCHAR(255) NULL,
 PARAMS_ID NUMERIC(19) NULL,
 FRACTION_COMPLETE INTEGER NULL,
 SERVER_ID NUMERIC(19) NULL,
 SETTINGS_ID NUMERIC(19) NULL,
 STATUS VARCHAR(255) NULL,
 TRANSACTION_ID NUMERIC(19) NULL,
 URM_ID NUMERIC(19) NULL,
 WORKING_DIR VARCHAR(255) NULL,
 PRIMARY KEY (ID)
);

CREATE TABLE CMT_OABA_BATCH_PARAMS (
 ID NUMERIC(19) NOT NULL,
 HIGH_THRESHOLD FLOAT(16) NULL,
 LOW_THRESHOLD FLOAT(16) NULL,
 MASTER_RS IMAGE NULL,
 STAGE_MODEL VARCHAR(255) NULL,
 STAGE_RS IMAGE NULL,
 PRIMARY KEY (ID)
);

CREATE TABLE CMT_OABA_PROCESSING (
 ID NUMERIC(19) NOT NULL,
 EVENT_ID INTEGER NULL,
 INFO VARCHAR(255) NULL,
 JOB_ID NUMERIC(19) NULL,
 TIMESTAMP DATETIME NULL,
 JOB_TYPE VARCHAR(255) NULL,
 VERSION NUMERIC(19) NULL,
 PRIMARY KEY (ID)
);

CREATE TABLE CMT_SERVER_CONFIG (
 ID NUMERIC(19) NOT NULL,
 FILE_URI VARCHAR(255) NULL,
 HOST_NAME VARCHAR(255) NULL,
 MAX_CHUNK_COUNT INTEGER NULL,
 MAX_CHUNK_SIZE INTEGER NULL,
 MAX_THREADS INTEGER NULL,
 CONFIG_NAME VARCHAR(255) NULL,
 UUID VARCHAR(255) NULL,
 PRIMARY KEY (ID)
);

CREATE TABLE CMT_DEFAULT_SERVER_CONFIG (
 HOST_NAME VARCHAR(255) NOT NULL,
 SERVER_CONFIG NUMERIC(19) NULL,
 PRIMARY KEY (HOST_NAME)
);

CREATE TABLE CMT_ABA_SETTINGS (
 ID NUMERIC(19) NOT NULL,
 TYPE VARCHAR(31) NULL,
 LIMIT_BLOCKSET INTEGER NULL,
 LIMIT_SINGLESET INTEGER NULL,
 LIMIT_SINGLETABLE INTEGER NULL,
 INTERVAL INTEGER NULL,
 MAX_BLOCKSIZE INTEGER NULL,
 MAX_CHUNKSIZE INTEGER NULL,
 MAX_OVERSIZE INTEGER NULL,
 MAX_SINGLE INTEGER NULL,
 MIN_FIELDS INTEGER NULL,
 PRIMARY KEY (ID)
);

CREATE TABLE CMT_DEFAULT_SETTINGS (
 SETTINGS_ID NUMERIC(19) NULL,
 MODEL VARCHAR(255) NOT NULL,
 BLOCKING_CONFIG VARCHAR(255) NOT NULL,
 DATABASE_CONFIG VARCHAR(255) NOT NULL,
 TYPE VARCHAR(255) NOT NULL,
 PRIMARY KEY (MODEL, BLOCKING_CONFIG, DATABASE_CONFIG, TYPE)
);

CREATE TABLE CMT_OABA_BATCHJOB_AUDIT (
 BATCHJOB_ID NUMERIC(19) NULL,
 STATUS VARCHAR(255) NULL,
 TIMESTAMP DATETIME NULL
);

ALTER TABLE CMT_OABA_BATCHJOB_AUDIT
 ADD CONSTRAINT CMTBBTCHJBAUDITBTCHJBD
 FOREIGN KEY (BATCHJOB_ID)
 REFERENCES CMT_OABA_BATCHJOB (ID);

CREATE TABLE CMT_SEQUENCE (
 SEQ_NAME VARCHAR(50) NOT NULL,
 SEQ_COUNT NUMERIC(28) NULL,
 PRIMARY KEY (SEQ_NAME)
);

INSERT INTO CMT_SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('OABA_BATCHPARAMS', 0);

INSERT INTO CMT_SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('SERVER_CONFIG', 0);

INSERT INTO CMT_SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('ABA_SETTINGS', 0);

INSERT INTO CMT_SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('BATCHJOB', 0);

INSERT INTO CMT_SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('OABA_PROCESSING', 0);

