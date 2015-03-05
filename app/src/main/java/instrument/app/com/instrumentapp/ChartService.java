package instrument.app.com.instrumentapp;

/**
 * Created by are-mac on 2015. 2. 18..
 */

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.Log;

public class ChartService {

    private GraphicalView mGraphicalView;
    private XYMultipleSeriesDataset multipleSeriesDataset;
    private XYMultipleSeriesRenderer multipleSeriesRenderer;
    private XYSeries sSeries;
    private XYSeries xSeries;
    private XYSeries ySeries;
    private XYSeries zSeries;
    private XYSeries emptySeries;
    private XYSeriesRenderer sRenderer;
    private XYSeriesRenderer xRenderer;
    private XYSeriesRenderer yRenderer;
    private XYSeriesRenderer zRenderer;
    private Context context;
    private double maxX;
    private double maxY;
    private float lineWidth = 3.0f;
    private int index = 0;

    public ChartService(Context context, double maxX, double maxY){
        this.context = context;
        this.maxX = maxX;
        this.maxY = maxY;
    }
    /**
     * 가져오기 도표
     *
     * @return
     */
    public GraphicalView getGraphicalView() {
        mGraphicalView = ChartFactory.getCubeLineChartView(context,
                multipleSeriesDataset, multipleSeriesRenderer, 0.5f);
        return mGraphicalView;
    }

    /**
     * 가져오는 데이터 및 xy 좌표 집합
     *
     */
    public void setXYMultipleSeriesDataset() {
        multipleSeriesDataset = new XYMultipleSeriesDataset();
        sSeries = new XYSeries("Sound");
        xSeries = new XYSeries("X");
        ySeries = new XYSeries("Y");
        zSeries = new XYSeries("Z");
        emptySeries = new XYSeries("");

        multipleSeriesDataset.addSeries(0, sSeries);
        multipleSeriesDataset.addSeries(1, xSeries);
        multipleSeriesDataset.addSeries(2, ySeries);
        multipleSeriesDataset.addSeries(3, zSeries);

    }

    /**
     * 가져오기 렌더러
     * @param chartTitle
     *            곡선 제목
     * @param xTitle
     *            x-축 제목
     * @param yTitle
     *            y 축 제목
     * @param axeColor
     *            축 색
     * @param labelColor
     *            제목 표시줄 색으로
     * @param curveColor
     *            곡선 색
     * @param gridColor
     *            격자 색상
     */
    public void setXYMultipleSeriesRenderer(String chartTitle, String xTitle, String yTitle, int axeColor,
                                            int labelColor, int curveColor, int gridColor) {
        multipleSeriesRenderer = new XYMultipleSeriesRenderer();
        if (chartTitle != null) {
            multipleSeriesRenderer.setChartTitle(chartTitle);
        }
        multipleSeriesRenderer.setXTitle(xTitle);
        multipleSeriesRenderer.setYTitle(yTitle);
        multipleSeriesRenderer.setRange(new double[]{0, maxX, 0 ,maxY});

        multipleSeriesRenderer.setClickEnabled(false);
        multipleSeriesRenderer.setLabelsColor(labelColor);
        multipleSeriesRenderer.setXLabels(10);
        multipleSeriesRenderer.setYLabels(10);
        multipleSeriesRenderer.setXLabelsAlign(Align.RIGHT);
        multipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);
        multipleSeriesRenderer.setAxisTitleTextSize(20);
        multipleSeriesRenderer.setChartTitleTextSize(20);
        multipleSeriesRenderer.setLabelsTextSize(20);
        multipleSeriesRenderer.setLegendTextSize(20);
        multipleSeriesRenderer.setPointSize(0.1f);
        multipleSeriesRenderer.setFitLegend(true);
        multipleSeriesRenderer.setMargins(new int[]{50, 50, 50, 50});
        multipleSeriesRenderer.setShowGrid(true);
        multipleSeriesRenderer.setZoomEnabled(true, false);
        multipleSeriesRenderer.setAxesColor(axeColor);
        multipleSeriesRenderer.setGridColor(gridColor);
        multipleSeriesRenderer.setBackgroundColor(Color.BLACK);
        multipleSeriesRenderer.setMarginsColor(Color.BLACK);

