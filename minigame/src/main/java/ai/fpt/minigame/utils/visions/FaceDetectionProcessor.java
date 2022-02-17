// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package ai.fpt.minigame.utils.visions;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.List;

import ai.fpt.minigame.R;
import ai.fpt.minigame.utils.common.CameraImageGraphic;
import ai.fpt.minigame.utils.common.FrameMetadata;
import ai.fpt.minigame.utils.common.GraphicOverlay;
import ai.fpt.minigame.utils.common.VisionProcessorBase;
import ai.fpt.minigame.utils.interfaces.FaceDetectStatus;
import ai.fpt.minigame.utils.interfaces.FrameReturn;
import ai.fpt.minigame.utils.model.RectModel;


public class FaceDetectionProcessor extends VisionProcessorBase<List<Face>> implements FaceDetectStatus {

    private static final String TAG = "FaceDetectionProcessor";
    public FaceDetectStatus faceDetectStatus = null;
    private final FaceDetector detector;

    private final Bitmap overlayBitmap;

    public FrameReturn frameHandler = null;

    public FaceDetectionProcessor(Resources resources) {
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
        detector = FaceDetection.getClient(options);

        overlayBitmap = BitmapFactory.decodeResource(resources, R.drawable.clown_nose);
    }

    @Override
    public void stop() {
        detector.close();
    }

    @Override
    protected Task<List<Face>> detectInImage(InputImage image) {
        return detector.process(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<Face> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.get(i);
            if (frameHandler != null) {
                frameHandler.onFrame(originalCameraImage, face, frameMetadata, graphicOverlay);
            }
            int cameraFacing =
                    frameMetadata != null ? frameMetadata.getCameraFacing() :
                            Camera.CameraInfo.CAMERA_FACING_BACK;
            FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay, face, cameraFacing, overlayBitmap);
            faceGraphic.faceDetectStatus = this;
            graphicOverlay.add(faceGraphic);
        }
        if (faces.size() < 1) {
            faceDetectStatus.onFaceNotLocated();
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }

    @Override
    public void onFaceLocated(RectModel rectModel) {
        if (faceDetectStatus != null) faceDetectStatus.onFaceLocated(rectModel);
    }

    @Override
    public void onFaceNotLocated() {
        if (faceDetectStatus != null) faceDetectStatus.onFaceNotLocated();
    }
}
