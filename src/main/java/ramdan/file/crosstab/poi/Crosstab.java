package ramdan.file.crosstab.poi;

import lombok.val;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class Crosstab implements Runnable{

    private Map<String,Integer> indexLookup= new HashMap<String,Integer>();
    protected String sheetName;
    protected int rownumber;
    protected Workbook workbook;
    protected Sheet sheet;
    protected Row row;

    protected Map<Integer,Double> totalRow = new HashMap<Integer, Double>();
    protected String lastRowKey = null;

    protected void checkSameRow(String rowKey){
        if(lastRowKey==null|| !lastRowKey.equals(rowKey)){
            short lastCell = row.getLastCellNum();
            for (int i = 0; i < lastCell; i++) {
                val cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null && cell.getCellTypeEnum()==CellType.NUMERIC) {
                    Double sum = totalRow.get(i);
                    if(sum==null){
                        sum = cell.getNumericCellValue();
                    }else {
                        sum = cell.getNumericCellValue()+sum;
                    }
                    totalRow.put(i, sum);
                }
            }
            createRowData();
            lastRowKey=rowKey;
        }
    }
    public void push(String rowKey,int collIndex, String value){
        if(rowKey==null) return;
        checkSameRow(rowKey);
        val coll = row.getCell(collIndex);
        coll.setCellValue(value);
    }
    public double getDouble(String rowKey,int collIndex){
        if(!rowKey.equals(lastRowKey)) return 0;
        val coll = row.getCell(collIndex);
        if(coll!=null){
            return coll.getNumericCellValue();
        }
        return 0;
    }
    public void push(String rowKey,int collIndex, double value){
        if(rowKey==null) return;
        checkSameRow(rowKey);
        val coll = row.getCell(collIndex);
        if(coll==null)return;
        coll.setCellValue(value);
        coll.setCellType(CellType.NUMERIC);

    }
    protected void ensureWorkbookReady() {
        if (this.workbook == null) {
            this.workbook = new XSSFWorkbook();
        }
    }
    protected void ensureSheetReady() {
        ensureWorkbookReady();
        if (this.sheet == null) {
            if (this.sheetName !=null ) {
                this.sheet = this.workbook.createSheet(this.sheetName);
            } else {
                this.sheet = this.workbook.createSheet();
            }
            this.rownumber = 0;
        }
    }
    public void prepareNewRow(){
        ensureSheetReady();
        row=sheet.createRow(rownumber);
        rownumber++;
    }
    public void createRowData(){
        prepareNewRow();
        row.createCell(0, CellType.STRING);
        totalRow.put(0,0.0);
        val size= indexLookup.size();
        for (int i = 1; i < size; i++) {
            val cell = row.createCell(i, CellType.NUMERIC);
            cell.setCellValue(0.0);
            totalRow.put(i,0.0);
        }
    }
    int contentStart;
    public void createHeader(){
        prepareNewRow();
        for (Map.Entry<String,Integer> entry : indexLookup.entrySet()){
            val cell = row.createCell(entry.getValue(), CellType.STRING);
            cell.setCellValue(entry.getKey());
        }
        contentStart = rownumber;
    }
    public void createHeader(int rownumber){
        this.rownumber = rownumber;
        createHeader();
    }
    public void addColName(String string){
        if(!indexLookup.containsKey(string)){
            indexLookup.put(string,indexLookup.size());
        }
    }
    public int getIndexColName(String col){
        val index = indexLookup.get(col);
        if(index!=null){
            return index;
        }
        return -1;
    }

    public void push(String rowKey,String colKey, String value){
        push(rowKey,getIndexColName(colKey),value);
    }
    public double getDouble(String rowKey,String colKey){
        return getDouble(rowKey,getIndexColName(colKey));
    }
    public void push(String rowKey,String colKey, double value){
        push(rowKey,getIndexColName(colKey),value);
    }
    public void total(String label,int startColl){
        val size= totalRow.size();
        prepareNewRow();
        row.createCell(0, CellType.STRING).setCellValue(label);
        for (int i = startColl; i < size; i++) {
            val cell = row.createCell(i, CellType.NUMERIC);
            cell.setCellValue(totalRow.get(i));
        }
    }
    public void testMemory(int rows){
        val collnamePefix ="COL-123456789012345678901234567890-";
        for (int i = 0; i < 8000; i++) {
            addColName(collnamePefix+i );
        }
        createHeader();
        long keyRow = 1;
        double value =1;
        while (keyRow<rows){
            for (int i = 0; i < 8000; i++) {
                value ++;
                System.out.printf("\r Rows %d Coll %d",keyRow,i);
                push("ROW"+keyRow,collnamePefix+i, value);
            }
            keyRow++;
        }
        total("total",1);
        try {
            workbook.write( new FileOutputStream("data.xls"));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
