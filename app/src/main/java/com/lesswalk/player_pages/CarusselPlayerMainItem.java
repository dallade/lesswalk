package com.lesswalk.player_pages;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import com.lesswalk.pagescarussel.CarusselPageInterface;
import com.lesswalk.pagescarussel.ICarusselMainItem;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

/**
 * Created by root on 4/4/16.
 */
public class CarusselPlayerMainItem extends ICarusselMainItem
{
    private static final int MAX_PARKING_SIZE = 10;
    //
    private static final String GENERAL            = "general";
    private static final String PARKING            = "parkings";
    private static final String IMAGE              = "image";
    private static final String VIDEO              = "video";
    private static final String MAPADDR            = "map_address";
    private static final String STRADDR            = "street_address";
    private static final String INDOOR             = "indoor";
    private static final String INDOOR_ADDR        = "indoor_address";

    private static final String GENERAL_JSON_STATE[]            = {GENERAL};
    private static final String GENERAL_IMAGE_JSON_STATE[]      = {GENERAL, IMAGE};
    private static final String GENERAL_MAPADDR_JSON_STATE[]    = {GENERAL, MAPADDR};
    private static final String GENERAL_VIDEO_JSON_STATE[]      = {GENERAL, VIDEO};
    private static final String GENERAL_INDOOR_JSON_STATE[]     = {GENERAL, INDOOR};
    private static final String GENERAL_INDOORADDR_JSON_STATE[] = {GENERAL, INDOOR_ADDR};
    private static final String PARKING_JSON_STATE[]            = {GENERAL, PARKING};
    private static final String PARKING_IMAGE_JSON_STATE[]      = {GENERAL, PARKING, IMAGE};
    private static final String PARKING_MAPADDR_JSON_STATE[]    = {GENERAL, PARKING, MAPADDR};
    private static final String PARKING_STRADDR_JSON_STATE[]    = {GENERAL, PARKING, STRADDR};
    //
    private enum LOAD_MODE{ OBJECT_MODE, ARRAY_MODE };
    //
    private Context              context          = null;
    private CarusselPlayerGeneralItem  generalItem      = null;
    private CarusselPlayerParkingItem  parkingsItem[]   = null;
    private int                        parkingsItemSize = 0;
    private CarusselPlayerIndoorItem   indoorItem       = null;
    
    private File objectsDir = null;

    public CarusselPlayerMainItem(Context context)
    {
        this.context = context;
        //
        generalItem  = new CarusselPlayerGeneralItem(context);
        parkingsItem = new CarusselPlayerParkingItem[MAX_PARKING_SIZE];

        for(int i = 0; i < parkingsItem.length; i++)
        {
            parkingsItem[i] = null;
        }
    }

    public void fillContainerByItems(Vector<CarusselPageInterface> container)
    {
    	int index = 0;
    	//
    	generalItem.setIndex(index++);
        container.add(generalItem);

        for(int i = 0; i < parkingsItemSize; i++)
        {
        	parkingsItem[i].setIndex(index++);
            container.add(parkingsItem[i]);
        }
        
        if(indoorItem != null) container.add(indoorItem);
    }

    private boolean checkJsonState(String[] objArray, String[] generalJsonState)
    {
        return objArray.length >= generalJsonState.length && checkJsonState(objArray, generalJsonState, generalJsonState.length);
    }

    private boolean checkJsonState(String[] objArray, String[] generalJsonState, int size)
    {
        if(generalJsonState.length != size) return false;
        //
        for(int i = 0; i < generalJsonState.length; i++)
        {
//            Log.d("elazarkin", "checkJsonState: " + generalJsonState[i] + " =? " + objArray[i]);
            if(objArray[i] == null || !generalJsonState[i].equals(objArray[i])) return false;
        }
        return true;
    }
    
    /**
     * 
     * @param objArray
     * @param size - amount of "JSON DEPTH"
     * @param key
     * @param value
     */

