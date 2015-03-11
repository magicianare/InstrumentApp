package instrument.app.com.instrumentapp;

import android.graphics.Color;
import android.util.Log;

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
public class ChartService {

    private static final int HISTORY_SIZE = 310;            // number of points to plot in history

    private Redrawer redrawer;
    private XYPlot mainPlot;
    private SimpleXYSeries sSeries = null;
    private SimpleXYSeries xSeries = null;
    private SimpleXYSeries ySeries = null;
    private SimpleXYSeries zSeries = null;

    private int start = 0;
    private int end = 10;


    public ChartService(XYPlot mainPlot){
        this.mainPlot = mainPlot;
    }

    public void createChart(){

        // setup the APR History plot:
        sSeries = new SimpleXYSeries("Sound");
        xSeries = new SimpleXYSeries("X");
        ySeries = new SimpleXYSeries("Y");
        zSeries = new SimpleXYSeries("Z");

        mainPlot.setRangeBoundaries(0, 100, BoundaryMode.GROW);
        mainPlot.setDomainBoundaries(start, end, BoundaryMode.FIXED);
        mainPlot.addSeries(sSeries,
                new LineAndPointFormatter(
                        Color.rgb(100, 100, 200), null, null, null));
//        mainPlot.addSeries(xSeries,
//                new LineAndPointFormatter(
//                        Color.rgb(100, 100, 200), null, null, null));
//        mainPlot.addSeries(ySeries,
//                new LineAndPointFormatter(
//                        Color.rgb(100, 200, 100), null, null, null));
//        mainPlot.addSeries(zSeries,
//                new LineAndPointFormatter(
//                        Color.rgb(200, 100, 100), null, null, null));
        mainPlot.setDomainStepMode(XYStepMode.SUBDIVIDE);
//        mainPlot.setdomain
        mainPlot.setDomainStepValue(10);
        mainPlot.setDomainValueFormat(new DecimalFormat("##.#"));
//        mainPlot.setTicksPerRangeLabel(3);
        mainPlot.setDomainLabel("Index");
        mainPlot.getDomainLabelWidget().pack();
        mainPlot.setRangeLabel("");
        mainPlot.getRangeLabelWidget().pack();


        final PlotStatistics histStats = new PlotStatistics(1000, false);

        mainPlot.addListener(histStats);

        redrawer = new Redrawer(Arrays.asList(new Plot[]{mainPlot}), 100, false);

    }

    public void seriesChange(String series){
        if(series.equals("sS")){
            mainPlot.clear();
            mainPlot.addSeries(sSeries, new LineAndPointFormatter(Color.rgb(100, 100, 200), null, null, null));
            mainPlot.setRangeBoundaries(0, 100, BoundaryMode.GROW);
        }else if(series.equals("xS")){
            mainPlot.clear();
            mainPlot.addSeries(xSeries, new LineAndPointFormatter(Color.rgb(100, 200, 100), null, null, null));
            mainPlot.setRangeBoundaries(0, 100, BoundaryMode.GROW);
        }else if(series.equals("yS")){
            mainPlot.clear();
            mainPlot.addSeries(ySeries, new LineAndPointFormatter(Color.rgb(200, 100, 100), null, null, null));
            mainPlot.setRangeBoundaries(0, 100, BoundaryMode.GROW);
        }else if(series.equals("zS")){
            mainPlot.clear();
            mainPlot.addSeries(zSeries, new LineAndPointFormatter(Color.rgb(200, 200, 200), null, null, null));
            mainPlot.setRangeBoundaries(0, 100, BoundaryMode.GROW);
        }else if(series.equals("all")){
            mainPlot.clear();
            mainPlot.addSeries(sSeries, new LineAndPointFormatter(Color.rgb(100, 100, 200), null, null, null));
            mainPlot.addSeries(xSeries, new LineAndPointFormatter(Color.rgb(100, 200, 100), null, null, null));
            mainPlot.addSeries(ySeries, new LineAndPointFormatter(Color.rgb(200, 100, 100), null, null, null));
            mainPlot.addSeries(zSeries, new LineAndPointFormatter(Color.rgb(200, 200, 200), null, null, null));
        }else{
            mainPlot.clear();
        }
    }


    public void setData(double t, double s, double x, double y, double z){

        // get rid the oldest sample in history:
        if (sSeries.size() > HISTORY_SIZE) {
            sSeries.removeFirst();
            xSeries.removeFirst();
            ySeries.removeFirst();
            zSeries.removeFirst();
        }


//        Log.d("t",String.valueOf(t) + "/" + String.valueOf(s));

        // add the latest history sample:
        sSeries.addLast(t, s);
        xSeries.addLast(t, x);
        ySeries.addLast(t, y);
        zSeries.addLast(t, z);

//        Log.d("t",String.valueOf(sSeries.getX(0)) + "/" + String.valueOf(sSeries.getY(0)));


        if(end < t){
            int time = (int) t;
            start += 1;
            end += 1;

            mainPlot.setDomainBoundaries(start, end, BoundaryMode.FIXED);
        }

    }

    public void clear(){
        mainPlot.invalidate();

    }
    public void start(){
        redrawer.start();
    }
    public void pause(){
        redrawer.pause();
    }
    public void stop(){
        redrawer.finish();
    }
}
