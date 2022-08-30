package com.benjaminwan.beans.OCRResult;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
class Page_info {
    Integer table_id;//0-1-2....
    String heads_info;//表头信息，可以选填
    Integer x_cell_size = 0;//0 不懂是什么
    Integer y_cell_size = 0;//0 不懂是什么
    List<Cell_info> cells_info;
}
