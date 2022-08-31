package com.benjaminwan.beans.TableResult;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TableResult {
    String fileName;
    List<SubImage> subImages;
}




