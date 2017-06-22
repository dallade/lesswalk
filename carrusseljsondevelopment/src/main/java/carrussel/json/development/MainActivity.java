package carrussel.json.development;

import android.app.Activity;
import android.os.Bundle;

import com.google.gson.Gson;

import java.io.FileReader;

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
        Gson gson = new Gson();

        try
        {
            CarruselJson carrusel = gson.fromJson(new FileReader("/sdcard/content.json"), CarruselJson.class);

            carrusel.Print();
        }
        catch (Exception e){e.printStackTrace();}
    }
}
