package com.android.yockie;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by palme_000 on 08/02/16.
 */
public class Photo {
    private static final String JSON_FILENAME = "filename";
    private static final String JSON_ROTATION = "rotation";
    private String mFilename;
    private int mRotation;
    /**
     * Create a Photo representing an existing file on disk
     */
    public Photo (String filename, int rotation){
        mFilename = filename;
        mRotation = rotation;
    }

    public Photo (JSONObject json) throws JSONException {
        mFilename = json.getString(JSON_FILENAME);
        mRotation = json.getInt(JSON_ROTATION);
    }

    public JSONObject toJSON() throws JSONException{
        JSONObject json = new JSONObject();
        json.put(JSON_FILENAME, mFilename);
        json.put(JSON_ROTATION, mRotation);
        return json;
    }
    public String getFilename(){
        return mFilename;
    }

    public int getOrientation(){
        return mRotation;
    }
}
