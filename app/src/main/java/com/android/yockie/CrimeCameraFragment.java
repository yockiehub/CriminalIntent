package com.android.yockie;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by palme_000 on 07/31/16.
 */
public class CrimeCameraFragment extends Fragment {
    private static final String TAG = "CrimeCameraFragment";
    public static final String EXTRA_PHOTO_FILENAME = "com.android.yockie.photoFilename";

    private OrientationEventListener mOrientationEventListener;
    private Orientation mOrientation;
    public enum Orientation {
        ORIENTATION_PORTRAIT_NORMAL, ORIENTATION_PORTRAIT_INVERTED,
        ORIENTATION_LANDSCAPE_NORMAL, ORIENTATION_LANDSCAPE_INVERTED
    }

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
                byte[] dataConverted = PictureUtils.rotatePicture(data, mOrientation);
                fos.write(dataConverted);
                saveImageToExternalStorage(BitmapFactory.decodeByteArray(dataConverted,0,dataConverted.length), filename);
                //fos.write(data);
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
                Intent i = new Intent();
                i.putExtra(EXTRA_PHOTO_FILENAME, filename);
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
        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(getActivity(),
                    SensorManager.SENSOR_DELAY_NORMAL) {
                @Override
                public void onOrientationChanged(int orientation) {
                    if ((orientation >= 315) || (orientation < 45)) {
                        mOrientation = Orientation.ORIENTATION_PORTRAIT_NORMAL;
                    } else if ((orientation < 315) && (orientation >= 225)) {
                        mOrientation = Orientation.ORIENTATION_LANDSCAPE_NORMAL;
                    } else if ((orientation < 225) && (orientation >= 135)) {
                        mOrientation = Orientation.ORIENTATION_PORTRAIT_INVERTED;
                    } else if ((orientation < 135) && (orientation > 45)) {
                        mOrientation = Orientation.ORIENTATION_LANDSCAPE_INVERTED;
                    }
                }
            };
        }

        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
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
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
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

    private void saveImageToExternalStorage(Bitmap finalBitmap, String filename) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/CriminalIntent");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        //String fname = "Image-" + n + ".jpg";
        String fname = filename;
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(getActivity(), new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }



}
