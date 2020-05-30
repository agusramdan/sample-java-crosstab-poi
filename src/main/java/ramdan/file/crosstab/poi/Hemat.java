package ramdan.file.crosstab.poi;

import lombok.val;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Hemat extends Crosstab{

    private int rowAccessWindowSize = 10;

    public void ensureWorkbookReady() {
        if (this.workbook == null) {
            this.workbook = new SXSSFWorkbook(this.rowAccessWindowSize);
        }
    }
    private File input;
    private File output;
    public static void main(String ... arg){
        val boros= new Hemat();
        if(arg.length==2) {
            boros.input = new File(arg[0]);
            boros.output = new File(arg[1]);
            boros.run();
        }else {
            boros.testMemory();
        }
    }

    private void loadHeader() throws IOException {
        val lineReader = new BufferedReader(new FileReader(input));
        String line;
        while ((line =lineReader.readLine())!=null){
            val rcv = line.split(",");
            addColName(rcv[1]);
        }
        super.createHeader();
    }

    private void loadData() throws IOException {
        val lineReader = new BufferedReader(new FileReader(input));
        String line;
        while ((line =lineReader.readLine())!=null){
            val rcv = line.split(",");
            addColName(rcv[1]);
        }
        super.createHeader();
    }
    @Override
    public void run() {
        try {
            loadHeader();
            loadData();
            workbook.write( new FileOutputStream(output));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}