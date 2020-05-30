# Sample java crostab poi Menghindari memory leak saat processing file.


Membuat crostab java dengan poi yang hemat memory kurang dari 64MB dengan jumlah kolom 8rb max heder 128 karakter.

Salah satu kunci proses file yang hemat memori adalah meminimalkan hold data di memori terlalu lama.
Dalam prosessing file selalu di usahakan untuk tidak melakukan loading seluruh data dari file ke memori.

Dalam project ini akan mendemonstrasikan 2 crostab yang satu boros memory dan yang satu lagi hemat memori.

## Cara kerja Prosess

Strukur data yang akan menjadi inputan untuk costab ini terdiri dari 3 colom yaitu

1. Key row
2. Key kolom
3. Value

contoh

Input
```text
SMART-POS-12,STRUK NO,SMART-POS-12
SMART-POS-12,BAYGON,1200
SMART-POS-13,STRUK NO,SMART-POS-13
SMART-POS-13,BAYGON,1300
SMART-POS-13,MARTABAK,1500
SMART-POS-13,KUKU BIMA,1400
```
Menjadi

| STRUK NO | BAYGON | MARTABAK  | KUKUBIMA |
| --------- | ------ | ------- | ------ |
| SMART-POS-12| 1200 | 0 | 0 |
| SMART-POS-13| 1300 | 1500 | 1400 |

 
Pada aplikasi ini terurut berdasarkan Key row data yang akan di proses harus terurut berdasarkan key row tersebut.


Untuk generate Crosstab bisa lihat pada class Crosstab

## Class Boros Memori
Class boros memori mempunyai karakteristik meyimpan data file dalam memori dalam waktu yang lama. Dengan cara ini memori akan cepat habis.

```java
public class Boros extends Crosstab {
    private File input;
    private File output;
    private List<String[]> data = new ArrayList<String[]>();
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
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
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
}

```

## Class HematDanSmart

Pembeda aplikasi hemat dan boros adalah bagaimana memperlakukan data. 
Untuk class Hemat berusah agar data tidak terlalu lama di simpan di memori.

```java
public class HematDanSmart extends Crosstab{

    private int rowAccessWindowSize = 10;

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
    public static void main(String ... arg){
        val boros= new HematDanSmart();
        if(arg.length==2) {
            boros.input = new File(arg[0]);
            boros.output = new File(arg[1]);
            boros.run();
        }else {
            boros.testMemory();
        }
    }
}    
```

Agar memori tidak terlalu banyak. Maka menggunakan class org.apache.poi.xssf.streaming.SXSSFWorkbook yang bisa meyimpan data disk. 
Untuk configurasi ini hanya 10 row saja yang di sipan di memori. Sehingga aplikasi bisa lebih hemat memori. Dan prosess GC lebih cepat

```java
      private int rowAccessWindowSize = 10; 
      public void ensureWorkbookReady() {
        if (this.workbook == null) {
            this.workbook = new SXSSFWorkbook(this.rowAccessWindowSize);
        }
    }
    
```
