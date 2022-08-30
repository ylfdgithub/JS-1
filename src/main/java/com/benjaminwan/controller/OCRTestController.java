package com.benjaminwan.controller;

import com.benjaminwan.beans.OCRResult.OCRResult;
import com.benjaminwan.utils.ImageUtils;
import com.benjaminwan.utils.Table_seg;
import org.apache.commons.codec.binary.Base64;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

@CrossOrigin
@RestController
@RequestMapping("/ocr")
public class OCRTestController {
    //上传图片，返回矫正后的图片的Base64编码
    @PostMapping("tc-online")
    public List<String> tcOnline(@RequestParam MultipartFile files[]) throws IOException {
        List<String> list = new ArrayList<>();
        for (MultipartFile file : files) {
            ImageUtils imageUtils = new ImageUtils();
            Mat m = imageUtils.mFileToBImageLink(file).bImageToMatLink().getMat();
//            System.out.println(m.size().height+" " +m.size().width);
            Mat rotated_image = Table_seg.get_rotated_image(m, (int)m.size().height, (int)m.size().width);
            BufferedImage image = imageUtils.matToBImageLink(rotated_image).getImage();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            byte[] bytes = os.toByteArray();
            String s = Base64.encodeBase64String(bytes);
            list.add(s);
        }
        return list;
    }

    @PostMapping("tc-native")
    public String tcNative(@RequestParam String path){
//        System.out.println(path);
        System.loadLibrary("opencv_java430");
        String[] split = path.split("/");
        String fName = split[split.length - 1];
//        System.out.println(fName);
        String resultPath = "/demo/tcimage";
        Mat m = imread(path);
        Mat rotated_image = Table_seg.get_rotated_image(m, (int)m.size().height, (int)m.size().width);
        imwrite(resultPath+"/tc_"+fName,rotated_image);
        return resultPath+"/tc_"+fName;
    }


//    @PostMapping("ocr-online")
//    public OCRResult ocrOnline()



//    @PostMapping("ocr-native")



}
