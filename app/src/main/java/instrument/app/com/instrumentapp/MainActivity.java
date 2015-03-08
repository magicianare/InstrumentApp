package instrument.app.com.instrumentapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.XYPlot;

import org.achartengine.GraphicalView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

public class MainActivity extends Activity {

    private String path = "/sdcard/Instrument/"; // 파일이 저장될 경로
    private FileOutputStream fos;

    private static final int ACTION_ENABLE_BT = 101;
    private static final String  BLUE_NAME = "BluetoothEx";  // 접속시 사용하는 이름
    private BluetoothAdapter mBA;

    static final UUID BLUE_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private ClientThread mCThread = null; // 클라이언트 소켓 접속 스레드
    private ServerThread mSThread = null; // 서버 소켓 접속 스레드
    private SocketThread mSocketThread = null; // 데이터 송수신 스레드

    private LinearLayout subChartLayout;
    private XYPlot mainChartLayout, sSubChartLayout, xSubChartLayout, ySubChartLayout, zSubChartLayout;
    private GraphicalView mView, sView,xView, yView, zView;
    private ChartService mService;
    private SingleChartService sService, xService, yService, zService;
    private Button startBtn, stopBtn, pauseBtn, recordBtn;
    private TextView sMaxTv, xMaxTv, yMaxTv, zMaxTv, sMinTv, xMinTv, yMinTv, zMinTv;
    private TextView rsMaxTv, rxMaxTv, ryMaxTv, rzMaxTv, rsMinTv, rxMinTv, ryMinTv, rzMinTv;
    private double sMin, xMin, yMin, zMin, sMax, xMax, yMax, zMax;
    private double rsMin, rxMin, ryMin, rzMin, rsMax, rxMax, ryMax, rzMax;
    private Timer timer;
    private List<double[]> list, recordList;
    private boolean pauseYn;
    private boolean recordYn;
    private boolean blueToothYn;
    private double t = 0;
    private double recordT = 0;
    private long startTime = 0;
    private long recordStartTime = 0;
    private String preStat = "sS";

    // 마이크용 변수
    public AudioReader audioReader;
    private int sampleRate = 8000;
    private int inputBlockSize = 256;
    private int sampleDecimate = 1;
    public  int mMicDataTemp= 0;
    public Handler mHandlerMic;

    private SerialConnector mSerialConnector;
    private Context mContext = null;
    private String[] mReceiveData = null;
    private boolean mIsUsbConnocted = false;



    public void initAudio(){
        audioReader = new AudioReader();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mHandlerMic = new Handler(){
            public void handleMessage(Message msg){
                super.handleMessage(msg);

                t = (double)(System.currentTimeMillis() - startTime)/1000.0;

                if(msg.what == 0) {
                    if (mIsUsbConnocted) {
                        mReceiveData = mSerialConnector.GetSerialData();
                    } else {
                        // When USB is not connected dummy data
                        mReceiveData = new String[3];
                        mReceiveData[0] = "10";
                        mReceiveData[1] = "20";
                        mReceiveData[2] = "30";
                    }

                    if ((mMicDataTemp) < 0){
                        //mMicData.setText("0dB");
                        //sensor.SetMicData(0);

                        //setS(0);
                        setChartData(0, Integer.parseInt(mReceiveData[0]), Integer.parseInt(mReceiveData[1]),Integer.parseInt(mReceiveData[2]));
                    } else {
                        //sensor.SetMicData(mMicDataTemp);
                        //setS(mMicDataTemp);
                        setChartData(mMicDataTemp, Integer.parseInt(mReceiveData[0]), Integer.parseInt(mReceiveData[1]),Integer.parseInt(mReceiveData[2]));
                    }

                }
            };
        };
    }

    public void MicDoStart()
    {
        audioReader.startReader(sampleRate, inputBlockSize * sampleDecimate, new AudioReader.Listener()
        {
            @Override
            public final void onReadComplete(int dB)
            {
                receiveDecibel(dB);
            }

            @Override
            public void onReadError(int error)
            {
            }
        });
    }

