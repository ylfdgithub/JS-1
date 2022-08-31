package com.benjaminwan.utils;

import com.benjaminwan.beans.TableResult.SubImage;
import com.benjaminwan.beans.TableResult.TableResult;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.*;

import static org.opencv.core.Core.*;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;

public class Table_seg {

    public static Mat get_rotated_image(Mat image,double h,double w){

        cvtColor(image, image, COLOR_RGB2GRAY);

        Mat binary=new Mat();
        adaptiveThreshold(image,binary,255,ADAPTIVE_THRESH_GAUSSIAN_C,THRESH_BINARY,15,-10);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        findContours(binary,contours,hierarchy,RETR_EXTERNAL,CHAIN_APPROX_NONE);
        double area = 0;
        int index = 0;
        for (int i=0;i<contours.size();i++){
            double area_ = contourArea(contours.get(i));
            if (area_ > area){
                area = area_;
                index = i;
            }
            else continue;
        }
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(index).toArray());
        RotatedRect rect = minAreaRect(matOfPoint2f);
        double angle=rect.angle;
        if (angle > 45){
            angle = -90. + angle;
        }
        else if (angle < -45){
            angle = 90. + angle;
        }
        Point center = new Point(h/2,w/2);
        Mat m = getRotationMatrix2D(center, angle, 1.0);
        Mat rotated_image = new Mat();
        Size size = new Size(w,h);
        warpAffine(image,rotated_image,m,size, INTER_CUBIC, BORDER_REPLICATE);

