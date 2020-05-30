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

    protected String lastRowKey = null;
    public void push(String rowKey,int collIndex, String value){
        if(rowKey==null) return;
        if(lastRowKey==null|| !lastRowKey.equals(rowKey)){
            createRowData();
            lastRowKey=rowKey;
        }
        val coll = row.getCell(collIndex);
        try{
            coll.setCellValue(Double.parseDouble(value));
        }catch (java.lang.OutOfMemoryError e){
            throw e;
        }
        catch (Exception e){
            coll.setCellValue(value);
        }
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
        val size= indexLookup.size();
        for (int i = 1; i < size; i++) {
            val cell = row.createCell(i, CellType.NUMERIC);
            cell.setCellValue(0.0);
        }
    }
    public void createHeader(){
        prepareNewRow();
        for (Map.Entry<String,Integer> entry : indexLookup.entrySet()){
            val cell = row.createCell(entry.getValue(), CellType.STRING);
            cell.setCellValue(entry.getKey());
        }
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

    public void testMemory(){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    workbook.write( new FileOutputStream("data.xls"));
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        val collnamePefix ="COL-123456789012345678901234567890-";
        for (int i = 0; i < 8000; i++) {
            addColName(collnamePefix+i );
        }
        createHeader();
        long keyRow = 1;
        double value =1;
        while (true){
            for (int i = 0; i < 8000; i++) {
                value ++;
                System.out.printf("\r Rows %d Coll %d",keyRow,i);
                push("ROW"+keyRow,collnamePefix+i, Double.toString(value));
            }
            keyRow++;
        }
    }
}
