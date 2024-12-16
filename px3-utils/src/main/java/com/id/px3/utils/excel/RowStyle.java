package com.id.px3.utils.excel;

import org.apache.poi.ss.usermodel.CellStyle;

public class RowStyle {
    private final short rowHeight;
    private final CellStyle[] cellStyles;

    public RowStyle(short rowHeight, CellStyle[] cellStyles) {
        this.rowHeight = rowHeight;
        this.cellStyles = cellStyles;
    }

    public short getRowHeight() {
        return rowHeight;
    }

    public CellStyle[] getCellStyles() {
        return cellStyles;
    }
}