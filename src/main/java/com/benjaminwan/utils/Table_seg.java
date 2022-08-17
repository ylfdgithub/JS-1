package com.benjaminwan.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencv.core.Core.*;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;

public class Table_seg {

    public static Mat get_rotated_image(Mat image){
//        //灰度化
//        Mat grayImage = new Mat();
//        cvtColor(image, grayImage, COLOR_RGB2GRAY);
//
//        //黑白转换
//        Mat grayImage1 = new Mat();
//        bitwise_not(grayImage,grayImage1);
//
//        //二值化
//        Mat threshImage = new Mat();
//        threshold(grayImage1, threshImage,0, 255, Imgproc.THRESH_BINARY| Imgproc.THRESH_OTSU);

        //图片倾斜校正
        Mat cannyMat =image.clone();
        Imgproc.Canny(image, cannyMat, 60, 200);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        // 寻找轮廓
        findContours(cannyMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE,
                new Point(0, 0));

        // 找出匹配到的最大轮廓
        double area = Imgproc.boundingRect(contours.get(0)).area();
        int index = 0;

        // 找出匹配到的最大轮廓
        for (int i = 0; i < contours.size(); i++) {
            double tempArea = Imgproc.boundingRect(contours.get(i)).area();
            if (tempArea > area) {
                area = tempArea;
                index = i;
            }
        }
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(index).toArray());
        RotatedRect rect = Imgproc.minAreaRect(matOfPoint2f);

        // 获取矩形的四个顶点
        Point[] rectPoint = new Point[4];
        rect.points(rectPoint);
        double angle = rect.angle + 90;
        Point center = rect.center;
        Mat CorrectImg = new Mat(cannyMat.size(), cannyMat.type());
        cannyMat.copyTo(CorrectImg);

        // 得到旋转矩阵算子
        Mat matrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        Imgproc.warpAffine(image, CorrectImg, matrix, image.size(), INTER_CUBIC, BORDER_REPLICATE, new Scalar(0, 0, 0));