        sRenderer = new XYSeriesRenderer();
        sRenderer.setColor(Color.WHITE);
        sRenderer.setPointStyle(PointStyle.DIAMOND);
        sRenderer.setLineWidth(lineWidth);
        sRenderer.setPointStrokeWidth(lineWidth);

        xRenderer = new XYSeriesRenderer();
        xRenderer.setColor(Color.RED);
        xRenderer.setPointStyle(PointStyle.DIAMOND);
        xRenderer.setLineWidth(lineWidth);
        xRenderer.setPointStrokeWidth(lineWidth);

        yRenderer = new XYSeriesRenderer();
        yRenderer.setColor(Color.BLUE);
        yRenderer.setPointStyle(PointStyle.DIAMOND);
        yRenderer.setLineWidth(lineWidth);
        yRenderer.setPointStrokeWidth(lineWidth);

        zRenderer = new XYSeriesRenderer();
        zRenderer.setColor(Color.YELLOW);
        zRenderer.setPointStyle(PointStyle.DIAMOND);
        zRenderer.setLineWidth(lineWidth);
        zRenderer.setPointStrokeWidth(lineWidth);

        multipleSeriesRenderer.addSeriesRenderer(0, sRenderer);
        multipleSeriesRenderer.addSeriesRenderer(1, xRenderer);
        multipleSeriesRenderer.addSeriesRenderer(2, yRenderer);
        multipleSeriesRenderer.addSeriesRenderer(3, zRenderer);
    }

    public void setXYMultipleSeriesRenderer(String chartTitle) {
        multipleSeriesRenderer = new XYMultipleSeriesRenderer();
        if (chartTitle != null) {
            multipleSeriesRenderer.setChartTitle(chartTitle);
        }
        multipleSeriesRenderer.setRange(new double[]{0, maxX, 0 ,maxY});

        multipleSeriesRenderer.setClickEnabled(false);
        multipleSeriesRenderer.setYTitle(chartTitle);
        multipleSeriesRenderer.setXLabels(10);
        multipleSeriesRenderer.setYLabels(10);
        multipleSeriesRenderer.setXLabelsAlign(Align.RIGHT);
        multipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);
        multipleSeriesRenderer.setAxisTitleTextSize(10);
        multipleSeriesRenderer.setChartTitleTextSize(10);
        multipleSeriesRenderer.setLabelsTextSize(10);
        multipleSeriesRenderer.setLegendTextSize(10);
        multipleSeriesRenderer.setPointSize(0.1f);
        multipleSeriesRenderer.setFitLegend(true);
        multipleSeriesRenderer.setMargins(new int[]{10, 30, 10, 10});
        multipleSeriesRenderer.setShowGrid(true);
