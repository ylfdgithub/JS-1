package com.benjaminwan.beans.HHResult;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.opencv.core.Mat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Page {
    Integer pageID;
    Mat image;
}
