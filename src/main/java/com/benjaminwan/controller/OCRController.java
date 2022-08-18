package com.benjaminwan.controller;

import com.alibaba.fastjson.JSONArray;
import com.benjaminwan.beans.RestBean;
import com.benjaminwan.utils.OCRUtils;
import com.benjaminwan.utils.Table_seg;
import com.benjaminwan.utils.UploadUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

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

    @PostMapping("/image01")
    public void upload(@RequestParam MultipartFile files[])throws Exception{
        for (MultipartFile file : files) {
            UploadUtils.upload(file);
        }
        Table_seg.tables();
    }

    @GetMapping("/image02")
    public void ocrTest() throws Exception {
        String resultPath = "/demo/split_result";
        File file01  = new File(resultPath);
        List<RestBean> list = new ArrayList<>();
        Map<String, List<int[]>> pairs_map = Table_seg.tables();     //这里的map键值对是（大图名字，照片里所有的配对的小图数字）
        Iterator<Map.Entry<String, List<int[]>>> entries = pairs_map.entrySet().iterator();
        while (entries.hasNext()) {
            List<String> list1 = new ArrayList<>();
            Map<String,String> map = new HashMap<>();//循环所有的照片
            Map.Entry<String, List<int[]>> entry = entries.next();
            RestBean restBean = new RestBean();
            for (int i=0;i<entry.getValue().size();i++){          //循环所有的配对，并把配对的识别结果输入到map中
                String key= OCRUtils.ocrToString(resultPath+"/"+entry.getKey()+"/"+entry.getKey()+"-part-"+entry.getValue().get(i)[0]+".jpg");
                String value =null;
                if(entry.getValue().get(i)[1]==-1){
                    value = "-1";
                }
                else{
                    value= OCRUtils.ocrToString(resultPath+"/"+entry.getKey()+"/"+entry.getKey()+"-part-"+entry.getValue().get(i)[1]+".jpg");
                }
                map.put(key,value);
                list1.add(key);
                restBean.setMap(map);
                restBean.setList(list1);
            }
            list.add(restBean);     //所有大图的所有值的配对
            //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().get(0)[0]);
        }
//        Object o = JSONArray.toJSON(list);
//        System.out.println(o.toString());
//        for (Map<String, String> stringStringMap : list) {
//            for (Map.Entry<String, String> stringStringEntry : stringStringMap.entrySet()) {
//                System.out.println(stringStringEntry.getKey()+" : "+stringStringEntry.getValue());
//            }
//        }
        for (RestBean restBean : list) {
            Map<String, String> map = restBean.getMap();
            List<String> list1 = restBean.getList();
            for (int i = 0; i < list1.size(); i++) {
                String s = map.get(list1.get(i));
                System.out.println(list1.get(i)+" : "+s);
            }
        }
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
