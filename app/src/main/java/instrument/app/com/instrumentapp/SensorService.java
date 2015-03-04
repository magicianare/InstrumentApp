package instrument.app.com.instrumentapp;

/**
 * Created by are-mac on 2015. 3. 4..
 */
public class SensorService {

    public int mMicDataDB = 0 ;

    public void SetMicData(int data) {
        mMicDataDB = data;
    }
    public double getS(){
        return mMicDataDB;
        //return Double.valueOf(String.format("%1.2f",Math.random() * 100));
    }


    public double getX(){
        return Double.valueOf(String.format("%1.2f",Math.random() * 100));
    }

    public double getY(){
        return Double.valueOf(String.format("%1.2f",Math.random() * 100));
    }

    public double getZ(){
        return Double.valueOf(String.format("%1.2f",Math.random() * 100));
    }

}
