package com.gbhrdt.whgonline;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.loopj.android.http.BinaryHttpResponseHandler;

public class TabFragmentVplan extends Fragment {

    private static final String TAG = "TabFragementVplan";
    //protected ImageView mImageView;
    //protected PhotoViewAttacher mAttacher;
    protected WebView mWebView;
    protected String vplanFile = "vplan";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** (non-Javadoc)
         * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
         */
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }

        View view = (LinearLayout)inflater.inflate(R.layout.tab_frag_layout, container, false);

        mWebView = (WebView) view.findViewById(R.id.webView);
        mWebView.setPadding(0, 0, 0, 0);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setScrollbarFadingEnabled(false);
        mWebView.getSettings().setBuiltInZoomControls(true);

        VplanApiClient.get("getpng.php?file=" + vplanFile, null, new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(byte[] fileData) {
                String b64Image = Base64.encodeToString(fileData, Base64.DEFAULT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    mWebView.loadData(b64Image, "image/jpeg", "base64");
                } else {
                    String pageData = "<img src=\"data:image/jpeg;base64," + b64Image + "\" />";
                    mWebView.loadData(pageData, "text/html", "utf-8");
                }
            }
        });

        return view;
    }


    private Bitmap getBitmap(byte[] fileData) {
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(fileData, 0, fileData.length, o);

            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d(TAG, "scale = " + scale + ", orig-width: " + o.outWidth + ", orig - height: " + o.outHeight);

            Bitmap b = null;
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                o.inPreferredConfig = Bitmap.Config.RGB_565;
                b = BitmapFactory.decodeByteArray(fileData, 0, fileData.length, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
                Log.d(TAG, "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                        (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeByteArray(fileData, 0, fileData.length);
            }

            Log.d(TAG, "bitmap size - width: " +b.getWidth() + ", height: " +
                    b.getHeight());
            return b;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(),e);
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Need to call clean-up
        //mAttacher.cleanup();
    }

}
