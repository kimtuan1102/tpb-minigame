package ai.fpt.minigame.utils.interfaces;


import ai.fpt.minigame.utils.model.RectModel;

public interface FaceDetectStatus {
    void onFaceLocated(RectModel rectModel);
    void onFaceNotLocated() ;
}