    private void jsonParserStateMachine(String[] objArray, int size, String key, String value)
    {
        String text = "";

        try
        {
            //
            if (checkJsonState(objArray, PARKING_IMAGE_JSON_STATE))
            {
//                text = "JSON_PARKING_IMAGE_STATE_MACHINE(" + parkingsItemSize + ")";
                parkingsItem[parkingsItemSize - 1].initImageItem(objectsDir, key, value);
            }
            else if (checkJsonState(objArray, PARKING_MAPADDR_JSON_STATE, size))
            {
//                text = "JSON_PARKING_MAPADDR_JSON_STATE(" + parkingsItemSize + ")";
                parkingsItem[parkingsItemSize - 1].initMapAddressItem(objectsDir, key, value);
            }
            else if (checkJsonState(objArray, PARKING_STRADDR_JSON_STATE, size))
            {
//                text = "JSON_PARKING_STRADDR_JSON_STATE(" + parkingsItemSize + ")";
                parkingsItem[parkingsItemSize - 1].initStreetAddressItem(objectsDir, key, value);
            }
            else if (checkJsonState(objArray, PARKING_JSON_STATE, size))
            {
//                text = "JSON_PARKING_JSON_STATE(" + parkingsItemSize + ")";
                if (key == null && value == null)
                {
//                    Log.d("elazarkin", "will init parkingItem[" + parkingsItemSize + "]");
                    parkingsItem[parkingsItemSize] = new CarusselPlayerParkingItem(context, parkingsItemSize);
                    parkingsItemSize++;
//                    Log.d("elazarkin", "init parkingItem[" + (parkingsItemSize - 1) + "] success");
                }
                else if (value != null)
                {
                    parkingsItem[parkingsItemSize - 1].initItem(objectsDir, key, value);
                }
                else
                {
                    text = "JSON_PARKING_STATE_MACHINE_WARNING";
                }
            }
            else if (checkJsonState(objArray, GENERAL_IMAGE_JSON_STATE))
            {
//                text = "GENERAL_IMAGE_JSON_STATE";
                generalItem.initImageItem(objectsDir, key, value);
            }
            else if (checkJsonState(objArray, GENERAL_MAPADDR_JSON_STATE, size))
            {
//                text = "GENERAL_MAPADDR_JSON_STATE";
                generalItem.initMapAddressItem(objectsDir, key, value);
            }
            else if(checkJsonState(objArray, GENERAL_VIDEO_JSON_STATE, size))
            {
            	generalItem.initVideoItem(objectsDir, key, value);
            }
            else if(checkJsonState(objArray, GENERAL_INDOOR_JSON_STATE, size))
            {
            	if(indoorItem == null) indoorItem = new CarusselPlayerIndoorItem(context);
            	indoorItem.initIndoorItem(objectsDir, key, value);
            }
            else if (checkJsonState(objArray, GENERAL_INDOORADDR_JSON_STATE, size))
            {
            	((CarusselPlayerGeneralItem)generalItem).initIndoorAddressItem(objectsDir, key, value);
            }
            else if (checkJsonState(objArray, GENERAL_JSON_STATE, size))
            {
//                text = "GENERAL_JSON_STATE";
                generalItem.initItem(objectsDir, key, value);
            }
            else
            {
                text = "JSON_STATE_MACHINE_ERROR";
            }
        }
        catch (Exception e)
        {
            text +=  "Exception: " + e.getMessage();
        }

        if(text.length() > 0)
        {
            for (int i = 0; i < size; i++) {
                text += ("_" + objArray[i]);
            }
            Log.d("elazarkin", text + " - " + key + ":" + value);
        }
    }

    private void _loadJSON(JsonReader reader, String[] objArray, int index)
    {
        LOAD_MODE  mode    = LOAD_MODE.OBJECT_MODE;
        boolean    success = false;

        String     keyObj  = null;
        String     value   = null;
        //
        try
        {
            success = false;
            try
            {
                reader.beginObject();
                success = true;
            }
            catch (Exception e){}

            if(!success)
            {
            	try
                {
	                reader.beginArray();
	                mode = LOAD_MODE.ARRAY_MODE;
	                success = true;
                }
            	catch (Exception e) 
            	{
            		Log.d("elazarkin", "not success twice!");
            	}
            }
            
            if(!success)
            {
            	reader.nextString();
            }

            while (reader.hasNext())
            {
                success = false;

                try
                {
                    keyObj = reader.nextName();
                    value  = reader.nextString();
                    success = true;

                    jsonParserStateMachine(objArray, index + 1, keyObj, value);
                }
                catch (Exception e){}

                if(!success)
                {
                    jsonParserStateMachine(objArray, index + 1, keyObj, null);
                    if(keyObj != null)
                    {
                        objArray[index + 1] = keyObj;
                        _loadJSON(reader, objArray, index + 1);
                    }
                    else
                    {
                        _loadJSON(reader, objArray, index);
                    }
                    objArray[index + 1] = null;
                }
            }

            switch (mode)
            {
                case OBJECT_MODE:
                    reader.endObject();
                    break;
                case ARRAY_MODE:
                    reader.endArray();
                default:
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void loadJSON(String number)
    {
        String array[] = {GENERAL, "", "", "", "", ""};
        int    index   = 0;
        try
        {
            JsonReader reader  = null;
            //
            //objectsDir = new File(Environment.getExternalStorageDirectory(), "801BFAF1-6480-481C-92C6-619C381E0222");
            //objectsDir = new File(Environment.getExternalStorageDirectory(), "E68F1C41-656A-4DA8-A001-10093F93E000");
            objectsDir = new File("/sdcard/E68F1C41-656A-4DA8-A001-10093F93E000");
            
            reader    = new JsonReader(new InputStreamReader(new FileInputStream(new File(objectsDir, "content.json")), "UTF-8"));

            _loadJSON(reader, array, index);
            
            reader.close();

        }
        catch (Exception e)
        {
        	Log.d("elazarkin", "ERROR:" + e.getMessage());
            e.printStackTrace();
        }
    }
}