package com.fiaxco.nativek0x09;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("lno-lib");
    }

    private EditText filename;
    private TextView tv;
    String mFilename;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        tv = findViewById(R.id.textView);
        filename = findViewById(R.id.filename);
        Button actionButton = findViewById(R.id.button_action);
        verifyStoragePermissions(MainActivity.this);
        mFilename = "Sine.wav";
        //tv.setText(stringFromJNI());

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fname = filename.getText().toString();
                if (fname.equals("")) {
                    filename.setText(mFilename);
                    fname = mFilename;
                }
                String filepath = cacheData(fname);
                if (filepath != null) {
                    String features = lnoPredict(filepath);
                    logCoefficients(features);
                    tv.append(features.substring(0, 2000));
                }
            }
        });

    }




    private void logCoefficients(String feats) {

        File log = getExtDataFile("logcoeff.txt");
        try {
            FileOutputStream logFos = new FileOutputStream(log);
            logFos.write(feats.getBytes());
            logFos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }





    private String cacheData(String filename) {
        final File extFile = getExtDataFile(filename);
        File cacheFile = getCacheFile(filename);

        if(extFile.exists()) {
            try {
                FileInputStream extFileIPStream = new FileInputStream(extFile);
                byte[] byteBuffer = new byte[extFileIPStream.available()];
                int fileSize = extFileIPStream.read(byteBuffer);
                extFileIPStream.close();

                FileOutputStream cacheFileOPStream = new FileOutputStream(cacheFile);
                cacheFileOPStream.write(byteBuffer);
                cacheFileOPStream.close();

                String info = filename + ": " + fileSize + " bytes \n";
                tv.setText(info);
                return cacheFile.getPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    private File getCacheFile(String filename) {
        String cacheDir = getCacheDir().getPath();
        return new File(cacheDir, filename);
    }

    private File getExtDataFile(String filename) {
        String extDir = Environment.getExternalStorageDirectory().getPath();
        return new File(extDir, filename);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }





    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String lnoPredict(String filepath);

}
