package com.benjaminwan.controller;

import ai.djl.translate.TranslateException;
import com.benjaminwan.beans.HHResult.HHResult;
import com.benjaminwan.beans.OCRResult.Cell_info;
import com.benjaminwan.beans.OCRResult.OCRResult;
import com.benjaminwan.beans.OCRResult.Page_info;
import com.benjaminwan.beans.OCRResult.Position;
import com.benjaminwan.beans.TableResult.SubImage;
import com.benjaminwan.beans.TableResult.TableResult;
import com.benjaminwan.pytorchOCR.PytorchOCRUtil;
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
        System.loadLibrary("opencv_java430");
        String[] split = path.split("/");
        String fName = split[split.length - 1];
        String resultPath = "/demo/tcimage";
        Mat m = imread(path);
        Mat rotated_image = Table_seg.get_rotated_image(m, (int)m.size().height, (int)m.size().width);
        imwrite(resultPath+"/tc_"+fName,rotated_image);
        return resultPath+"/tc_"+fName;
    }


    @PostMapping("ocr-online")
    public OCRResult ocrOnline(@RequestParam MultipartFile files[]) throws Exception {
        ImageUtils imageUtils = new ImageUtils();
        List<Mat> mats = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        List<Page_info> page_infos = new ArrayList<>();
        for (MultipartFile file : files) {
            fileNames.add(file.getName());
            mats.add(imageUtils.mFileToBImageLink(file).bImageToMatLink().getMat());
        }
        List<TableResult> tables = Table_seg.tables(mats, fileNames);
        System.out.println(tables.size());
        for (int i = 0; i < tables.size(); i++) {
            Page_info page_info = new Page_info();
            List<Cell_info> cells_infos = new ArrayList<>();
            page_info.setTable_id(i).setHeads_info("无");
            List<SubImage> subImages = tables.get(i).getSubImages();
            System.out.println(subImages.size());
            for (int i1 = 0; i1 < subImages.size(); i1++) {
                SubImage subImage = subImages.get(i1);
                String result = PytorchOCRUtil.ocr(subImage.getMat());
                Position position = new Position(subImage.getPosition()[0], subImage.getPosition()[1], subImage.getPosition()[2], subImage.getPosition()[3]);
                Cell_info cell_info = new Cell_info();
                cell_info.setTable_cell_id(i1).setWords(result).setPosition(position);
                cells_infos.add(cell_info);
            }
            page_info.setCells_info(cells_infos);
            page_infos.add(page_info);
        }
        return new OCRResult(page_infos);
    }


    //单张图片
    @PostMapping("ocr-native")
    public OCRResult ocrNative(@RequestParam String path) throws Exception {
        System.loadLibrary("opencv_java430");
        List<Page_info> page_infos = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        List<Mat> mats = new ArrayList<>();
        String[] split = path.split("/");
        mats.add(imread(path));
        fileNames.add(split[split.length-1]);
        List<TableResult> tables = Table_seg.tables(mats, fileNames);
        System.out.println(tables.size());
        for (int i = 0; i < tables.size(); i++) {
            Page_info page_info = new Page_info();
            List<Cell_info> cells_infos = new ArrayList<>();
            page_info.setTable_id(i).setHeads_info("无");
            List<SubImage> subImages = tables.get(i).getSubImages();
            System.out.println(subImages.size());
            for (int i1 = 0; i1 < subImages.size(); i1++) {
                SubImage subImage = subImages.get(i1);
                String result = PytorchOCRUtil.ocr(subImage.getMat());
                Position position = new Position(subImage.getPosition()[0], subImage.getPosition()[1], subImage.getPosition()[2], subImage.getPosition()[3]);
                Cell_info cell_info = new Cell_info();
                cell_info.setTable_cell_id(i1).setWords(result).setPosition(position);
                cells_infos.add(cell_info);
            }
            page_info.setCells_info(cells_infos);
            page_infos.add(page_info);
        }
        return new OCRResult(page_infos);
    }

    /**
     * hh-online
     */

    public List<HHResult> hhOnline(@RequestParam MultipartFile files[]){

        return null;
    }

    /**
     * hh-native
     */
    public List<HHResult> hhNative(@RequestParam String dir){

        return null;
    }



}
