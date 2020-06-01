package ramdan.file.crosstab.poi;

import lombok.val;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Boros extends Crosstab {
    private File input;
    private File output;
    private List<String[]> data = new ArrayList<String[]>();
    protected void ensureWorkbookReady() {
        if (this.workbook == null) {
            this.workbook = new XSSFWorkbook();
        }
    }
    @Override
    public void run() {
        FileReader fr=null;
        OutputStream fos = null;
        try {
            fr = new FileReader(input);
            fos =new FileOutputStream(output);
            val lineReader = new BufferedReader(fr);
            String line;
            while ((line =lineReader.readLine())!=null){
                val rcv = line.split(",");
                data.add(rcv);
                addColName(rcv[1]);
            }
            super.createHeader();
            for (String[] rcv: data) {
                push(rcv[0],rcv[1],rcv[2]);
            }
            workbook.write( fos);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fr!=null){
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fos!=null){
                try {
                    fos.flush();
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String ... arg){
        val boros= new Boros();
        if(arg.length==2) {
            if("test".equals(arg[0])){
                boros.output = new File("test.xls");
                boros.testMemory(Integer.parseInt(arg[1]));
            }else{
                boros.input = new File(arg[0]);
                boros.output = new File(arg[1]);
                boros.run();
            }

        }else {
            System.out.printf("No parameter");
        }
    }
}
