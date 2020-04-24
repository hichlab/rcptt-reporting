package com.hiclab.rcptt.reporting.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.reporting.util.internal.XMLUtils;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hiclab.rcptt.reporting.XMLCustomReportGenerator;

/**
 * Tests for {@link XMLCustomReportGenerator}
 * 
 * @author Hiclab
 *
 */
public class XMLCustomReportGeneratorTest {

	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private XMLStreamWriter writer;
	private XMLCustomReportGenerator generator;

	private static final String SUITE_NAME = "suite123";

	@Before
	public void setup() throws CoreException {
		generator = new XMLCustomReportGenerator();
		out = new ByteArrayOutputStream();
		writer = XMLUtils.createWriter(out);
	}

	@After
	public void close() throws XMLStreamException, IOException {
		out.close();
		writer.close();
	}

	private void generate(Iterable<Report> reports) throws XMLStreamException {
		generator.writeTestSuite(writer, SUITE_NAME, reports);
	}

	private Node createNode(String name, int severity, String resultMessage) {
		Node node = ReportFactory.eINSTANCE.createNode();
		node.setName(name);
		ReportHelper.getInfo(node).setResult(RcpttPlugin.createProcessStatus(severity, resultMessage));
		return node;
	}

	private Report createReport(String name, int severity, int startTime, int endTime) {
		return createReport(name, severity, startTime, endTime, "No message");
	}

	private Report createReport(String name, int severity, int startTime, int endTime, String resultMessage) {
		Report report = ReportFactory.eINSTANCE.createReport();
		Node root = createNode(name, severity, resultMessage);
		report.setRoot(root);
		root.setStartTime(startTime);
		root.setEndTime(endTime);
		root.setDuration(report.getRoot().getEndTime() - report.getRoot().getStartTime());
		return report;
	}

	private void checkReport(Suite suite) throws XMLStreamException, FactoryConfigurationError {
		InputStream in = new ByteArrayInputStream(out.toByteArray());
		XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(in);
		Iterator<Case> testcasesIterator = null;
		Case currentTestcase = null;

		if (suite.getTestCases() != null) {
			testcasesIterator = suite.getTestCases().iterator();
		}

		while (xmlStreamReader.hasNext()) {
			int event = xmlStreamReader.next();
			if (event == XMLStreamConstants.START_ELEMENT && xmlStreamReader.getLocalName().equals("testsuite")) {
				assertEquals(suite.getName(), xmlStreamReader.getAttributeValue(null, "name"));
				assertEquals(suite.getTime(), xmlStreamReader.getAttributeValue(null, "time"));
				assertEquals(suite.getTests(), xmlStreamReader.getAttributeValue(null, "tests"));
				assertEquals(suite.getPassed(), xmlStreamReader.getAttributeValue(null, "passed"));
				assertEquals(suite.getFailed(), xmlStreamReader.getAttributeValue(null, "failed"));
				assertEquals(suite.getSkipped(), xmlStreamReader.getAttributeValue(null, "skipped"));
			} else {
				if (testcasesIterator != null) {
					if (event == XMLStreamConstants.START_ELEMENT) {
						if (xmlStreamReader.getLocalName().equals("testcase")) { // test case
							currentTestcase = testcasesIterator.next();
							assertEquals(currentTestcase.getName(), xmlStreamReader.getAttributeValue(null, "name"));
							assertEquals(currentTestcase.getTags(), xmlStreamReader.getAttributeValue(null, "tags"));
							assertEquals(currentTestcase.getTime(), xmlStreamReader.getAttributeValue(null, "time"));
							assertEquals(currentTestcase.getResult(),
									xmlStreamReader.getAttributeValue(null, "result"));
						} else if (xmlStreamReader.getLocalName().equals("description")) { // description
							assertEquals(currentTestcase.getDescription(), xmlStreamReader.getElementText());
						} else if (xmlStreamReader.getLocalName().equals("message")) { // error message
							assertEquals(currentTestcase.getFailureMessage(), xmlStreamReader.getElementText().trim());
						}
					}
				} else {
					assertNotEquals(XMLStreamConstants.START_ELEMENT, event);
				}
			}
		}

		if (testcasesIterator != null) { // check exceeded test case
			assertTrue(!testcasesIterator.hasNext());
		}
	}

