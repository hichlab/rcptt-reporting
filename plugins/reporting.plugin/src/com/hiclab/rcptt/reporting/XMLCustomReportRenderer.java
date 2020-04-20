package com.hiclab.rcptt.reporting;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.reporting.core.IReportRenderer;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;


public class XMLCustomReportRenderer implements IReportRenderer {

	@Override
	public IStatus generateReport(IContentFactory factory, String reportName, Iterable<Report> report) {

		try {
			factory.removeFileOrFolder(reportName);
			factory = factory.createFolder(reportName);
			Iterator<Report> report = reports.iterator();
			while (report.hasNext()) {
				Report test = report.next();
				writeContents(factory.createFileStream(getFileName(test.getRoot().getName(), factory)),
						new XMLCustomReportGenerator().generateContent(test));
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
