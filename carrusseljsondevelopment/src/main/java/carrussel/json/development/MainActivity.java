package carrussel.json.development;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        test();
    }

    private void test()
    {
        try
        {
            AssetsJson assetsJson = AssetsJson.createFromFile("/sdcard/test.json");
            //Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE).create();
//            Gson gson = new Gson();
//            AssetsJson dummy = AssetsJson.createDummy();
//            String json = gson.toJson(dummy);
//            Log.d("elazarkin", "" + json);
//
//            Type       fooType    = new TypeToken<AssetsJson>() {}.getType();
//            AssetsJson assetsJson = gson.fromJson(new FileReader("/sdcard/test.json"), AssetsJson.class);

            //carrusel.Print();
            //CarruselJson carrusel = gson.fromJson(new FileReader("/sdcard/content.json"), CarruselJson.class);
        }
        catch (Exception e){e.printStackTrace();}

    }

}
