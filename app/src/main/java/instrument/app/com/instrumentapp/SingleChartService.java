package instrument.app.com.instrumentapp;

import com.androidplot.Plot;
import com.androidplot.util.PlotStatistics;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Created by are-mac on 2015. 3. 7..
 */
public class SingleChartService {

    private XYPlot mainPlot;
    private Redrawer redrawer;

    private SimpleXYSeries series = null;

    private int start = 0;
    private int end = 5;


    public SingleChartService(XYPlot mainPlot){
        this.mainPlot = mainPlot;
    }

    public void createChart(String name, int color){

        // setup the APR History plot:
        series = new SimpleXYSeries(name);

        mainPlot.setRangeBoundaries(0, 100, BoundaryMode.GROW);
        mainPlot.setDomainBoundaries(start, end, BoundaryMode.FIXED);
        mainPlot.addSeries(series,
                new LineAndPointFormatter(color, null, null, null));
        mainPlot.setDomainStepMode(XYStepMode.SUBDIVIDE);
//        mainPlot.setdomain
        mainPlot.setDomainStepValue(10);
        mainPlot.setDomainValueFormat(new DecimalFormat("##.#"));
//        mainPlot.setTicksPerRangeLabel(3);
        mainPlot.setDomainLabel("Index");
        mainPlot.getDomainLabelWidget().pack();
        mainPlot.setRangeLabel("Angle (Degs)");
        mainPlot.getRangeLabelWidget().pack();

        final PlotStatistics histStats = new PlotStatistics(1000, false);

        mainPlot.addListener(histStats);
        redrawer = new Redrawer(Arrays.asList(new Plot[]{mainPlot}), 100, false);

    }



    public void setData(double t, double y){

        // add the latest history sample:
        series.addLast(t, y);


    }

    public void start(){
        redrawer.start();
    }

    public void stop(){
        redrawer.finish();
    }
}