        return CorrectImg;
    }

    public static Mat get_outline(Mat image,int x_scaled,int y_scaled) {
        Mat img_gray = new Mat();
        cvtColor(image,img_gray, COLOR_RGB2GRAY);
        Mat img_bin = new Mat();
        threshold(img_gray, img_bin,128, 255, THRESH_BINARY | THRESH_OTSU);
        bitwise_not(img_bin,img_bin);

        int kernel_length_v = img_gray.cols()/y_scaled;
        Size kernelSize1 = new Size(1,kernel_length_v);
        Mat vertical_kernel = Imgproc.getStructuringElement(MORPH_RECT,kernelSize1);
        Mat im_temp1 =new Mat();
        Mat vertical_line_img = new Mat();
        Mat ver_lines = new Mat();
        Point anchor = new Point(-1, -1);
        int iterations=3;
        erode(img_bin,im_temp1,vertical_kernel,anchor,iterations);
        dilate(im_temp1,vertical_line_img, vertical_kernel,anchor, iterations);
        HoughLinesP(vertical_line_img, ver_lines,1, Math.PI/180, 110, 150,
                50);


        int kernel_length_h = img_gray.cols()/x_scaled;
        Size kernelSize2 = new Size(kernel_length_h,1);
        Mat horizontal_kernel = Imgproc.getStructuringElement(MORPH_RECT,kernelSize2);
        Mat im_temp2 =new Mat();
        Mat horizontal_line_img = new Mat();
        Mat hor_lines = new Mat();
        erode(img_bin,im_temp2,horizontal_kernel,anchor,iterations);
        dilate(im_temp2,horizontal_line_img, horizontal_kernel,anchor, iterations);
        HoughLinesP(horizontal_line_img, hor_lines,1, Math.PI/180, 100, 150,
                100);


        Size kernelSize3 =new Size(3,3);
        Mat kernel = Imgproc.getStructuringElement(MORPH_RECT,kernelSize3);
        Mat table_segment = new Mat();
        addWeighted(vertical_line_img, 0.5, horizontal_line_img, 0.5, 0.0,table_segment);
        Mat tabel_segment1 = new Mat();
        bitwise_not(table_segment,tabel_segment1);
        erode(tabel_segment1,table_segment, kernel, anchor,2);
        Mat tabel_segment2 = table_segment.clone();
        threshold(tabel_segment2, table_segment,0, 255, THRESH_OTSU);

        if(ver_lines==null||hor_lines==null){
            return img_gray;
        }
        for (int y=0;y<ver_lines.rows();y++) {
            double[] vec = ver_lines.get(y, 0);

            double  x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];

            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            Imgproc.line(table_segment, start, end, new Scalar(0,0,0), 1);
	    }
        for (int y=0;y<hor_lines.rows();y++) {
            double[] vec = hor_lines.get(y, 0);

            double  x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];

            Point start = new Point(x1-10, y1);
            Point end = new Point(x2+5, y2);
            Imgproc.line(table_segment, start, end, new Scalar(0,0,0), 1);
        }

        return table_segment;

        //System.out.println(kernel_length_v);
    }

    public static List<int[]> get_boundings(Mat table_segment) {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierachy = new Mat();
        findContours(table_segment,contours,hierachy,RETR_CCOMP,CHAIN_APPROX_SIMPLE);

        List<int[]> bounds = new ArrayList<int[]>();
        MatOfPoint2f scr = new MatOfPoint2f();
        MatOfPoint2f dst = new MatOfPoint2f();
        MatOfPoint2f dst1 = new MatOfPoint2f();
        for (int i=0;i<contours.size();i++){
            contours.get(i).convertTo(scr, CvType.CV_32F);
            Imgproc.approxPolyDP(scr,dst,3,true);
            dst.convertTo(dst1,CvType.CV_32S);
            Rect r = Imgproc.boundingRect(dst1);
            if(r.width>40&&r.height>40){
                int[] arr = new int[]{r.x, r.y, r.width, r.height};
                bounds.add(arr);
            }
        }
        Collections.reverse(bounds);
        if (bounds.size()>2){
            int num=0;
            for (int i=0;i<bounds.size();i++){
                num+=1;
                if(bounds.get(i)[0]<5) break;
            }
            for(int j=0;j<num;j++){
                bounds.remove(0);
            }
        }
        //System.out.println(bounds.size());
        return bounds;
    }

    public static List<List<int[]>> get_resorted_bounds(List<int[]> bounds) {
        List<List<int[]>> boundings = new ArrayList<>();
        List<int[]> line_bounds = new ArrayList<int[]>();
        int it=0;
        for (int i=0;i<bounds.size();i++){
            if (line_bounds.size()<1){
                line_bounds.add(bounds.get(i));
            }
            else {
                int x=bounds.get(i)[0];
                int y=bounds.get(i)[1];
                int x_=line_bounds.get(line_bounds.size()-1)[0];
                int y_=line_bounds.get(line_bounds.size()-1)[1];
                if(Math.abs(y_-y)<20){
                    line_bounds.add(bounds.get(i));
                }
                else {
                    Collections.sort(line_bounds, (a,b)->{
                        return a[0] - b[0];
                    });
                    List<int[]> line_bounds_copy = ListUtils.deepCopy(line_bounds);
                    boundings.add(line_bounds_copy);
                    line_bounds.clear();
                    line_bounds.add(bounds.get(i));
                    it+=1;
                }
            }

        }
        Collections.sort(line_bounds, (a,b)->{
            return a[0] - b[0];
        });
        boundings.add(line_bounds);

        return boundings;
    }

    public static List<List<int[]>> add_head_tail_bound(List<List<int[]>> bounds,int width,int height) {
        int _x = bounds.get(0).get(0)[0];
        int _y = bounds.get(0).get(0)[1];
        int _w = bounds.get(0).get(0)[2];
        int _h = bounds.get(0).get(0)[3];
        int newx = 5;
        int newy = _y + _h + 5;
        int neww = width - 10;
        int newh = height - newy - 2 - 2;
        List<int[]> list = new ArrayList<>();
        List<int[]> list_copy = new ArrayList<>();
        int[] num = new int[]{newx,newy,neww,newh};
        list.add(num);
        list_copy=ListUtils.deepCopy(list);
        bounds.add(0,list_copy);
        list.clear();

        _x = bounds.get(bounds.size()-1).get(0)[0];
        _y = bounds.get(bounds.size()-1).get(0)[1];
        _w = bounds.get(bounds.size()-1).get(0)[2];
        _h = bounds.get(bounds.size()-1).get(0)[3];
        int x_ = 10;
        int y_ = 10;
        int w_ = width - 10 - 10;
        int h_ = _y - 4 - 10;
        num = new int[]{x_, y_, w_, h_};
        list.add(num);
        list_copy=ListUtils.deepCopy(list);
        bounds.add(bounds.size(),list_copy);
        list.clear();
        return bounds;
    }

    public static Mat draw_rectangle(Mat image_x,List<List<int[]>> bounds,String dirname,String f_name,String result_path) {
        int count = 0;
        int coordinate_count = bounds.size();
        int left_point_x = bounds.get(1).get(0)[0]-8;
        int left_point_y = bounds.get(1).get(0)[1]-8;
        int right_point_x = bounds.get(coordinate_count - 2).get(1)[0]-5;
        int right_point_y = bounds.get(coordinate_count - 2).get(1)[1]-5;

        int title_width = right_point_y - left_point_y + bounds.get(coordinate_count - 2).get(1)[3];
        int title_height = left_point_y;

        Rect R = new Rect(0, 0, image_x.width(), title_height);
        Mat title = image_x.submat(R);
        cvtColor(title,title,COLOR_BGR2GRAY);
//        adaptiveThreshold(title,title,255,ADAPTIVE_THRESH_GAUSSIAN_C,THRESH_BINARY,15,-10);
        int row = title.rows();
        int col = title.cols();
        int sum=0;
        for (int i=0;i<=row;i++){
            for (int j=0;j<=col;j++){
                if(title.get(i,j)!=null){
                    sum+=1;
                }
            }
        }
        if (sum>2000){
            imwrite(result_path+"/"+f_name+"/"+f_name+"-part-0.jpg",title);
            count=1;
        }

//        imwrite(result_path+"\\1.jpg",title);

        for (int i=1;i<bounds.size()-1;i++){
            for (int j=0;j<bounds.get(i).size();j++){
                int x = bounds.get(i).get(j)[0];
                int y = bounds.get(i).get(j)[1];
                int w = bounds.get(i).get(j)[2];
                int h = bounds.get(i).get(j)[3];
                //rectangle(image_x, new Point(x, y), new Point(x + w, y + h), new Scalar(255, 0, 0), 2);
                //imwrite("D:\\IDEA\\Project\\table_split\\result\\1.jpg",image_x);
                Rect rect = new Rect(x,y,w,h);
                Mat ROI = new Mat(image_x,rect);
//                if(count>=0&&count<10){
//                    imwrite(result_path+"/"+f_name+"/"+f_name+"-part-"+String.valueOf(count)+".jpg",ROI);
//                    count+=1;
//                }
//                else {
//                    imwrite(result_path+"/"+f_name+"/"+f_name+"-part-0"+String.valueOf(count)+".jpg",ROI);
//                    count+=1;
//                }
                imwrite(result_path+"/"+f_name+"/"+f_name+"-part-"+String.valueOf(count)+".jpg",ROI);
                count+=1;
            }
        }

        File file = new File(result_path);
        File[] fs = file.listFiles();
        String dirname1 = null;

        for(File f:fs) {
            String file_name = f.getName();
            dirname1 = result_path + "/" + file_name;
            File file1 = new File(dirname1);
            File[] fs1 = file1.listFiles();
            for(File f1:fs1){
                String file_name1= f1.getName();
                Mat image = imread(result_path+"/"+file_name+"/"+file_name1);
                Mat final_image = new Mat(image.rows(),image.cols(),image.type());
                int top = (int) (0.2*image.rows());
                int bottom = (int) (0.2*image.rows());
                int left = (int) (0.2*image.cols());
                int right = (int) (0.2*image.cols());
                copyMakeBorder(image, final_image, top, bottom, left, right, BORDER_REPLICATE);
                imwrite(result_path+"/"+file_name+"/"+file_name1,final_image);
            }
        }

        return image_x;
    }


    public static void tables() throws Exception {

        String img_path = "/usr/lib/images";
        String result_path = "/usr/lib/result";
        File file = new File(img_path);
        File[] fs = file.listFiles();
        String dirname = null;

        for(File f:fs){
            String f_name = f.getName();
            if(!f.isDirectory())
            {
                dirname = result_path + "/" + f_name;
                File path = new File(dirname);
                if ( !path.exists()){
                    path.mkdir();
                    System.out.println("创建文件夹路径为："+ path);
                }
            }
            System.loadLibrary("opencv_java430");
            String image_path = img_path + "/" + f.getName();
            Mat image = imread(image_path);

            if (image.empty()) {
                throw new Exception("image is empty");
            }

            Size size = image.size();
            double w = size.width;
            double h = size.height;
            int width = (int)w;
            int height = (int)h;

            Mat image_rotated = get_rotated_image(image);

            int x_scaled=80;
            int y_scaled=80;
            Mat image_contour = get_outline(image_rotated.clone(), x_scaled, y_scaled);

            List<int[]> boundings = get_boundings(image_contour);

            List<List<int[]>> sorted_bounds =new ArrayList<>();
            List<List<int[]>> bounds =new ArrayList<>();
            if(boundings.size()>2){
                sorted_bounds = get_resorted_bounds(boundings);
                bounds = add_head_tail_bound(sorted_bounds, width=width, height=height);
            }

            Mat img_rect = draw_rectangle(image_rotated.clone(), bounds, dirname, f_name, result_path);

        }
        //imwrite("D:\\IDEA\\Project\\table_split\\result\\result.jpg",big_image);
    }
}
