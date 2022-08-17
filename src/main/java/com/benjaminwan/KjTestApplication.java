package com.benjaminwan;

import com.alibaba.fastjson.JSONArray;
import com.benjaminwan.utils.OCRUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class KjTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(KjTestApplication.class, args);
        String resultPath = "/demo/split_result";
        File file01  = new File(resultPath);
        List<Map<String,String>> list = new ArrayList<>();
        for (File file : file01.listFiles()) {
            String name = file.getName();
            File[] files = file.listFiles();
            Map<String,String> map = new HashMap<>();
            String key = "";
            String value = "";
            int count  = 0 ;
            for (int i = 0; i < files.length; i++) {
                count++;
                String nums="";
                if (i<10) nums = String.valueOf(i);
                else nums = "0"+String.valueOf(i);
                String imgPath = resultPath+"/"+name+"/"+name+"-part-"+nums+".jpg";
                if (count==1) key= OCRUtils.ocrToString(imgPath);
                if (count==2) value = OCRUtils.ocrToString(imgPath);
                if (count==2){
                    map.put(key,value);
                    count=0;
                }
            }
            list.add(map);
        }

        Object o = JSONArray.toJSON(list);
        System.out.println(o.toString());
    }
}
