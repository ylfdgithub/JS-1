package com.benjaminwan.beans.TableResult;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableResult {
    String fileName;
    List<SubImage> subImages;
}




