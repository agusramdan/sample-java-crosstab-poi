package ramdan.file.crosstab.poi;

import lombok.val;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Boros extends Crosstab {
    private File input;
    private File output;
    private List<String[]> data = new ArrayList<String[]>();
    public static void main(String ... arg){
        val boros= new Boros();
        if(arg.length==2) {
            boros.input = new File(arg[0]);
            boros.output = new File(arg[1]);
            boros.run();
        }else {
            boros.testMemory();
        }
    }


    @Override
    public void run() {
        try {
            val lineReader = new BufferedReader(new FileReader(input));
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
            workbook.write( new FileOutputStream(output));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