//        multipleSeriesRenderer.setShowLabels(false)
        multipleSeriesRenderer.setShowLegend(false);
        multipleSeriesRenderer.setZoomEnabled(true, false);

        sRenderer = new XYSeriesRenderer();
        sRenderer.setColor(Color.WHITE);
        sRenderer.setPointStyle(PointStyle.DIAMOND);
        sRenderer.setLineWidth(lineWidth);
        sRenderer.setPointStrokeWidth(lineWidth);

        xRenderer = new XYSeriesRenderer();
        xRenderer.setColor(Color.RED);
        xRenderer.setPointStyle(PointStyle.DIAMOND);
        xRenderer.setLineWidth(lineWidth);
        xRenderer.setPointStrokeWidth(lineWidth);

        yRenderer = new XYSeriesRenderer();
        yRenderer.setColor(Color.BLUE);
        yRenderer.setPointStyle(PointStyle.DIAMOND);
        yRenderer.setLineWidth(lineWidth);
        yRenderer.setPointStrokeWidth(lineWidth);

        zRenderer = new XYSeriesRenderer();
        zRenderer.setColor(Color.YELLOW);
        zRenderer.setPointStyle(PointStyle.DIAMOND);
        zRenderer.setLineWidth(lineWidth);
        zRenderer.setPointStrokeWidth(lineWidth);

        multipleSeriesRenderer.addSeriesRenderer(0, sRenderer);
        multipleSeriesRenderer.addSeriesRenderer(1, xRenderer);
        multipleSeriesRenderer.addSeriesRenderer(2, yRenderer);
        multipleSeriesRenderer.addSeriesRenderer(3, zRenderer);
    }

    public void resetChart(){
        sSeries.clear();
        xSeries.clear();
        ySeries.clear();
        zSeries.clear();
    }

    /**
     * 새로 더하기 데이터 따라 업데이트 곡선, 오직 실행 주 스레드
     *
     * @param x
     *            새 좀 있는 x 좌표
     * @param y
     *            새 좀 있는 y 좌표
     */
    public void updateChart(double t, double s, double x, double y, double z, boolean pauseYn) {

        //mSeries.add(x, y);

        sSeries.add(t, s);
        xSeries.add(t, x);
        ySeries.add(t, y);
        zSeries.add(t, z);

        if(index > 200){
            sSeries.remove(0);
            xSeries.remove(0);
            ySeries.remove(0);
            zSeries.remove(0);
        }
        index++;

        if(!pauseYn){
            double startX = (t-maxX < 0)? 0 : t-maxX;
            double endX = (t-maxX > 0) ? t : maxX;

            multipleSeriesRenderer.setRange(new double[]{startX, endX, 0d, maxY});
            mGraphicalView.repaint();//이 곳은 해도 호출 invalidate()
        }

    }

    public void updateChart2(List<double[]> list) {
        for (int i = 0; i <list.size(); i++) {
            double[] record = list.get(i);
            sSeries.add(record[0], record[1]);
            xSeries.add(record[0], record[2]);
            ySeries.add(record[0], record[3]);
            zSeries.add(record[0], record[4]);

        }

        double t = list.get(list.size()-1)[0];

        double startX = (t-maxX < 0)? 0 : t-maxX;
        double endX = (t-maxX > 0) ? t : maxX;

        multipleSeriesRenderer.setRange(new double[]{startX, endX, 0d, maxY});
        mGraphicalView.repaint();//이 곳은 해도 호출 invalidate()
    }

    public void SeriesChange(String series){

        if(series.equals("sS")){
            multipleSeriesDataset.clear();
            multipleSeriesDataset.addSeries(0, sSeries);
        }else if(series.equals("xS")){
            multipleSeriesDataset.clear();
            multipleSeriesDataset.addSeries(0, emptySeries);
            multipleSeriesDataset.addSeries(1, xSeries);
        }else if(series.equals("yS")){
            multipleSeriesDataset.clear();
            multipleSeriesDataset.addSeries(0, emptySeries);
            multipleSeriesDataset.addSeries(1, emptySeries);
            multipleSeriesDataset.addSeries(2, ySeries);
        }else if(series.equals("zS")){
            multipleSeriesDataset.clear();
            multipleSeriesDataset.addSeries(0, emptySeries);
            multipleSeriesDataset.addSeries(1, emptySeries);
            multipleSeriesDataset.addSeries(2, emptySeries);
            multipleSeriesDataset.addSeries(3, zSeries);
        }else if(series.equals("wAll")){
            multipleSeriesDataset.clear();
            multipleSeriesDataset.addSeries(0, emptySeries);
            multipleSeriesDataset.addSeries(1, xSeries);
            multipleSeriesDataset.addSeries(2, ySeries);
            multipleSeriesDataset.addSeries(3, zSeries);

        }else if(series.equals("all")){
            multipleSeriesDataset.clear();
            multipleSeriesDataset.addSeries(0, sSeries);
            multipleSeriesDataset.addSeries(1, xSeries);
            multipleSeriesDataset.addSeries(2, ySeries);
            multipleSeriesDataset.addSeries(3, zSeries);

        }

    }

    public void updateChart(List<double[]> list) {
        for (int i = 0; i <list.size(); i++) {
            double[] record = list.get(i);
            sSeries.add(record[0], record[1]);
            xSeries.add(record[0], record[2]);
            ySeries.add(record[0], record[3]);
            zSeries.add(record[0], record[4]);

        }
        multipleSeriesRenderer.setRange(new double[]{list.get(0)[0], list.get(list.size()-1)[0], 0d, maxY});
        mGraphicalView.repaint();//이 곳은 해도 호출 invalidate()
    }
}