create table CMT_CHUNK_ID
(
GROUP_ID                       NUMBER NOT NULL,
SEQ_ID                       NUMBER NOT NULL,
CHUNK_ID                      NUMBER NOT NULL,
CONSTRAINT CMT_CHUNK_ID_PK PRIMARY KEY (GROUP_ID, SEQ_ID, CHUNK_ID)
);



create table CMT_REC_VAL_ID
(
GROUP_ID                       NUMBER NOT NULL,
REC_ID                       NUMBER NOT NULL,
VAL_ID                      NUMBER NOT NULL,
CONSTRAINT CMT_REC_VAL_ID_PK PRIMARY KEY (GROUP_ID, REC_ID, VAL_ID)
);


