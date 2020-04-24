package com.hiclab.rcptt.reporting.tests;

import org.eclipse.rcptt.reporting.util.ReportUtils;

/**
 * Represents a test case used for testing.
 *
 * @author Hiclab
 *
 */
public class Case {

	private String name;
	private String time = ReportUtils.formatTime(0);
	private String tags;
	private String description;
	private String result;
	private String failureMessage;

	public Case(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}

}
