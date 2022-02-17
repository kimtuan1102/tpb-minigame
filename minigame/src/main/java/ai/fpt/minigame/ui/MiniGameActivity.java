package ai.fpt.minigame.ui;

import static ai.fpt.minigame.utils.base.MiniGame.GENDER;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.face.Face;

import java.io.IOException;

import ai.fpt.minigame.R;
import ai.fpt.minigame.utils.base.BaseActivity;
import ai.fpt.minigame.utils.common.CameraSource;
import ai.fpt.minigame.utils.common.CameraSourcePreview;
import ai.fpt.minigame.utils.common.FrameMetadata;
import ai.fpt.minigame.utils.common.GraphicOverlay;
import ai.fpt.minigame.utils.interfaces.FaceDetectStatus;
import ai.fpt.minigame.utils.interfaces.FrameReturn;
import ai.fpt.minigame.utils.model.RectModel;
import ai.fpt.minigame.utils.visions.FaceDetectionProcessor;

public class MiniGameActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback, FrameReturn, FaceDetectStatus {

    private static final String TAG = "Mini Game Activity";

    Bitmap originalImage = null;
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private ImageView faceFrame;
    private ImageView test;
    private Button takePhoto;
    private Bitmap croppedImage = null;

    public static void start(Activity activity, Bundle extras) {
        Intent starter = new Intent(activity, MiniGameActivity.class);
        starter.putExtras(extras);
        activity.startActivity(starter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_game);
        test = findViewById(R.id.test);
        preview = findViewById(R.id.firePreview);
        takePhoto = findViewById(R.id.takePhoto);
        faceFrame = findViewById(R.id.faceFrame);
        graphicOverlay = findViewById(R.id.fireFaceOverlay);

        if (allPermissionsGranted(this)) {
            createCameraSource();
        }
        else {
            getRuntimePermissions(this);
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String gender = extras.getString(GENDER);
            Log.d("GENDER", gender);
        }
    }

    private void createCameraSource() {
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
        try {
            FaceDetectionProcessor processor = new FaceDetectionProcessor(getResources());
            processor.frameHandler = this;
            processor.faceDetectStatus = this;
            cameraSource.setMachineLearningFrameProcessor(processor);
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: ", e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (allPermissionsGranted(this)) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //calls with each frame includes by face
    @Override
    public void onFrame(Bitmap image, Face face, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) {
        originalImage = image;
        if (face.getLeftEyeOpenProbability() < 0.4) {
            findViewById(R.id.rightEyeStatus).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.rightEyeStatus).setVisibility(View.INVISIBLE);
        }
        if (face.getRightEyeOpenProbability() < 0.4) {
            findViewById(R.id.leftEyeStatus).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.leftEyeStatus).setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onFaceLocated(RectModel rectModel) {
        faceFrame.setColorFilter(ContextCompat.getColor(this, R.color.green));
        takePhoto.setEnabled(true);

        float left = (float) (originalImage.getWidth() * 0.2);
        float newWidth = (float) (originalImage.getWidth() * 0.6);

        float top = (float) (originalImage.getHeight() * 0.2);
        float newHeight = (float) (originalImage.getHeight() * 0.6);
        croppedImage =
                Bitmap.createBitmap(originalImage,
                        ((int) (left)),
                        (int) (top),
                        ((int) (newWidth)),
                        (int) (newHeight));
        test.setImageBitmap(croppedImage);
    }

    private void takePhoto() {
//        if (croppedImage != null) {
//            String path = PublicMethods.saveToInternalStorage(croppedImage, Cons.IMG_FILE, mActivity);
//            startActivity(new Intent(mActivity, PhotoViewerActivity.class)
//                    .putExtra(IMG_EXTRA_KEY, path));
//        }
    }

    @Override
    public void onFaceNotLocated() {
        Log.d("xxxx","===OnFace Not Located");
        faceFrame.setColorFilter(ContextCompat.getColor(this, R.color.red));
        takePhoto.setEnabled(false);
    }
}