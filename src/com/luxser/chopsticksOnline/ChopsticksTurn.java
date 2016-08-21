package com.luxser.chopsticksOnline;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Basic turn data. It's just a blank data string and a turn number counter.
 * 
 * @author wolff
 * 
 */
public class ChopsticksTurn {

    public static final String TAG = "EBTurn";

    public int topLeft;
    public int topRight;
    public int bottomLeft;
    public int bottomRight;
    public int turnCounter;

    public ChopsticksTurn() {
    }

    // This is the byte array we will write out to the TBMP API.
//    public byte[] persist() {
//        JSONObject retVal = new JSONObject();
//
//        try {
//            retVal.put("data", data);
//            retVal.put("turnCounter", turnCounter);
//
//        } catch (JSONException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        String st = retVal.toString();
//
//        Log.d(TAG, "==== PERSISTING\n" + st);
//
//        return st.getBytes(Charset.forName("UTF-8"));
//    }

    // Creates a new instance of SkeletonTurn.
    static public ChopsticksTurn unpersist(byte[] byteArray) {

        if (byteArray == null) {
            Log.d(TAG, "Empty array---possible bug.");
            return new ChopsticksTurn();
        }

        String st = null;
        try {
            st = new String(byteArray, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }

        Log.d(TAG, "====UNPERSIST \n" + st);

        ChopsticksTurn retVal = new ChopsticksTurn();

        try {
            JSONObject obj = new JSONObject(st);

            if (obj.has("topLeft")) {
                retVal.topLeft = obj.getInt("topLeft");
            }
            if (obj.has("topRight")) {
                retVal.topRight = obj.getInt("topRight");
            }
            if (obj.has("bottomLeft")) {
                retVal.bottomLeft = obj.getInt("bottomLeft");
            }
            if (obj.has("bottomRight")) {
                retVal.bottomRight = obj.getInt("bottomRight");
            }
            if (obj.has("turnCounter")) {
                retVal.turnCounter = obj.getInt("turnCounter");
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return retVal;
    }

	public byte[] persist() {
		JSONObject retVal = new JSONObject();
		try {
          retVal.put("topLeft", topLeft);
          retVal.put("topRight", topRight);
          retVal.put("bottomLeft", bottomLeft);
          retVal.put("bottomRight", bottomRight);
          retVal.put("turnCounter", turnCounter);

      } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }

      String st = retVal.toString();

      Log.d(TAG, "==== PERSISTING\n" + st);

      return st.getBytes(Charset.forName("UTF-8"));
	}
}