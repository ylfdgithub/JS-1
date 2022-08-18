package com.benjaminwan.utils;

import com.benjaminwan.ocrlibrary.OcrEngine;
import com.benjaminwan.ocrlibrary.OcrResult;
import com.benjaminwan.ocrlibrary.TextBlock;

import java.util.ArrayList;

public class OCRUtils {
    public static String ocrToString(String filePath){
        String jniLibDir = System.getProperty("java.library.path");
        System.loadLibrary("OcrLiteNcnn");

        String modelsDir = "/demo/models";
        String detName = "dbnet_op";
        String clsName = "angle_op";
        String recName = "crnn_lite_op";
        String keysName = "keys.txt";

        int numThread = 4;
        int padding = 50;
        int maxSideLen = 1024;
        float boxScoreThresh = 0.6f;
        float boxThresh = 0.3f;
        float unClipRatio = 2.2f;
        boolean doAngle = false;
        boolean mostAngle = false;
        int gpuIndex = 0;

        OcrEngine ocrEngine = new OcrEngine();
        ocrEngine.setNumThread(numThread);

        ocrEngine.initLogger(
                false,
                false,
                false
        );
//        ocrEngine.enableResultText(filePath);
        ocrEngine.setGpuIndex(gpuIndex);

        boolean initModelsRet = ocrEngine.initModels(modelsDir, detName, clsName, recName, keysName);
        if (!initModelsRet) {
            System.out.println("Error in models initialization, please check the models/keys path!");
            return "";
        }

        ocrEngine.setPadding(padding);
        ocrEngine.setBoxScoreThresh(boxScoreThresh);
        ocrEngine.setBoxThresh(boxThresh);
        ocrEngine.setUnClipRatio(unClipRatio);
        ocrEngine.setDoAngle(doAngle);
        ocrEngine.setMostAngle(mostAngle);
        //------- start detect -------
        OcrResult ocrResult = ocrEngine.detect(filePath, maxSideLen);
        ArrayList<TextBlock> textBlocks = ocrResult.getTextBlocks();
        StringBuilder builder = new StringBuilder();
        for (TextBlock textBlock : textBlocks) {
            builder.append(textBlock.getText());
        }
        return builder.toString();
    }
}
