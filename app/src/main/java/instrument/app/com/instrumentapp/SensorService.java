package instrument.app.com.instrumentapp;


import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by are-mac on 2015. 3. 4..
 */
public class SensorService {
    public static final String TAG = "SencorService";


    // 마이크 데이터 변수
    public AudioReader audioReader;
    private int sampleRate = 8000;
    private int inputBlockSize = 256;
    private int sampleDecimate = 1;
    public  int mMicDataTemp= 0;



    // 가속도 센서 변수
    private Context mContext;
    private TextView mTextvw;
    private UsbDevice mDevice = null;
    private UsbSerialDriver mDriver;
    private Thread mThread;
    public Handler mHandlerSensor;
    public UsbManager manager;

    public int[] outputData;

    public SensorService(){
        outputData = new int[3];
    }


    public String[] parseData(String SensorData) {

        String tmp = SensorData;

        // 공백 엔터 제거
        tmp = tmp.replace("\n","");
        tmp = tmp.replace(" ","");

        // 데이터 구분 ::x,y,z::
        String[] datatmp = tmp.split("::");
        ;

        if (datatmp.length == 3) {
            // x,y,z 구분
            String[] data = datatmp[1].split(",");

            if (data.length == 3) {
                // x : data[0], y : data[1], z : data[2]
                return data;
            } else {
                Log.e("###", "data length : " + data.length);
            }

        } else {
            Log.e("###", "datatmp length : " + datatmp.length);
        }


        return datatmp;
    }

    public int[] getSensorData() {
        try {

            while (!Thread.currentThread().isInterrupted()) {

                int numBytesRead = 0;
                byte buffer[] = new byte[16];

                sendData("1");
                //sleep(0);

                numBytesRead = mDriver.read(buffer, 1000);
                String str = new String(buffer, 0,16);

                if (numBytesRead > 0) {
                    //Message msg1 = mHandlerSensor.obtainMessage(0, numBytesRead, 0, str);
                    //mHandlerSensor.sendMessage(msg1);
                    String tmp[] = parseData(str);

                    if (tmp == null) {
                        Log.e("SensorService","parceData is null");
                        return null;
                    }

                    for (int i = 0; i < tmp.length; i++) {
                        outputData[i] = Integer.parseInt(tmp[i]);
                    }

                    return outputData;

                } else {
                    //sleep(1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }


    public boolean Connect(UsbManager UsbMa) {

        manager = UsbMa;

        mDriver = UsbSerialProber.acquire(manager);

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        int vid_num = 9025;

        //디바이스 찾기
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){

            UsbDevice device = deviceIterator.next();

            //아두이노 찾기
            if(device.getVendorId() == vid_num) {
                mDevice = device;
                Log.e("SensorService","found device :" + device.getDeviceName() + "\n" );

            } else {
                Log.e("SensorService","Not found device :" + "\n" );
                return false;
            }

        }


        if(mDevice != null) {
            mDriver = UsbSerialProber.acquire(manager, mDevice);

            if(mDriver != null) {
                try {
                    //보더 레이트 설정
                    mDriver.setBaudRate(115200);
                    mDriver.open();
                    return true;

                } catch (IOException e) {

                    mTextvw.append("Fatal Error : User has not given permission to current device. \n");
                    e.printStackTrace();
                    return false;
                }
            }
            else {
                Log.e("SensorService","Driver is Null \n");
                return false;

            }


        } else {
            Log.e("SensorService","cannot find target device \n");
            return false;
        }
    }

    public void DisConnect() {
        if (mDriver != null) {
            try {

                mDriver.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendData(String data){
        try {

            String message = data;

            String tokens[] = message.split(",");

            byte[] send = new byte[tokens.length];

            int index = 0;
            for(String token : tokens ) {
                send[index++] = (byte)Integer.parseInt(token,16);
            }

            mDriver.write(send,1000);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
