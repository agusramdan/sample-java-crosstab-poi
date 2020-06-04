package ramdan.file.crosstab.poi;

import lombok.Setter;
import lombok.val;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.*;

public class HematDanSmart extends Crosstab{

    public HematDanSmart(String... colls) {
        super(colls);
    }

    @Setter
    private int rowAccessWindowSize = 10;

    public void ensureWorkbookReady() {
        if (this.workbook == null) {
            this.workbook = new SXSSFWorkbook(this.rowAccessWindowSize);
        }
    }

    private File input;
    private File output;

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
        val fr= new FileReader(input);
        try {
            val lineReader = new BufferedReader(fr);
            String line;
            while ((line = lineReader.readLine()) != null) {
                val rcv = line.split(",");
                push(rcv[0],rcv[1],rcv[2]);
            }
        }finally {
            if(fr!=null){
                fr.close();
            }
        }
    }
    @Override
    public void run() {
        OutputStream os = null;
        try {
            loadHeader();
            loadData();
            os= new FileOutputStream(output);
            workbook.write(os);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(os!=null){
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String ... arg){

        val smart= new HematDanSmart();
        if(arg.length==2) {
            if("test".equals(arg[0])){
                smart.output = new File("test.xls");
                smart.testMemory(Integer.parseInt(arg[1]));
            }else{
                smart.input = new File(arg[0]);
                smart.output = new File(arg[1]);
                smart.run();
            }
        }else {
            System.out.printf("No parameter");
        }
    }
}
