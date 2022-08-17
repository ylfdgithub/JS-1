package com.benjaminwan.controller;

import com.benjaminwan.utils.OCRUtils;
import com.benjaminwan.utils.Table_seg;
import com.benjaminwan.utils.UploadUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/ocr")
public class OCRController {
    @PostMapping("/image")
    public Map<String,String> ocrImage(@RequestParam MultipartFile files[]) throws Exception {
        Map<String,String> map = new HashMap<>();
        for (MultipartFile file : files) {
            UploadUtils.upload(file);
        }
        Table_seg.tables();

        File result = new File("/usr/lib/result");
        File[] results = result.listFiles();
        for (File file : results) {
            String path = file.getPath();
            File[] files1 = file.listFiles();
            for (File file1 : files1) {
                String imgPath = path+"/"+file1.getName();
                String s = OCRUtils.ocrToString(imgPath);
                map.put(file1.getName(),s);
            }
        }

        return map;
    }
    @PostMapping("test")
    public String test(){
        return "success";
    }

    @GetMapping("test")
    public String test02(){
        String s = OCRUtils.ocrToString("/usr/lib/images/2.jpg");
        return s;
    }
}