        return rotated_image;
    }

    public static Mat get_outline(Mat image,int x_scaled,int y_scaled) {
        Mat img_gray = image.clone();
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

    public static boolean title_belong(Mat image_x,List<List<int[]>> bounds,String dirname,String f_name,String result_path) {
        boolean title_exist = false;
        int count = 1;
        int coordinate_count = bounds.size();
        int left_point_x = bounds.get(1).get(0)[0]-8;
        int left_point_y = bounds.get(1).get(0)[1]-8;
        int right_point_x = bounds.get(coordinate_count - 2).get(1)[0]-1;
        int right_point_y = bounds.get(coordinate_count - 2).get(1)[1]-1;

        int title_width = right_point_y - left_point_y + bounds.get(coordinate_count - 2).get(1)[3];
        int title_height = left_point_y;

        Rect R = new Rect(0, 0, image_x.width(), title_height);
        Mat title = image_x.submat(R);
        Mat title_= new Mat();
        adaptiveThreshold(title, title_, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 15, -10);

//        adaptiveThreshold(title,title,255,ADAPTIVE_THRESH_GAUSSIAN_C,THRESH_BINARY,15,-10);
        int row = title.rows();
        int col = title.cols();
        int sum=0;

        for (int i=0;i<=row;i++){
            for (int j=0;j<=col;j++){
                //System.out.println(title.get(i,j));
                if(title_.get(i,j)!=null&&title_.get(i,j)[0]==255.0){
                    sum+=1;
                }
            }
        }
        int i=1;
        if (sum>2000){
            imwrite(result_path+"/"+f_name+"/"+f_name+"-part-0.jpg",title);
            title_exist =true;
        }

//        imwrite(result_path+"\\1.jpg",title);

//        for (;i<bounds.size()-1;i++){
//            //System.out.println(bounds.get(i).size());
//            for (int j=0;j<bounds.get(i).size();j++){
//                int x = bounds.get(i).get(j)[0];
//                int y = bounds.get(i).get(j)[1];
//                int w = bounds.get(i).get(j)[2];
//                int h = bounds.get(i).get(j)[3];
//                //rectangle(image_x, new Point(x, y), new Point(x + w, y + h), new Scalar(255, 0, 0), 2);
//                //imwrite("D:\\IDEA\\Project\\table_split\\result\\1.jpg",image_x);
//                Rect rect = new Rect(x,y,w,h);
//                Mat ROI = new Mat(image_x,rect);
//                Mat ROI1 = ROI.clone();
//                Mat final_image = new Mat();
//                int top = (int) (0.3*ROI1.rows());
//                int bottom = (int) (0.3*ROI1.rows());
//                int left = (int) (0.3*ROI1.cols());
//                int right = (int) (0.3*ROI1.cols());
//                copyMakeBorder(ROI1, final_image, top, bottom, left, right, BORDER_CONSTANT,new Scalar(255));
//                if(count>=0&&count<10){
//                    imwrite(result_path+"/"+f_name+"/"+f_name+"-part-"+String.valueOf(count)+".jpg",final_image);
//                    count+=1;
//                }
//                else {
//                    imwrite(result_path+"/"+f_name+"/"+f_name+"-part-"+String.valueOf(count)+".jpg",final_image);
//                    count+=1;
//                }
//            }
//        }

        return title_exist;
    }

    public static List<SubImage> get_SubImage(Mat image_x,List<List<int[]>> bounds,String f_name) {
        List<SubImage> subImages = new ArrayList<>();
        for (int i=0;i<bounds.size();i++){
            //System.out.println(bounds.get(i).size());
            for (int j=0;j<bounds.get(i).size();j++){
                int x = bounds.get(i).get(j)[0];
                int y = bounds.get(i).get(j)[1];
                int w = bounds.get(i).get(j)[2];
                int h = bounds.get(i).get(j)[3];
                Rect rect = new Rect(x,y,w,h);
                Mat ROI = new Mat(image_x,rect);
                Mat ROI1 = ROI.clone();
                Mat final_image = new Mat();
                int top = (int) (0.3*ROI1.rows());
                int bottom = (int) (0.3*ROI1.rows());
                int left = (int) (0.3*ROI1.cols());
                int right = (int) (0.3*ROI1.cols());
                copyMakeBorder(ROI1, final_image, top, bottom, left, right, BORDER_CONSTANT,new Scalar(255));
                int[] position=new int[]{x,y,x+w,y+h};
                SubImage subImage = new SubImage(final_image,position);
                subImages.add(subImage);
            }
        }

        return subImages;
    }


    public static List<TableResult> tables(List<Mat> mats,List<String> fnames) throws Exception {

        String img_path = "/demo/images";
        String result_path = "/demo/split_result";
        File file = new File(img_path);
        File[] fs = file.listFiles();
        String dirname = null;
        String title_belong = null;
        ArrayList<String> fname = new ArrayList<String>();
        Map<String, List<int[]>> pairs_map = new HashMap<String, List<int[]>>();
        List<TableResult> tableResults = new ArrayList<>();

//        for(File f:fs) {//图片名字重新排序，防止在linux下乱序
//            String f_name = f.getName();
//            fname.add(f_name);
//        }
//        Collections.sort(fname);
        for(int i=0;i<mats.size();i++){
            //List<int[]> pairs = new ArrayList<>();
            String f_name = fnames.get(i);
//            if(fnames.size()!=0)
//            {
//                dirname = result_path + "/" + f_name;
//                File path = new File(dirname);
//                if ( !path.exists()){
//                    path.mkdir();
//                    System.out.println("创建文件夹路径为："+ path);
//                }
//            }
            System.loadLibrary("opencv_java430");
            //String image_path = img_path + "/" + f_name;
            Mat image = mats.get(i);

            if (image.empty()) {
                throw new Exception("image is empty");
            }

            Size size = image.size();
            double w = size.width;
            double h = size.height;
            int width = (int) w;
            int height = (int) h;

            Mat image_rotated = get_rotated_image(image,h,w);

            int x_scaled = 80;
            int y_scaled = 80;
            Mat image_contour = get_outline(image_rotated.clone(), x_scaled, y_scaled);

            List<int[]> boundings = get_boundings(image_contour);

            List<List<int[]>> sorted_bounds = new ArrayList<>();
            List<List<int[]>> bounds = new ArrayList<>();
            if (boundings.size() > 2) {
                sorted_bounds = get_resorted_bounds(boundings);
                bounds = add_head_tail_bound(sorted_bounds, width = width, height = height);
            }
            boolean title_exist = title_belong(image_rotated, bounds, dirname, f_name, result_path);//散件挂接（判断图片有没有表头）
            if(title_exist){
                title_belong = f_name;
                System.out.println(f_name+"  belong to  "+title_belong);
            }
            else{
                System.out.println(f_name+"  belong to  "+title_belong);
            }

            List<SubImage> subImages = get_SubImage(image_rotated,bounds,f_name);//返回切割完的小图以及坐标
            TableResult tableResult = new TableResult(f_name,subImages);
            tableResults.add(tableResult);

//            int count = 0;
//            int flag =1;
//            int key = 0;
//            int value = 0;
//            int k=1;
//            if(title_exist){
//                pairs.add(new int[]{0,-1});
//            }
//            for (; k < bounds.size()-1; k++) {       //默认不算第一张，因为第一张是表头
//                for (int j = 0; j < ((List) bounds.get(k)).size(); j++) {
//                    count++;
//                    int h_ = ((int[]) ((List) bounds.get(k)).get(j))[3];
//                    if (flag % 2 == 1) {
//                        if (j == ((List) bounds.get(k)).size() - 1 || h_ > ((int[]) ((List) bounds.get(k)).get(j + 1))[3]+5 ||  ((List) bounds.get(k)).size() == 1) {      //代表配对失败
//                            pairs.add(new int[]{count,-1});
//                            //System.out.println("1"+"   "+pairs.get(pairs.size()-1)[0]);
//                        } else {
//                            key = count;
//                            flag++;
//                            //System.out.println("2");
//                        }
//                    }
//                    else if (flag % 2 == 0) {
//                        value = count;
//                        pairs.add(new int[]{key,value});
//                        flag++;
//                        //System.out.println("3"+"   "+pairs.get(pairs.size()-1)[0]);
//                    }
//                }
//
//            }
//
//            pairs_map.put(f_name,pairs);
        }
        return tableResults;
    }
}
