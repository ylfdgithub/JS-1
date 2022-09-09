package com.benjaminwan.controller;

import com.benjaminwan.beans.OCRResult.Cell_info;
import com.benjaminwan.beans.OCRResult.OCRResult;
import com.benjaminwan.beans.OCRResult.Page_info;
import com.benjaminwan.beans.OCRResult.Position;
import com.benjaminwan.beans.TableResult.SubImage;
import com.benjaminwan.beans.TableResult.TableResult;
import com.benjaminwan.pytorchOCR.PytorchOCRUtil;
import com.benjaminwan.utils.ImageUtils;
import com.benjaminwan.utils.Table_seg;
import org.opencv.core.Mat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

@CrossOrigin
@RestController
@RequestMapping("/ocr")
public class OCRTestController {
    //上传图片，返回矫正后的图片的Base64编码
//    @PostMapping("tc-online")
//    public List<String> tcOnline(@RequestParam MultipartFile files[]) throws IOException {
//        List<String> list = new ArrayList<>();
//        for (MultipartFile file : files) {
//            ImageUtils imageUtils = new ImageUtils();
//            Mat m = imageUtils.mFileToBImageLink(file).bImageToMatLink().getMat();
//            Mat rotated_image = Table_seg.get_rotated_image(m, (int)m.size().height, (int)m.size().width);
//            BufferedImage image = imageUtils.matToBImageLink(rotated_image).getImage();
//            ByteArrayOutputStream os = new ByteArrayOutputStream();
//            ImageIO.write(image, "png", os);
//            byte[] bytes = os.toByteArray();
//            String s = Base64.encodeBase64String(bytes);
//            list.add(s);
//        }
//        return list;
//    }
    @PostMapping("tc-online")
    public void tcOnline(@RequestParam MultipartFile file ,HttpServletResponse response) throws IOException {
        String fileName = file.getName();
        ServletOutputStream outputStream = response.getOutputStream();
        ImageUtils imageUtils = new ImageUtils();
        Mat m = imageUtils.mFileToBImageLink(file).bImageToMatLink().getMat();
        Mat rotated_image = Table_seg.get_rotated_image(m, (int)m.size().height, (int)m.size().width);
        BufferedImage image = imageUtils.matToBImageLink(rotated_image).getImage();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        byte[] bytes = os.toByteArray();
        response.setHeader("Content-Disposition","attachment;filename="+fileName);
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }

    @PostMapping("tc-native")
    public String tcNative(@RequestParam String path){
        System.out.println(path);
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
        long startTime = new Date().getTime();
        ImageUtils imageUtils = new ImageUtils();
        List<Mat> mats = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        List<Page_info> page_infos = new ArrayList<>();
        for (MultipartFile file : files) {
            fileNames.add(file.getName());
            mats.add(imageUtils.mFileToBImageLink(file).bImageToMatLink().getMat());
        }
        List<TableResult> tables = Table_seg.tables(mats, fileNames);
        long midTime1  = new Date().getTime();
        System.out.println("图像转换类型和表格分割占用时间"+(midTime1-startTime)+"毫秒");
        for (int i = 0; i < tables.size(); i++) {
            Page_info page_info = new Page_info();
            List<Cell_info> cells_infos = new ArrayList<>();
            page_info.setTable_id(i).setHeads_info("无");
            List<SubImage> subImages = tables.get(i).getSubImages();
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
        long midTime2  = new Date().getTime();
        System.out.println("分组ocr占用时间"+(midTime2-midTime1)+"毫秒");
        return new OCRResult(page_infos);

    }


    //单张图片
    @PostMapping("ocr-native")
    public OCRResult ocrNative(@RequestParam String path) throws Exception {
        long startTime = new Date().getTime();
        System.loadLibrary("opencv_java430");
        List<Page_info> page_infos = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        List<Mat> mats = new ArrayList<>();
        String[] split = path.split("/");
        mats.add(imread(path));
        fileNames.add(split[split.length-1]);
        List<TableResult> tables = Table_seg.tables(mats, fileNames);
        long midTime1  = new Date().getTime();
        System.out.println("图像转换类型和表格分割占用时间"+(midTime1-startTime)+"毫秒");
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
        long midTime2  = new Date().getTime();
        System.out.println("分组ocr占用时间"+(midTime2-midTime1)+"毫秒");
        return new OCRResult(page_infos);
    }



    /**
     * hh-online
     */
//    @PostMapping("hh-online")
//    public List<List<String>> hhOnline(@RequestParam MultipartFile files[]){
//        List<Mat> mats = new ArrayList<>();
//        ImageUtils imageUtils = new ImageUtils();
//        List<String> fileNames = new ArrayList<>();
//        for (MultipartFile file : files) {
//            fileNames.add(file.getOriginalFilename());
//            mats.add(imageUtils.mFileToBImageLink(file).bImageToMatLink().getMat());
//        }
//        List<List<String>> lists = Table_seg.title_sort(mats,fileNames);
//        lists.remove(0);
//
//        return lists;
//    }

    /**
     * hh-native
     */
//    @PostMapping("hh-native")
//
//    public List<List<String>> hhNative(@RequestParam String paths){
//        System.loadLibrary("opencv_java430");
//        List<String> names = new ArrayList<>();
//        List<Mat> mats = new ArrayList<>();
//        String[] split = paths.split(",");
//        for (int i = 0; i < split.length; i++) {
//            String[] name = split[i].split("/");
//            String fName = split[split.length - 1];
//            names.add(fName);
//            mats.add(imread(split[i]));
//        }
//        List<List<String>> lists = Table_seg.title_sort(mats,names);
//        return lists;
//    }



}
