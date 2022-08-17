package com.benjaminwan.utils;

import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;


public class UploadUtils {
    //定义图片上传目标路径
    public static final String FILE_SPEACE = "/usr/lib/images/";
    @SneakyThrows
    //上传文件，返回上传之后的地址
    public static String upload(MultipartFile file){
        String fileName = file.getOriginalFilename();
        File imageFile = new File(FILE_SPEACE+fileName);
        try{file.transferTo(imageFile);}catch (IOException e){e.printStackTrace();}
        return FILE_SPEACE+fileName;
    }
    public static void clearDir(){
        File file = new File(FILE_SPEACE);
        File[] files = file.listFiles();
        assert files != null;
        for (File file1 : files) {
            boolean delete = file1.delete();
        }
    }
    public static int countFiles(){
        File file = new File(FILE_SPEACE);
        File[] files = file.listFiles();
        assert files != null;
        return files.length;
    }
}
