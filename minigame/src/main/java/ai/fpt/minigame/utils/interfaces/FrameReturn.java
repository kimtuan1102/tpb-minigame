package ai.fpt.minigame.utils.interfaces;

import android.graphics.Bitmap;

import com.google.mlkit.vision.face.Face;

import ai.fpt.minigame.utils.common.FrameMetadata;
import ai.fpt.minigame.utils.common.GraphicOverlay;

public interface FrameReturn{
    void onFrame(
            Bitmap image ,
            Face face ,
            FrameMetadata frameMetadata,
            GraphicOverlay graphicOverlay
    );
}