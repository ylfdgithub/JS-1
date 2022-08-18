package com.benjaminwan;

import com.alibaba.fastjson.JSONArray;
import com.benjaminwan.beans.RestBean;
import com.benjaminwan.utils.OCRUtils;
import com.benjaminwan.utils.Table_seg;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.*;

@SpringBootApplication
public class KjTestApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(KjTestApplication.class, args);
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
}
