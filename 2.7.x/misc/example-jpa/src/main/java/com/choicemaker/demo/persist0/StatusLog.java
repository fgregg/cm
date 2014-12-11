package com.choicemaker.demo.persist0;

public interface StatusLog {

	public static final int DEFAULT_VERSION = 271;

	public abstract long getJobId();

	public abstract String getJobType();

	public abstract void setJobType(String jobType);

	public abstract int getStatusId();

	public abstract void setStatusId(int statusId);

	public abstract int getVersion();

	public abstract void setVersion(int version);

	public abstract String getInfo();

	public abstract void setInfo(String info);

}