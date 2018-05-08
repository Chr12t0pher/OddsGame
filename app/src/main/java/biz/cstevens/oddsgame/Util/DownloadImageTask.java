package biz.cstevens.oddsgame.Util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView image;

    public DownloadImageTask(ImageView image) {
        this.image = image;
    }

    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        Bitmap out = null;
        try {
            InputStream inputStream = new URL(url).openStream();
            out = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            Log.e("ImageDownload", e.getMessage());
        }
        return out;
    }

    protected void onPostExecute(Bitmap result) {
        image.setImageBitmap(result);
    }
}
