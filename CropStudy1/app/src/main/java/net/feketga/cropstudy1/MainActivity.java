package net.feketga.cropstudy1;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String BUNDLE_STATE = "ImageViewState";

    private static final String ORIGINAL_IMAGE_PATH = "/sdcard/Pictures/city.jpg";
    private CropImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageViewState imageViewState = null;
        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_STATE)) {
            imageViewState = (ImageViewState)savedInstanceState.getSerializable(BUNDLE_STATE);
        }

        mImageView = (CropImageView) findViewById(R.id.imageView);
        mImageView.setPanLimit(CropImageView.PAN_LIMIT_CENTER);

        mImageView.setMinimumScaleType(CropImageView.SCALE_TYPE_CUSTOM);
        mImageView.setMinScale(0.05f);

//        mImageView.setImage(ImageSource.resource(R.mipmap.picture), imageViewState);
        mImageView.setImage(ImageSource.uri("file://" + ORIGINAL_IMAGE_PATH), imageViewState);

        Button cropButton = (Button) findViewById(R.id.crop_button);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImage();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ImageViewState state = mImageView.getState();
        if (state != null) {
            outState.putSerializable(BUNDLE_STATE, state);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void cropImage() {
        File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File croppedImageFile = new File("/sdcard/Pictures/", "CropStudy1.png");
        ImageCropper.saveCropToFile(
                ORIGINAL_IMAGE_PATH,
                mImageView.getCropRectangle(),
                croppedImageFile.getAbsolutePath());
    }
}
