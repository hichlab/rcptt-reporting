package com.hiclab.rcptt.reporting;

import static org.eclipse.rcptt.reporting.util.internal.Plugin.UTILS;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.reporting.core.IReportRenderer;
import org.eclipse.rcptt.reporting.util.internal.XMLUtils;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;

/**
 * Custom report generator for rcptt.
 * 
 * @author Hiclab
 *
 */
public class XMLCustomReportRenderer implements IReportRenderer {

	@Override
	public IStatus generateReport(IContentFactory factory, String reportName, Iterable<Report> reports) {

		try {
			factory = factory.createFolder(reportName);
			try (OutputStream out = factory.createFileStream(reportName + ".xml")) {
				XMLStreamWriter writer = XMLUtils.createWriter(out);
				try {
					writer.writeStartDocument();
					new XMLCustomReportGenerator().writeTestSuite(writer, reportName, reports);
				} catch (XMLStreamException ex) {
					return UTILS.createError(ex);
				} finally {
					XMLUtils.closeWriter(writer);
				}
			} catch (IOException e) {
				return UTILS.createError(e);
			}
		} catch (CoreException e) {
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

	public String[] getGeneratedFileNames(String reportName) {
		return new String[] { reportName };
	}

}
