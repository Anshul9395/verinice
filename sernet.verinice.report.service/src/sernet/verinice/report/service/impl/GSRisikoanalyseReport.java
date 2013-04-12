/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.impl;

import java.net.URL;

import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;

public class GSRisikoanalyseReport implements IReportType {
	
    private static final String REPORT_DESIGN = "Risikoanalyse.rptdesign"; //$NON-NLS-1$

	public String getDescription() {
		return Messages.RisikoanalyseReport_1;
	}

	public String getId() {
		return "risikoanalyse"; //$NON-NLS-1$
	}

	public String getLabel() {
		return Messages.RisikoanalyseReport_2;
	}

	public IOutputFormat[] getOutputFormats() {
        return new IOutputFormat[] { new PDFOutputFormat(), new HTMLOutputFormat(), new ExcelOutputFormat(), new WordOutputFormat(), new ODTOutputFormat(), new ODSOutputFormat() };
    }

	public void createReport(IReportOptions reportOptions) {
		BIRTReportService brs = new BIRTReportService();
		
		URL reportDesign = GSRisikoanalyseReport.class.getResource(REPORT_DESIGN); //$NON-NLS-1$
		
		if (((AbstractOutputFormat) reportOptions.getOutputFormat()).isRenderOutput())
		{
			IRunAndRenderTask task = brs.createTask(reportDesign);
			brs.render(task, reportOptions);
		}
		else
		{
			IDataExtractionTask task = brs.createExtractionTask(reportDesign);
			brs.extract(task, reportOptions, 1);
		}
	}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#getReportFile()
     */
    @Override
    public String getReportFile() {
        return Messages.RisikoanalyseReport_3;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#setReportFile(java.lang.String)
     */
    @Override
    public void setReportFile(String file) {
        // TODO Auto-generated method stub
        
    }

	@Override
	public String getUseCaseID() {
		return IReportType.USE_CASE_ID_GENERAL_REPORT;
	}

}