    private void receiveDecibel(final int dB) {
        mMicDataTemp = dB;
        mHandlerMic.sendEmptyMessage(0);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Usb to Serial init
        mContext = getApplicationContext();

        mSerialConnector = new SerialConnector(mContext);
        mIsUsbConnocted = mSerialConnector.initialize();

        if (!mIsUsbConnocted) {
            Log.e("","Usb is Not Connected");
        }

        // Mic init
        initAudio();


        mainChartLayout = (XYPlot) findViewById(R.id.main_chart);
        subChartLayout = (LinearLayout) findViewById(R.id.sub_chart);

        sSubChartLayout = (XYPlot) findViewById(R.id.sub_s);
        xSubChartLayout = (XYPlot) findViewById(R.id.sub_x);
        ySubChartLayout = (XYPlot) findViewById(R.id.sub_y);
        zSubChartLayout = (XYPlot) findViewById(R.id.sub_z);

        startBtn = (Button)findViewById(R.id.start);
        pauseBtn = (Button)findViewById(R.id.pause);
        stopBtn = (Button)findViewById(R.id.stop);
        recordBtn = (Button)findViewById(R.id.record);

        sMaxTv = (TextView)findViewById(R.id.sMax);
        xMaxTv = (TextView)findViewById(R.id.xMax);
        yMaxTv = (TextView)findViewById(R.id.yMax);
        zMaxTv = (TextView)findViewById(R.id.zMax);

        sMinTv = (TextView)findViewById(R.id.sMin);
        xMinTv = (TextView)findViewById(R.id.xMin);
        yMinTv = (TextView)findViewById(R.id.yMin);
        zMinTv = (TextView)findViewById(R.id.zMin);

        rsMaxTv = (TextView)findViewById(R.id.rsMax);
        rxMaxTv = (TextView)findViewById(R.id.rxMax);
        ryMaxTv = (TextView)findViewById(R.id.ryMax);
        rzMaxTv = (TextView)findViewById(R.id.rzMax);

        rsMinTv = (TextView)findViewById(R.id.rsMin);
        rxMinTv = (TextView)findViewById(R.id.rxMin);
        ryMinTv = (TextView)findViewById(R.id.ryMin);
        rzMinTv = (TextView)findViewById(R.id.rzMin);

        RadioGroup waveGroup = (RadioGroup) findViewById(R.id.radioGroup);
        waveGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("radio button : ", String.valueOf(checkedId));

                switch(checkedId){
                    case R.id.S :
                        preStat = "sS";
                        mService.seriesChange(preStat);
                        break;

                    case R.id.X :
                        preStat = "xS";
                        mService.seriesChange(preStat);
                        break;

                    case R.id.Y :
                        preStat = "yS";
                        mService.seriesChange(preStat);
                        break;

                    case R.id.Z :
                        preStat = "zS";
                        mService.seriesChange(preStat);
                        break;

                    case R.id.all :
                        preStat = "all";
                        mService.seriesChange(preStat);
                        break;

                    default :
                        break;
                }

            }
        });

        pauseBtn.setEnabled(false);
        stopBtn.setEnabled(false);
        recordBtn.setEnabled(false);

//        sService = new ChartService(this, xWidth, yHeight);
//        xService = new ChartService(this, xWidth, yHeight);
//        yService = new ChartService(this, xWidth, yHeight);
//        zService = new ChartService(this, xWidth, yHeight);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("start", "시작");
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                lp.weight = 0.7f;
                mainChartLayout.setLayoutParams(lp);
                findViewById(R.id.sub_layout).setVisibility(View.GONE);

                startBtn.setEnabled(false);
                pauseBtn.setEnabled(true);
                stopBtn.setEnabled(true);
                recordBtn.setEnabled(true);

//                sSubChartLayout.removeAllViews();
//                xSubChartLayout.removeAllViews();
//                ySubChartLayout.removeAllViews();
//                zSubChartLayout.removeAllViews();

                mService = new ChartService(mainChartLayout);
