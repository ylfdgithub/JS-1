package com.benjaminwan.utils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 试着写写链式调用的工具类
 */
public class ImageUtils {
    static {
        System.loadLibrary("opencv_java430");
    }
    private MultipartFile multipartFile;
    private BufferedImage image;
    private Mat mat;

    public BufferedImage getImage() {
        return image;

    }

    public Mat getMat() {
        return mat;
    }

    public MultipartFile getMultipartFile() {
        return multipartFile;
    }


    /**
     * static {@link MultipartFile} 转 {@link BufferedImage}
     */
    public static BufferedImage mFileToBImage(MultipartFile file) {
        BufferedImage srcImage = null;
        try {
            FileInputStream in = (FileInputStream) file.getInputStream();
            srcImage = javax.imageio.ImageIO.read(in);
        } catch (IOException e) {
            System.out.println("读取图片文件出错！" + e.getMessage());
        }
        return srcImage;
    }


    /**
     * static {@link BufferedImage} 转 {@link Mat}
     */
    public static Mat bImageToMat(BufferedImage image){
        Mat m = convertMat(image);
        return m;
    }

    /**
     *有参 {@link BufferedImage} 转 {@link Mat}
     */
    public ImageUtils bImageToMatLink(BufferedImage images){
        this.mat = convertMat(images);
        return this;
    }

    /**
     *无参 {@link BufferedImage} 转 {@link Mat}
     */
    public ImageUtils bImageToMatLink(){
        this.mat = convertMat(this.image);
        return this;
    }


    /**
     * 有参 {@link MultipartFile} 转 {@link BufferedImage}
     */
    public ImageUtils mFileToBImageLink(MultipartFile file){
        BufferedImage srcImage = null;
        try {
            FileInputStream in = (FileInputStream) file.getInputStream();
            srcImage = javax.imageio.ImageIO.read(in);
        } catch (IOException e) {
            System.out.println("读取图片文件出错！" + e.getMessage());
        }
        image=srcImage;
        return this;
    }

    /**
     * 无参 {@link MultipartFile} 转 {@link BufferedImage}
     */
    public ImageUtils mFileToBImageLink(){
        BufferedImage srcImage = null;
        try {
            FileInputStream in = (FileInputStream) this.multipartFile.getInputStream();
            srcImage = javax.imageio.ImageIO.read(in);
        } catch (IOException e) {
            System.out.println("读取图片文件出错！" + e.getMessage());
        }
        image=srcImage;
        return this;
    }

    /**
     * static Mat 转 BufferedImage
     */
    public static BufferedImage matToBImage(Mat mat) throws IOException {
        BufferedImage bi;//Image图片
        MatOfByte b = new MatOfByte();//保存的二进制数据
        Imgcodecs.imencode(".png", mat, b);//Mat转换成二进制数据。.png表示图片格式，格式不重要，基本不会对程序有任何影响。
        bi = ImageIO.read(new ByteArrayInputStream(b.toArray()));
        return bi;
    }
    /**
     * static BufferedImage 转 MultipartFile
     */


    /**
     * 有参 Mat 转 BufferedImage
     */
    public ImageUtils matToBImageLink(Mat mat) throws IOException {
        BufferedImage bi;//Image图片
        MatOfByte b = new MatOfByte();//保存的二进制数据
        Imgcodecs.imencode(".png", mat, b);//Mat转换成二进制数据。.png表示图片格式，格式不重要，基本不会对程序有任何影响。
        bi = ImageIO.read(new ByteArrayInputStream(b.toArray()));
        this.image = bi;
        return this;
    }

    /**
     * 无参 Mat 转 BufferedImage
     */
    public ImageUtils matToBImageLink() throws IOException{
        BufferedImage bi;//Image图片
        MatOfByte b = new MatOfByte();//保存的二进制数据
        Imgcodecs.imencode(".png", this.mat, b);//Mat转换成二进制数据。.png表示图片格式，格式不重要，基本不会对程序有任何影响。
        bi = ImageIO.read(new ByteArrayInputStream(b.toArray()));
        this.image = bi;
        return this;
    }


    /**
     * 有参 BufferedImage 转 MultipartFile
     */

    /**
     *无参 BufferedImage 转 MultipartFile
     */




    /**
     * bufferedImage convert mat
     * @param im
     * @return
     */
    public static Mat convertMat(BufferedImage im) {
        // Convert INT to BYTE
        im = toBufferedImageOfType(im, BufferedImage.TYPE_3BYTE_BGR);
        // Convert bufferedimage to byte array
        byte[] pixels = ((DataBufferByte) im.getRaster().getDataBuffer())
                .getData();
        // Create a Matrix the same size of image
        Mat image = new Mat(im.getHeight(), im.getWidth(), 16);
        // Fill Matrix with image values
        image.put(0, 0, pixels);
        return image;
    }

    /**
     *  8-bit RGBA convert 8-bit RGB
     * @param original
     * @param type
     * @return
     */
    private static BufferedImage toBufferedImageOfType(BufferedImage original, int type) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't convert if it already has correct type
        if (original.getType() == type) {
            return original;
        }
        // Create a buffered image
        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), type);
        // Draw the image onto the new buffer
        Graphics2D g = image.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.drawImage(original, 0, 0, null);
        } finally {
            g.dispose();
        }

        return image;
    }
}
