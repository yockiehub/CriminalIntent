package com.android.yockie;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by palme_000 on 07/31/16.
 */
public class CrimeCameraFragment extends Fragment {
    private static final String TAG = "CrimeCameraFragment";
    public static final String EXTRA_PHOTO_FILENAME = "com.android.yockie.photoFilename";
    public static final String EXTRA_PHOTO_ORIENTATION = "com.android.yockie.photoOrientation";


    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private View mProgressContainer;

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback(){

        /**
         * Called as near as possible to the moment when a photo is captured
         * from the sensor.  This is a good opportunity to play a shutter sound
         * or give other feedback of camera operation.  This may be some time
         * after the photo was triggered, but some time before the actual data
         * is available.
         */
        public void onShutter() {
            //Display the progress indicator
            mProgressContainer.setVisibility(View.VISIBLE);
        }
    };

    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //Create a filename
            String filename = UUID.randomUUID().toString() + ".jpg";
            //Save the jpeg to disk
            FileOutputStream fos = null;
            boolean success = true;
            try{
                fos = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                fos.write(data);
            }catch(Exception e){
                Log.e(TAG, "Error writing file " + filename, e);
                success = false;
            }finally{
                try{
                    if(fos != null){
                        fos.close();
                    }
                }catch (Exception e){
                    Log.e(TAG, "Error closing file " + filename, e);
                    success = false;
                }
            }if (success){
                int rotation = getActivity().getResources().getConfiguration().orientation;
                Intent i = new Intent();
                i.putExtra(EXTRA_PHOTO_FILENAME, filename);
                i.putExtra(EXTRA_PHOTO_ORIENTATION, rotation);
                getActivity().setResult(Activity.RESULT_OK, i);
            }else{
                getActivity().setResult(Activity.RESULT_CANCELED);
            }
            getActivity().finish();
        }
    };


    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){

        View v = inflater.inflate(R.layout.fragment_crime_camera, parent, false);
        mProgressContainer = v.findViewById(R.id.crime_camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);

        Button takePictureButton = (Button) v.findViewById(R.id.crime_camera_takePictureButon);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mCamera != null){
                    mCamera.takePicture(mShutterCallback, null, mJpegCallback);
                }
            }
        });
        mSurfaceView = (SurfaceView)v.findViewById(R.id.crime_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();

        //setType() and SURFACE_TYPE_PUSH_BUFFERS are both deprecated, but necessary
        //to work on pre-honeycomb devices
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //This interface listens for events int he lifecycle of a surface so that
        //you can coordinate the surface with its client (camera, in this case)
        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                //Tell the camera to use this surface as its preview area.
                try{
                    if(mCamera != null){
                        mCamera.setPreviewDisplay(holder);
                    }
                }catch (IOException exception){
                    Log.e(TAG, "Error setting up preview display", exception);
                }
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mCamera != null){
                    //The surface has changed size, update the camera preview size
                    Camera.Parameters parameters = mCamera.getParameters();
                    Camera.Size s;
                    //Parameters for the preview
                    s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), width, height);
                    parameters.setPreviewSize(s.width, s.height);
                    //Paremeters for the picture to save
                    s = getBestSupportedSize(parameters.getSupportedPictureSizes(), width, height);
                    parameters.setPictureSize(s.width, s.height);
                    mCamera.setParameters(parameters);
                    try{
                        mCamera.startPreview();
                    }catch (Exception e){
                        Log.e(TAG, "Could not start preview", e);
                        mCamera.release();
                        mCamera = null;
                    }
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                //We can no longer display on this surface, so stop the preview
                if (mCamera != null){
                    mCamera.stopPreview();
                }
            }
        });

        return v;
    }

    @TargetApi(9)
    @Override
    public void onResume(){
        super.onResume();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD){
            mCamera = Camera.open(0);
        }else{
            mCamera = Camera.open();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        //Make sure that there is a Camera instance
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height){
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size s : sizes){
            int area = s.width * s.height;
            if (area > largestArea){
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }
}
