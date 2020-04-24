package com.hiclab.rcptt.reporting.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rcptt.reporting.util.ReportUtils;

import com.hiclab.rcptt.reporting.XMLCustomReportGenerator;

/**
 * Represents a test suite used for testing.
 * 
 * @author Hiclab
 *
 */
public class Suite {

	private List<Case> testCases;

	private String name;
	
	private String date;

	public Suite(String name) {
		this.name = name;
	}

	public void addTestCase(Case testcase) {
		if (testCases == null) {
			testCases = new ArrayList<Case>();
		}
		testCases.add(testcase);
	}

	public List<Case> getTestCases() {
		return testCases;
	}

	public void setTestCases(List<Case> testCases) {
		this.testCases = testCases;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		int time = 0;
		if (testCases != null) {
			time = testCases.stream().map(t -> Integer.parseInt(t.getTime().replace(".", ""))).reduce(0, Integer::sum);
		}
		return ReportUtils.formatTime(time);
	}

	public String getPassed() {
		long passed = 0;
		if (testCases != null) {
			passed = testCases.stream().filter(t -> XMLCustomReportGenerator.RESULT_PASSED.equals(t.getResult()))
					.count();
		}
		return String.valueOf(passed);
	}

	public String getFailed() {
		long failed = 0;
		if (testCases != null) {
			failed = testCases.stream().filter(t -> XMLCustomReportGenerator.RESULT_FAILED.equals(t.getResult()))
					.count();
		}
		return String.valueOf(failed);
	}

	public String getSkipped() {
		long skipped = 0;
		if (testCases != null) {
			skipped = testCases.stream().filter(t -> XMLCustomReportGenerator.RESULT_SKIPPED.equals(t.getResult()))
					.count();
		}
		return String.valueOf(skipped);
	}

	public String getTests() {
		int tests = 0;
		if (testCases != null) {
			tests = testCases.size();
		}
		return String.valueOf(tests);
	}

}
