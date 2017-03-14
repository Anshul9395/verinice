/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web.poseidon.view;

import java.io.Serializable;
import java.util.SortedMap;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.PieChartModel;

import sernet.verinice.web.poseidon.services.ChartService;

/**
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "totalBsiChartView")
public class TotalBsiChartsView implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManagedProperty("#{chartService}")
    private ChartService chartService;

    private SortedMap<String, Number> states;

    private PieChartModel pieModel;

    private BarChartModel barModel;

    private boolean calculated = false;

    public void init() {
        BsiControlChartsFactory chartModelFactory = new BsiControlChartsFactory(getStates());
        this.pieModel = chartModelFactory.getPieChartModel();
        this.barModel = chartModelFactory.getBarChart();
        this.calculated = true;
    }

    private SortedMap<String, Number> getStates() {
        if (states == null){
            states = getChartService().aggregateMassnahmenUmsetzungStatus();
        }
        return states;
    }

    public void setPieModel(PieChartModel pieModel) {
        this.pieModel = pieModel;
    }

    public PieChartModel getPieModel() {
        return pieModel;
    }

    public BarChartModel getBarModel() {
        return barModel;
    }

    public void setBarModel(BarChartModel barModel) {
        this.barModel = barModel;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    public ChartService getChartService() {
        return chartService;
    }

    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }
}
