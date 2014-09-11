/******************************************************/

/****** TABLES, ordered by decreasing dependency ******/

/******************************************************/

/****** Object:  Table [dbo].[CM_MDB_MODEL_FEATURE] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CM_MDB_MODEL_FEATURE]') AND type in (N'U'))
DROP TABLE [dbo].[CM_MDB_MODEL_FEATURE]

/****** Object:  Table [dbo].[CM_MDB_MODEL_AUDIT] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CM_MDB_MODEL_AUDIT]') AND type in (N'U'))
DROP TABLE [dbo].[CM_MDB_MODEL_AUDIT]

/****** Object:  Table [dbo].[CM_MDB_MODEL_PROPERTY] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CM_MDB_MODEL_PROPERTY]') AND type in (N'U'))
DROP TABLE [dbo].[CM_MDB_MODEL_PROPERTY]

/****** Object:  Table [dbo].[CMT_OABA_BATCHJOB_AUDIT] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CMT_OABA_BATCHJOB_AUDIT]') AND type in (N'U'))
DROP TABLE [dbo].[CMT_OABA_BATCHJOB_AUDIT]

/****** Object:  Table [dbo].[CMT_TRANSITIVITYJOB_AUDIT] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CMT_TRANSITIVITYJOB_AUDIT]') AND type in (N'U'))
DROP TABLE [dbo].[CMT_TRANSITIVITYJOB_AUDIT]

/****** Object:  Table [dbo].[CMT_TRANSITIVITYJOB_TIMESTAMPS] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CMT_TRANSITIVITYJOB_TIMESTAMPS]') AND type in (N'U'))
DROP TABLE [dbo].[CMT_TRANSITIVITYJOB_TIMESTAMPS]
GO

/****** Object:  Table [dbo].[CM_MDB_MODEL] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CM_MDB_MODEL]') AND type in (N'U'))
DROP TABLE [dbo].[CM_MDB_MODEL]

/****** Object:  Table [dbo].[CMT_OABA_BATCH_PARAMS] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CMT_OABA_BATCH_PARAMS]') AND type in (N'U'))
DROP TABLE [dbo].[CMT_OABA_BATCH_PARAMS]

/****** Object:  Table [dbo].[CMP_AUDIT] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CMP_AUDIT]') AND type in (N'U'))
DROP TABLE [dbo].[CMP_AUDIT]

/****** Object:  Table [dbo].[CMP_BATCH_PARAMS] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CMP_BATCH_PARAMS]') AND type in (N'U'))
DROP TABLE [dbo].[CMP_BATCH_PARAMS]

/****** Object:  Table [dbo].[CMP_BATCH_JOB] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CMP_BATCH_JOB]') AND type in (N'U'))
DROP TABLE [dbo].[CMP_BATCH_JOB]

/****** Object:  Table [dbo].[CMP_MODEL] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CMP_MODEL]') AND type in (N'U'))
DROP TABLE [dbo].[CMP_MODEL]

/****** Object:  Table [dbo].[CMT_OABA_STATUS_LOG] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CMT_OABA_STATUS_LOG]') AND type in (N'U'))
DROP TABLE [dbo].[CMT_OABA_STATUS_LOG]

/****** Object:  Table [dbo].[CMT_SEQUENCE] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CMT_SEQUENCE]') AND type in (N'U'))
DROP TABLE [dbo].[CMT_SEQUENCE]

/****** Object:  Table [dbo].[CMT_TRANSITIVITY_JOB] ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[CMT_TRANSITIVITY_JOB]') AND type in (N'U'))
DROP TABLE [dbo].[CMT_TRANSITIVITY_JOB]
GO
