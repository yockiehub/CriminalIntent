package com.android.yockie;

import android.content.Context;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by palme_000 on 07/23/16.
 */
public class CriminalIntentJSONSerializer {
    private Context mContext;
    private String mFilename;
    //private String mExternFile;

    public CriminalIntentJSONSerializer(Context c, String f/*, String d*/){
        mContext = c;
        mFilename = f;
        //mExternFile = d;
    }

    public void saveCrimes(ArrayList<Crime> crimes) throws JSONException, IOException {
        // Build an array in JSON
        JSONArray array = new JSONArray();
        for (Crime c : crimes) {
            array.put(c.toJSON());
        }

        //Write the file to disk
        Writer writer = null;
        try{
            OutputStream out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
        }finally {
            if (writer != null){
                writer.close();
            }
        }

    }

    public ArrayList<Crime> loadCrimes() throws IOException, JSONException{
        ArrayList<Crime> crimes = new ArrayList<Crime>();
        BufferedReader reader = null;
        try{
            //Open and read the file into a read builder
            InputStream in = mContext.openFileInput(mFilename);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null){
                //Line breaks are omitted and irrelevant
                jsonString.append(line);
            }
            // Parse the JSON using JSONTokener
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();

            //Build the array of crimes from JSONObjects
            for (int i = 0; i <array.length(); i++){
                crimes.add(new Crime(array.getJSONObject(i)));
            }
        }catch (FileNotFoundException e){
            //Ignore this. This happens when starting fresh
        }finally{
            if (reader != null){
                reader.close();
            }
        }
        return crimes;
    }

    /* Checks if external storage is available for read and write
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void saveCrimesToDisk(ArrayList<Crime> crimes) throws JSONException, IOException{

        JSONArray array = new JSONArray();
        for (Crime c : crimes) {
            array.put(c.toJSON());
        }
        //Write the file to disk
        try{
            File f = new File(mContext.getExternalFilesDir(null), mExternFile);
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            //fos.write(array.toString().getBytes());
            //fos.close();
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fos);
            myOutWriter.append(array.toString());
            fos.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Crime> loadCrimesFromDisk() throws IOException, JSONException {
        ArrayList<Crime> crimes = new ArrayList<Crime>();
        BufferedReader reader = null;
        StringBuffer stringBuffer = new StringBuffer();

        try {
            File f = new File(mContext.getExternalFilesDir(null), mExternFile);
            FileInputStream fis = new FileInputStream(f);
            //DataInputStream in = new DataInputStream(fis);
            reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                //Line breaks are omitted and irrelevant
                jsonString.append(line);
            }
            reader.close();
            // Parse the JSON using JSONTokener
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
            for (int i = 0; i < array.length(); i++) {
                crimes.add(new Crime(array.getJSONObject(i)));
            }

        } catch (FileNotFoundException e) {
            //Ignore this. This happens when starting fresh
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return crimes;
    }*/
}
