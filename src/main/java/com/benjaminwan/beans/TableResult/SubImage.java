package com.benjaminwan.beans.TableResult;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencv.core.Mat;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubImage{
    Mat mat;
    int[] position;
}