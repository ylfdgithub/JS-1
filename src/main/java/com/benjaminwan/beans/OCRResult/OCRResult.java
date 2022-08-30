package com.benjaminwan.beans.OCRResult;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OCRResult {
    List<Page_info> tables_info;
}