//                mService.clear();
                mService.createChart();

                list = new ArrayList<double[]>();
                startTime = System.currentTimeMillis();
                mService.start();
                MicDoStart();


                if(blueToothYn){
                    mSocketThread.write("M,STT,E");
                }

                /*
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.sendMessage(handler.obtainMessage());
                        //Log.d("speed", String.valueOf((double)speed/10000));
                        t += 0.1;
                    }
                }, 1, speed);
                */


                Log.d("start", "종료");

            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopBtn.setEnabled(pauseYn);
                recordBtn.setEnabled(pauseYn);
                Log.d("pause", "시작");

                //startBtn.setEnabled(pauseYn);
                pauseYn = !pauseYn;

                if(pauseYn){
                    mService.pause();
                }else{
                    mService.start();
                }

                if(blueToothYn){
                    mSocketThread.write("M,PAU,E");
                }

                Log.d("pause", "종료");
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startBtn.setEnabled(true);
                pauseBtn.setEnabled(false);
                stopBtn.setEnabled(false);
                recordBtn.setEnabled(false);
                Log.d("stop", "시작");

                audioReader.stopReader();
                startTime = 0;

                if(blueToothYn){
                    mSocketThread.write("M,STP,E");
                }
                mService.stop();
                //timer.cancel();
                //timer = null;

                Log.d("stop", "종료");
            }
        });

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recordBtn.setEnabled(false);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                lp.weight = 0.2f;
                mainChartLayout.setLayoutParams(lp);
                findViewById(R.id.sub_layout).setVisibility(View.VISIBLE);


                sService = new SingleChartService(sSubChartLayout);
                sService.createChart("Sound", Color.rgb(100, 100, 200));
                xService = new SingleChartService(xSubChartLayout);
                xService.createChart("X", Color.rgb(100, 200, 100));
                yService = new SingleChartService(ySubChartLayout);
                yService.createChart("Y", Color.rgb(200, 100, 100));
                zService = new SingleChartService(zSubChartLayout);
                zService.createChart("Z", Color.rgb(200, 200, 200));

                //recordList = new ArrayList<double[]>();
                //recordCreate();
                fileCreate();
                sService.start();
                xService.start();
                yService.start();
                zService.start();

                if(blueToothYn && !recordYn){
                    mSocketThread.write("M,RCD,E");
                }

                recordYn = true;
                recordStartTime = System.currentTimeMillis();

            }
        });


        // 블루투스 사용 가능상태 판단
        boolean isBlue = canUseBluetooth();
        if( isBlue )
            // 페어링된 원격 디바이스 목록 구하기
            getParedDevice();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private Handler handler = new Handler() {
        @Override
        //정시 업데이트 도표
        public void handleMessage(Message msg) {

            double s = Double.valueOf(String.format("%1.2f",Math.random() * 100));
            double x = Double.valueOf(String.format("%1.2f",Math.random() * 100));
            double y = Double.valueOf(String.format("%1.2f",Math.random() * 100));
            double z = Double.valueOf(String.format("%1.2f",Math.random() * 100));

//            double s = sensor.getS();
//            double x = sensor.getX();
//            double y = sensor.getY();
//            double z = sensor.getZ();

            setChartData(s, x, y, z);
        }
    };

    public void setS(double s){
        setChartData(s, 0, 0, 0);
    }

    public void setChartData(double s, double x, double y, double z){

//        double s = sensor.getS();
//        double x = sensor.getX();
//        double y = sensor.getY();
//        double z = sensor.getZ();

        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMddHHmmss");


        double[] data = new double[5];

        data[0] = t;
        data[1] = s;
        data[2] = x;
        data[3] = y;
        data[4] = z;
        list.add(data);


//        if(speed >= 5){
//            mService.updateChart2(list, pauseYn);
//            list.clear();
//            speed = 0;
//        }else{
//            speed++;
//        }

//        mService.updateChart(t, s, x, y, z, pauseYn);
        mService.setData(t, s, x, y, z);


        if(sMax < s)
            sMax = s;
        if(xMax < x)
            xMax = x;
        if(yMax < y)
            yMax = y;
        if(zMax < z)
            zMax = z;

        if(sMin == 0 || sMin > s)
            sMin = s;
        if(xMin == 0 || xMin > x)
            xMin = x;
        if(yMin == 0 || yMin > y)
            yMin = y;
        if(zMin == 0 || zMin > z)
            zMin = z;

        sMaxTv.setText(String.valueOf(sMax));
        xMaxTv.setText(String.valueOf(xMax));
        yMaxTv.setText(String.valueOf(yMax));
        zMaxTv.setText(String.valueOf(zMax));

        sMinTv.setText(String.valueOf(sMin));
        xMinTv.setText(String.valueOf(xMin));
        yMinTv.setText(String.valueOf(yMin));
        zMinTv.setText(String.valueOf(zMin));


        if(blueToothYn){

            if(recordYn && recordT <= 5){
                mSocketThread.write("Y,"+t+","+s+","+x+","+y+","+z+",E");
            }else{
                //Log.d("data", "N,"+t+","+s+","+x+","+y+","+z);
                mSocketThread.write("N,"+t+","+s+","+x+","+y+","+z+",E");
            }

        }

        //Log.d("recordT", String.valueOf(recordT));
        if(recordYn && recordT <= 5){

//                Log.d("record start", "start");
            //sService.updateChart(t, x, y, z, s, false);
            //xService.updateChart(t, x, y, z, s, false);
            //yService.updateChart(t, x, y, z, s, false);
            //zService.updateChart(t, x, y, z, s, false);

            sService.setData(recordT, s);
            xService.setData(recordT, x);
            yService.setData(recordT, y);
            zService.setData(recordT, z);

            double[] record = new double[5];

//            record[0] = recordT;
//            record[1] = s;
//            record[2] = x;
//            record[3] = y;
//            record[4] = z;
//            recordList.add(record);

            fileWrite(recordT+","+s+","+x+","+y+","+z+"\n");

            if(rsMax < s)
                rsMax = s;
            if(rxMax < x)
                rxMax = x;
            if(ryMax < y)
                ryMax = y;
            if(rzMax < z)
                rzMax = z;

            if(rsMin == 0 || rsMin > s)
                rsMin = s;
            if(rxMin == 0 || rxMin > x)
                rxMin = x;
            if(ryMin == 0 || ryMin > y)
                ryMin = y;
            if(rzMin == 0 || rzMin > z)
                rzMin = z;

            rsMaxTv.setText(String.valueOf(rsMax));
            rxMaxTv.setText(String.valueOf(rxMax));
            ryMaxTv.setText(String.valueOf(ryMax));
            rzMaxTv.setText(String.valueOf(rzMax));

            rsMinTv.setText(String.valueOf(rsMin));
            rxMinTv.setText(String.valueOf(rxMin));
            ryMinTv.setText(String.valueOf(ryMin));
            rzMinTv.setText(String.valueOf(rzMin));

            recordT = (double)(System.currentTimeMillis() - recordStartTime)/1000.0;


        }else if(recordYn && recordT > 5) {

//            sService.updateChart(recordList);
//            xService.updateChart(recordList);
//            yService.updateChart(recordList);
//            zService.updateChart(recordList);
            fileClose();
            recordYn = false;
            sService.stop();
            xService.stop();
            yService.stop();
            zService.stop();
        }
    }

