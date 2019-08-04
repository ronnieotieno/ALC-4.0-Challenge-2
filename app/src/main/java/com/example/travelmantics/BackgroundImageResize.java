package com.example.travelmantics;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.example.travelmantics.ChooseImageFragment.mOnInputListener;
import static com.example.travelmantics.ChooseImageFragment.progressBar;


public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

    private Bitmap mBitmap;
    private byte[] mUploadBytes;
    private Context context;


    public BackgroundImageResize(Context ctx, ChooseImageFragment chooseImageActivity) {
        this.chooseImageActivity = chooseImageActivity;
        context = ctx.getApplicationContext();

    }

    private ChooseImageFragment chooseImageActivity;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressBar.setVisibility(View.VISIBLE);

    }

    @Override
    protected byte[] doInBackground(Uri... params) {


        try {
            RotateBitmap rotateBitmap = new RotateBitmap();
            mBitmap = rotateBitmap.HandleSamplingAndRotationBitmap(context, params[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] bytes;
        bytes = getBytesFromBitmap(mBitmap, 70);
        return bytes;
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        super.onPostExecute(bytes);
        mUploadBytes = bytes;
        progressBar.setVisibility(View.INVISIBLE);
        chooseImageActivity.getDialog().dismiss();
        mOnInputListener.sendInput(mUploadBytes);
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

}