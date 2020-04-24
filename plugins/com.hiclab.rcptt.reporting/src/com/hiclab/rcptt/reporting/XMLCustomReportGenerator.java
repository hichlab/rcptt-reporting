package com.hiclab.rcptt.reporting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.Q7Statistics;
import org.eclipse.rcptt.reporting.core.IQ7ReportConstants;
import org.eclipse.rcptt.reporting.core.SimpleSeverity;
import org.eclipse.rcptt.reporting.util.ReportUtils;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;

/**
 * XML generator for rcptt reports. 
 * The generated content is similar to the Junit xml report.
 * 
 * @author Hiclab
 *
 */
public class XMLCustomReportGenerator {

	public static final String RESULT_FAILED = "Failed";
	public static final String RESULT_PASSED = "Passed";
	public static final String RESULT_SKIPPED = "Skipped";

	private DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

	public static final int DESCRIPTION_MAX_LENGTH = 1000;

	/**
	 * Writes in xml format statistics and other relevant information about the
	 * execution of a given test suite
	 * 
	 * @param writer      XMLStreamWriter
	 * @param name        Suite name
	 * @param testReports List of test case reports
	 * @throws XMLStreamException
	 */
	/**
	 * Generates all information for a test suite and its executed test cases.
	 * 
	 * @param writer  the xml writer used to write the information into the output
	 *                stream
	 * @param name    the name of rcptt report
	 * @param reports the list of rcptt reports
	 * @throws XMLStreamException
	 */
	public void writeTestSuite(XMLStreamWriter writer, String name, Iterable<Report> reports)
			throws XMLStreamException {
		Q7Statistics stats = ReportUtils.calculateStatistics(reports.iterator());

		writer.writeStartElement("testsuite");

		writer.writeAttribute("name", name);
		writer.writeAttribute("date", dateFormat.format(new Date()));
		writer.writeAttribute("time", ReportUtils.formatTime(stats.getTime()));

		writer.writeAttribute("passed", Integer.toString(stats.getPassed()));
		writer.writeAttribute("failed", Integer.toString(stats.getFailed()));
		writer.writeAttribute("skipped", Integer.toString(stats.getSkipped()));
		writer.writeAttribute("tests", Integer.toString(stats.getTotal()));

		Iterator<Report> reportIterator = reports.iterator();
		while (reportIterator.hasNext()) {
			Report report = reportIterator.next();
			writeTestCase(writer, report);
		}

		writer.writeEndElement();
	}

	/**
	 * Generates all relevant information for a given test case execution in xml
	 * format.
	 * 
	 * @param writer the xml writer used to write the information into the output
	 *               stream
	 * @param report the rcptt report
	 * @throws XMLStreamException
	 */
	protected void writeTestCase(XMLStreamWriter writer, Report report) throws XMLStreamException {
		Node item = report.getRoot();
		Q7Info info = (Q7Info) item.getProperties().get(IQ7ReportConstants.ROOT);
		String name = item.getName();

		if (info != null && info.getVariant() != null && !info.getVariant().isEmpty()) {
			name += "_" + ReportUtils.combineNames(info.getVariant(), "_");
		}

		writer.writeStartElement("testcase");
		writer.writeAttribute("name", name);
		writer.writeAttribute("time", ReportUtils.formatTime(item.getEndTime() - item.getStartTime()));

		// execution result
		SimpleSeverity severity = SimpleSeverity.create(info);
		String result = null;
		switch (severity) {
		case OK:
			result = RESULT_PASSED;
			break;
		case CANCEL:
			result = RESULT_SKIPPED;
			break;
		case ERROR:
			result = RESULT_FAILED;
			break;
		}

		writer.writeAttribute("result", String.valueOf(result));

		if (info != null) {
			writer.writeAttribute("tags", info.getTags());

			// write only the first x characters of the test description
			String description = info.getDescription();
			if (description != null) {
				if (description.length() > DESCRIPTION_MAX_LENGTH) {
					description = description.substring(0, DESCRIPTION_MAX_LENGTH);
				}
				writer.writeStartElement("description");
				writer.writeCData(description);
				writer.writeEndElement();
			}
		}

		// error message
		if (RESULT_FAILED.equals(result)) {
			writer.writeStartElement("failure");
			writer.writeStartElement("message");
			writer.writeCData(ReportUtils.getFailMessage(item));
			writer.writeEndElement();

			writer.writeEndElement();
		}

		writer.writeEndElement();
	}
}
