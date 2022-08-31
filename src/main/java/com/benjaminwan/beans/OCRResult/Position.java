package com.benjaminwan.beans.OCRResult;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Position {
    Integer left;
    Integer top;
    Integer right;
    Integer bottom;
}