	@Test
	public void returnEmptyReport_WhenSuiteIsEmpty() throws XMLStreamException {
		// given
		Iterable<Report> reports = List.of();

		// when
		generate(reports);

		// then
		Suite suite = new Suite(SUITE_NAME);
		checkReport(suite);
	}

	@Test
	public void returnReport_WhenAllCasesPassed() throws XMLStreamException {
		// given
		String name = "case1";
		String tags = "list of tags";
		String desc = "short description";
		Report report = createReport(name, IStatus.OK, 1000, 3000);
		Q7Info info = ReportHelper.getInfo(report.getRoot());
		info.setTags(tags);
		info.setDescription(desc);

		// when
		generate(Arrays.asList(report));

		// then
		Suite suite = new Suite(SUITE_NAME);
		Case testcase = new Case(name);
		testcase.setResult(XMLCustomReportGenerator.RESULT_PASSED);
		testcase.setTime("2.000");
		testcase.setTags(tags);
		testcase.setDescription(desc);

		suite.addTestCase(testcase);
		checkReport(suite);
	}

	@Test
	public void returnReport_WhenOneCaseFailed() throws XMLStreamException {
		// given
		String name = "case1";
		String tags = "list of tags";
		String desc = "short description";
		Report report = createReport(name, IStatus.OK, 1000, 3000);
		Q7Info info = ReportHelper.getInfo(report.getRoot());
		info.setTags(tags);
		info.setDescription(desc);

		String name2 = "case2";
		String tags2 = "list of tags 2";
		String desc2 = "short description 2";
		String errorMessage2 = "Error message 2";
		Report report2 = createReport(name2, IStatus.ERROR, 4000, 9500, errorMessage2);
		Q7Info info2 = ReportHelper.getInfo(report2.getRoot());
		info2.setTags(tags2);
		info2.setDescription(desc2);

		// when
		generate(Arrays.asList(report, report2));

		// then
		Suite suite = new Suite(SUITE_NAME);
		Case testcase = new Case(name);
		testcase.setResult(XMLCustomReportGenerator.RESULT_PASSED);
		testcase.setTime("2.000");
		testcase.setTags(tags);
		testcase.setDescription(desc);

		Case testcase2 = new Case(name2);
		testcase2.setResult(XMLCustomReportGenerator.RESULT_FAILED);
		testcase2.setTime("5.500");
		testcase2.setTags(tags2);
		testcase2.setDescription(desc2);
		testcase2.setFailureMessage(errorMessage2);

		suite.addTestCase(testcase);
		suite.addTestCase(testcase2);
		checkReport(suite);
	}

	@Test
	public void returnReport_WhenDescriptionIsLong() throws XMLStreamException {
		// given
		String name = "case1";
		StringBuilder desc = new StringBuilder("Long description");
		for (int i = 0; i < 200; i++) {
			desc.append("0123456789");
		}

		Report report = createReport(name, IStatus.OK, 1000, 3000);
		Q7Info info = ReportHelper.getInfo(report.getRoot());
		info.setTags("");
		info.setDescription(desc.toString());
		assertTrue(info.getDescription().length() > XMLCustomReportGenerator.DESCRIPTION_MAX_LENGTH);

		// when
		generate(Arrays.asList(report));

		// then
		Suite suite = new Suite(SUITE_NAME);
		Case testcase = new Case(name);
		testcase.setTime("2.000");
		testcase.setTags("");
		testcase.setResult(XMLCustomReportGenerator.RESULT_PASSED);
		testcase.setDescription(desc.substring(0, XMLCustomReportGenerator.DESCRIPTION_MAX_LENGTH));

		suite.addTestCase(testcase);
		checkReport(suite);
	}
}
