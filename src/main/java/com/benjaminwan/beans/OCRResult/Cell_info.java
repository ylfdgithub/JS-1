package com.benjaminwan.beans.OCRResult;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cell_info {
    Integer table_cell_id;//0-1-2....
    Integer xsc = 0;
    Integer xec = 0;
    Integer ysc = 0;
    Integer yec = 0;
    Integer yid = 0;
    Integer xid = 0;
    Position position;
    String words;
}
