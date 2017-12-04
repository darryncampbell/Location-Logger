package com.darryncampbell.locationlogger;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by darry on 29/11/2017.
 */

//  Persist the currently recorded locations to a file (done everytime a location is pinned).  This
//  file is shared when requested via the 'Share' option

public class Storage {

    Boolean persistLocationRecordsToFile(Context context, ArrayList<LocationRecord> locationRecords, String fileName)
    {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, fileName + ".csv");
        Boolean isWriteable = isExternalStorageWritable();
        if (isWriteable)
        {
            try
            {
                path.mkdirs();
                file.createNewFile();
                if (file.exists())
                {
                    Log.i(Constants.LOG.LOG_TAG, "Log file location: " + file.getAbsolutePath());
                    String newLine = "\n";
                    OutputStream os = new FileOutputStream(file);
                    String deviceInformation = "Device Model: " + Build.MODEL + newLine +
                            "Build Number: " + Build.ID + newLine + "Serial Number: " + Build.SERIAL + newLine;
                    os.write(deviceInformation.getBytes());
                    os.write(LocationRecord.CSVHeaderRow().getBytes());
                    os.write(newLine.getBytes());
                    for (int i = 0; i < locationRecords.size(); i++)
                    {
                        os.write(locationRecords.get(i).CSVRow().getBytes());
                        os.write(newLine.getBytes());
                    }
                    os.close();

                    // Tell the media scanner about the new file so that it is
                    // immediately available to the user.
                    MediaScannerConnection.scanFile(context,
                            new String[] { file.toString() }, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> uri=" + uri);
                                }
                            });
                }
                else
                {
                    Log.e(Constants.LOG.LOG_TAG, "Error creating log file");
                    Toast.makeText(context, "Error creating log file", Toast.LENGTH_LONG).show();
                    return false;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(Constants.LOG.LOG_TAG, "Error writing " + file, e);
                Toast.makeText(context, "Error writing log file", Toast.LENGTH_LONG).show();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(Constants.LOG.LOG_TAG, "Error writing " + file, e);
                Toast.makeText(context, "Error writing log file", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else
        {
            Log.e(Constants.LOG.LOG_TAG, "External Storage was not writeable, could not create log");
            Toast.makeText(context, "External storage is not writeable, could not create log", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