/*
    public void recordCreate(){
        Log.d("sub layout", "click");

        sService.setXYMultipleSeriesDataset();
        sService.setXYMultipleSeriesRenderer("Sound");
        sView = sService.getGraphicalView();
        sSubChartLayout.addView(sView, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        sService.SeriesChange("sS");


        xService.setXYMultipleSeriesDataset();
        xService.setXYMultipleSeriesRenderer("X");
        xView = xService.getGraphicalView();
        xSubChartLayout.addView(xView, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        xService.SeriesChange("xS");

        yService.setXYMultipleSeriesDataset();
        yService.setXYMultipleSeriesRenderer("Y");
        yView = yService.getGraphicalView();
        ySubChartLayout.addView(yView, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        yService.SeriesChange("yS");

        zService.setXYMultipleSeriesDataset();
        zService.setXYMultipleSeriesRenderer("Z");
        zView = zService.getGraphicalView();
        zSubChartLayout.addView(zView, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        zService.SeriesChange("zS");

    }
*/
    // 블루투스 사용 가능상태 판단
    public boolean canUseBluetooth() {
        // 블루투스 어댑터를 구한다
        mBA = BluetoothAdapter.getDefaultAdapter();

        //mTextMsg.setText("Device is exist");
//        Toast.makeText(this, "Device is exist", Toast.LENGTH_SHORT).show();
        showMessage("Device is exist");
        // 블루투스 활성화 상태라면 함수 탈출
        if( mBA.isEnabled() ) {
            //mTextMsg.append("\nDevice can use");
//            Toast.makeText(this, "블루투스를 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
            showMessage("블루투스를 사용할 수 있습니다.");
            return true;
        }

        // 사용자에게 블루투스 활성화를 요청한다
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, ACTION_ENABLE_BT);
        return false;
    }

    // 블루투스 활성화 요청 결과 수신
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == ACTION_ENABLE_BT ) {
            // 사용자가 블루투스 활성화 승인했을때
            if( resultCode == RESULT_OK ) {
                //mTextMsg.append("\nDevice can use");
                // 페어링된 원격 디바이스 목록 구하기
//                Toast.makeText(this, "블루투스를 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
                showMessage("블루투스를 사용할 수 있습니다.");
                getParedDevice();
            }
            // 사용자가 블루투스 활성화 취소했을때
            else {
                //mTextMsg.append("\nDevice can not use");
//                Toast.makeText(this, "블루투스를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                showMessage("블루투스를 사용할 수 없습니다.");

            }
        }
    }

    // 다른 디바이스에게 자신을 검색 허용
    public void setDiscoverable() {
        // 현재 검색 허용 상태라면 함수 탈출
        if( mBA.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE )
            return;
        // 다른 디바이스에게 자신을 검색 허용 지정
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(intent);
    }

    // 페어링된 원격 디바이스 목록 구하기
    public void getParedDevice() {
        if( mSThread != null ) return;
        // 서버 소켓 접속을 위한 스레드 생성 & 시작
        mSThread = new ServerThread();
        mSThread.start();

        // 다른 디바이스에 자신을 노출
        setDiscoverable();
    }

    // 클라이언트 소켓 생성을 위한 스레드
    private class ClientThread extends Thread {
        private BluetoothSocket mmCSocket;

        // 원격 디바이스와 접속을 위한 클라이언트 소켓 생성
        public ClientThread(BluetoothDevice  device) {
            try {
                mmCSocket = device.createInsecureRfcommSocketToServiceRecord(BLUE_UUID);
            } catch(IOException e) {
                showMessage("Create Client Socket error");
                return;
            }
        }

        public void run() {
            // 원격 디바이스와 접속 시도
            try {
                mmCSocket.connect();
            } catch(IOException e) {
                showMessage("Connect to server error");
                // 접속이 실패했으면 소켓을 닫는다
                try {
                    mmCSocket.close();
                } catch (IOException e2) {
                    showMessage("Client Socket close error");
                }
                return;
            }

            // 원격 디바이스와 접속되었으면 데이터 송수신 스레드를 시작
            onConnected(mmCSocket);
        }

        // 클라이언트 소켓 중지
        public void cancel() {
            try {
                mmCSocket.close();
            } catch (IOException e) {
                showMessage("Client Socket close error");
            }
        }
    }

    // 서버 소켓을 생성해서 접속이 들어오면 클라이언트 소켓을 생성하는 스레드
    private class ServerThread extends Thread {
        private BluetoothServerSocket mmSSocket;

        // 서버 소켓 생성
        public ServerThread() {
            try {
                mmSSocket = mBA.listenUsingInsecureRfcommWithServiceRecord(BLUE_NAME, BLUE_UUID);
            } catch(IOException e) {
                showMessage("Get Server Socket Error");
            }
        }

        public void run() {
            BluetoothSocket cSocket = null;

            // 원격 디바이스에서 접속을 요청할 때까지 기다린다
            try {
                cSocket = mmSSocket.accept();
            } catch(IOException e) {
                showMessage("Socket Accept Error");
                return;
            }

            // 원격 디바이스와 접속되었으면 데이터 송수신 스레드를 시작
            onConnected(cSocket);
        }

        // 서버 소켓 중지
        public void cancel() {
            try {
                mmSSocket.close();
            } catch (IOException e) {
                showMessage("Server Socket close error");
            }
        }
    }

    // 메시지를 화면에 표시
    public void showMessage(String strMsg) {
        // 메시지 텍스트를 핸들러에 전달
        Message msg = Message.obtain(mHandler, 0, strMsg);
        mHandler.sendMessage(msg);
        Log.d("tag1", strMsg);
    }

    // 메시지 화면 출력을 위한 핸들러
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                String strMsg = (String)msg.obj;
//                mTextMsg.setText(strMsg);
                Toast.makeText(MainActivity.this, strMsg, Toast.LENGTH_SHORT).show();
            }else if(msg.what == 9){

                String strMsg = (String)msg.obj;
                if(strMsg.equals("START")){
                    startBtn.performClick();
                }else if(strMsg.equals("STOP")){
                    stopBtn.performClick();
                }else if(strMsg.equals("PAUSE")){
                    pauseBtn.performClick();
                }else if(strMsg.equals("RECORD")){
                    recordBtn.performClick();

                }
            }
        }
    };

    // 원격 디바이스와 접속되었으면 데이터 송수신 스레드를 시작
    public void onConnected(BluetoothSocket socket) {
        showMessage("Socket connected");

        // 데이터 송수신 스레드가 생성되어 있다면 삭제한다
        if( mSocketThread != null )
            mSocketThread = null;
        // 데이터 송수신 스레드를 시작
        mSocketThread = new SocketThread(socket);
        mSocketThread.start();

        blueToothYn = true;

        if(startTime != 0){
            mSocketThread.write("M,START,E");
        }
    }

    // 데이터 송수신 스레드
    private class SocketThread extends Thread {
        private final BluetoothSocket mmSocket; // 클라이언트 소켓
        private InputStream mmInStream; // 입력 스트림
        private OutputStream mmOutStream; // 출력 스트림
        private Button startBtn, stopBtn, pauseBtn;

        public SocketThread(BluetoothSocket socket) {
            mmSocket = socket;

            // 입력 스트림과 출력 스트림을 구한다
            try {
                mmInStream = socket.getInputStream();
                mmOutStream = socket.getOutputStream();
            } catch (IOException e) {
                showMessage("Get Stream error");
            }
        }

        // 소켓에서 수신된 데이터를 화면에 표시한다
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    // 입력 스트림에서 데이터를 읽는다
                    bytes = mmInStream.read(buffer);
                    String strBuf = new String(buffer, 0, bytes);
                    //showMessage("Receive: " + strBuf);
                    //Toast.makeText(MainActivity.this, strBuf, Toast.LENGTH_SHORT).show();

                    if(strBuf.equals("START")){
                        //blueToothYn = true;
//                        startBtnClick();
                        Message msg = Message.obtain(mHandler, 9, "START");
                        mHandler.sendMessage(msg);
                    }else if(strBuf.equals("STOP")){
//                        stopBtnClick();
                        Message msg = Message.obtain(mHandler, 9, "STOP");
                        mHandler.sendMessage(msg);
                    }else if(strBuf.equals("PAUSE")){
//                        pauseBtnClick();
                        Message msg = Message.obtain(mHandler, 9, "PAUSE");
                        mHandler.sendMessage(msg);
                    }else if(strBuf.equals("RECORD")){
                        //recordYn = true;
                        Message msg = Message.obtain(mHandler, 9, "RECORD");
                        mHandler.sendMessage(msg);
                    }


                    SystemClock.sleep(1);
                } catch (IOException e) {
                    showMessage("Socket disconneted");
                    blueToothYn = false;
                    break;
                }
            }
        }

        // 데이터를 소켓으로 전송한다
        public void write(String strBuf) {
            try {
                // 출력 스트림에 데이터를 저장한다
                byte[] buffer = strBuf.getBytes();
                mmOutStream.write(buffer);
                //showMessage("Send: " + strBuf);
            } catch (IOException e) {
                showMessage("Socket write error");
            }
        }
    }

    public void fileCreate(){

        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = sdfNow.format(new Date(System.currentTimeMillis())) + ".txt";


        File file = new File(path + time);

        try {
            fos = new FileOutputStream(file); // 파일 생성
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void fileWrite(String str) {

        try {
            fos.write((str).getBytes()); // 파일에 내용 저장
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fileClose(){
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public double getTime(){

        System.currentTimeMillis();

        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 디바이스 검색 중지

        // 스레드를 종료
        if( mCThread != null ) {
            mCThread.cancel();
            mCThread = null;
        }
        if( mSThread != null ) {
            mSThread.cancel();
            mSThread = null;
        }
        if( mSocketThread != null )
            mSocketThread = null;

        if (timer != null) {
            timer.cancel();
        }

        audioReader.stopReader();
        mService.stop();
    }




}