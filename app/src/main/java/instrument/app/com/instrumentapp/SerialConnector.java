package instrument.app.com.instrumentapp;

/**
 * Created by youyounggyu on 15. 3. 9..
 */

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by youyounggyu on 15. 3. 8..
 */
public class SerialConnector {
    public static final String tag = "SerialConnector";

    private Context mContext;

    private UsbDevice mDevice;
    private UsbSerialDriver mDriver;
    private UsbDeviceConnection mDevConn;


    public static final int TARGET_VENDOR_ID = 9025;
    public static final int BAUD_RATE = 115200;

    public SerialConnector(Context c) {
        mContext = c;
    }

    public boolean initialize() {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        Log.d(tag, "Device Count : " + deviceList.size());

        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            StringBuilder sb = new StringBuilder();
            UsbDevice device = deviceIterator.next();
            sb.append(" DName : ").append(device.getDeviceName()).append("\n")
                    .append(" DID : ").append(device.getDeviceId()).append("\n")
                    .append(" VID : ").append(device.getVendorId()).append("\n")
                    .append(" PID : ").append(device.getProductId()).append("\n")
                    .append(" IF Count : ").append(device.getInterfaceCount()).append("\n");

            if (device.getVendorId() == TARGET_VENDOR_ID) {
                mDevice = device;
            }
        }

        if (mDevice != null) {
            try {
                mDriver = UsbSerialProber.acquire(manager, mDevice);
            } catch (Exception allEx) {
                return false;
            }


            if (mDriver != null) {
                try {
                    mDriver.open();
                    mDriver.setBaudRate(BAUD_RATE);
                    return true;
                } catch (IOException e) {
                    return false;
                } catch (Exception allEx) {
                    return false;
                } finally {
                }

            } else {
                return false;
            }
        }
        return false;
    }

    public void finalize() {
        try {
            if (mDriver != null)
                mDriver.close();
            mDevice = null;
            mDriver = null;

        } catch (Exception ex) {

        }
    }

    public String[] parseData(String SensorData) {

        String tmp = SensorData;

        // 공백 엔터 제거
        tmp = tmp.replace("\n","");
        tmp = tmp.replace(" ","");

        // 데이터 구분 ::x,y,z::
        String[] datatmp = tmp.split("::");


        if (datatmp.length == 3) {
            // x,y,z 구분
            String[] data = datatmp[1].split(",");

            if (data.length == 3) {
                // x : data[0], y : data[1], z : data[2]
                return data;
            } else {
                Log.e("###", "data length : " + data.length);
                return null;
            }

        } else {
            Log.e("###", "datatmp length : " + datatmp.length);
            return null;
        }
        //return null;
    }

    public String[] GetSerialData() {
        byte buffer[] = new byte[32];
        //Arrays.fill(buffer, (byte)0x00);

        boolean flag = true;
        byte cendBuffer[] = new byte[1];
        //Arrays.fill(cendBuffer, (byte)0x00);
        cendBuffer[0] = 1;

        while (flag) {
            if (mDriver != null) {
                try {
                    mDriver.write(cendBuffer, 1000);

                    int numBytesRead = mDriver.read(buffer, 1000);
                    Log.d(tag, "run : read bytes = " + numBytesRead);

                    if (numBytesRead > 0) {

                        String str = new String(buffer, 0, 32);

                        String[] receiveData = parseData(str);


                        if (receiveData != null) {
                            return receiveData;
                        } else {
                            try{
                                Thread.sleep(5); // 1초 = 1000밀리초
                            } catch (InterruptedException ignore) {}
                        }

                    } else {
                        try{
                            Thread.sleep(5); // 1초 = 1000밀리초
                        } catch (InterruptedException ignore) {}
                    }
                } catch (IOException e) {
                    Log.d(tag, "IOException - mDriver.read");
                    e.printStackTrace();
                    //return null;

                }


            } else {
                return null;
            }
        }
        return null;
    }
}