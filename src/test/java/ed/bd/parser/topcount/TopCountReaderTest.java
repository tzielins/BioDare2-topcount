/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount;

import ed.bd.parser.topcount.err.FormatException;
import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.util.Pair;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author tzielins
 */
public class TopCountReaderTest {
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    double EPS = 1E-6;
    
    TopCountReader instance;
    
    public TopCountReaderTest() {
    }
    
    @Before
    public void setUp() {
        instance = new TopCountReader();
    }
    
    protected Path getTestPath(String fname) {
        try {
            return Paths.get(this.getClass().getResource(fname).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void isSuitableFormatWorks() throws IOException {

        String fname = "1517.002";
        Path file = getTestPath(fname);
        assertTrue(instance.isSuitableFormat(file));
        
        fname = "1609.484";
        file = getTestPath(fname);
        assertTrue(instance.isSuitableFormat(file));
        
        fname = "1517";
        file = getTestPath(fname);
        assertTrue(instance.isSuitableFormat(file));
        
        fname = "col1609";
        file = getTestPath(fname);
        assertTrue(instance.isSuitableFormat(file));
        
        fname = "mixed.zip";
        file = getTestPath(fname);
        assertTrue(instance.isSuitableFormat(file));        
        
        fname = "nozip.zip";
        file = getTestPath(fname);
        assertFalse(instance.isSuitableFormat(file));        
        
    }
    
    @Test
    public void readsPlateConcatenatedFile() throws Exception {
        
        String fname = "plate0626.txt";
        Path file = getTestPath(fname);
        
        Map<Pair<Integer, Integer>, TimeSeries> data = instance.read(file);
        
        assertEquals(96,data.size());
        TimeSeries series = data.get(new Pair<>(1,1));
        assertEquals(300,series.size());
    }
    
    @Test
    public void readsPlateFilesFromDir() throws Exception {
        
        String fname = "1517";
        Path file = getTestPath(fname);
        
        Map<Pair<Integer, Integer>, TimeSeries> data = instance.read(file);
        
        assertEquals(96,data.size());
        TimeSeries series = data.get(new Pair<>(1,1));
        assertEquals(7,series.size());
    }    
    
    @Test
    public void readsColumnsFilesFromDir() throws Exception {
        
        String fname = "col1609";
        Path file = getTestPath(fname);
        
        Map<Pair<Integer, Integer>, TimeSeries> data = instance.read(file);
        
        assertEquals(96,data.size());
        TimeSeries series = data.get(new Pair<>(1,1));
        assertEquals(10,series.size());
    }  
    
    @Test
    public void readsDesignatedPlateFromDir() throws Exception {
        
        String fname = "mixed";
        Path dir = getTestPath(fname);
        
        String plate = "1517";
        
        Map<Pair<Integer, Integer>, TimeSeries> data = instance.read(dir,plate);
        
        assertEquals(96,data.size());
        TimeSeries series = data.get(new Pair<>(1,1));
        assertEquals(3,series.size());
        
        plate = "1609";
        data = instance.read(dir,plate);
        
        assertEquals(96,data.size());
        series = data.get(new Pair<>(1,1));
        assertEquals(4,series.size());
        
    }     
    
    @Test
    public void readsDesignatedPlateFromZip() throws Exception {
        
        String fname = "mixed.zip";
        Path file = getTestPath(fname);
        
        String plate = "1517";
        
        Map<Pair<Integer, Integer>, TimeSeries> data = instance.read(file,plate);
        
        assertEquals(96,data.size());
        TimeSeries series = data.get(new Pair<>(1,1));
        assertEquals(3,series.size());
        
        plate = "1609";
        data = instance.read(file,plate);
        
        assertEquals(96,data.size());
        series = data.get(new Pair<>(1,1));
        assertEquals(4,series.size());
        
    }
    
    //form parsers tests
    @Test
    public void parseReadsColDataFromFilesSequence() throws Exception {
        
        Path dir = Paths.get(this.getClass().getResource("col1609").toURI());
        assertTrue(Files.isDirectory(dir));
        
        String plate = "1609";
        
        Map<Pair<Integer, Integer>, TimeSeries> series = instance.read(dir, plate);
        
        series.values().forEach( ts -> {
            assertEquals(10,ts.size());
        });
        
        TimeSeries a1 = series.get(new Pair<>(1,1));
        
        //09:08:40
        assertEquals(12.0+9+8/60.0+40.0/(60*60),a1.getLast().getTime(),EPS);
        assertEquals(2398,a1.getLast().getValue(),EPS);

        assertEquals(2067,a1.getFirst().getValue(),EPS);        
    }
    
    //from parser files
    @Test
    public void testFileParsingPlateParts() throws Exception {
        //System.out.println("test plates parts");
        
        Path dir = Paths.get(this.getClass().getResource("20150401a").toURI());
        assertTrue(Files.isDirectory(dir));
        
        String[] plates = {"0074","0114"};
        
        for (String plateNr : plates) {
            Path wholePlate = dir.resolve("plate"+plateNr);
            assertTrue(Files.isRegularFile(wholePlate));
        
        
            Map<Pair<Integer,Integer>,TimeSeries> exp = instance.read(wholePlate);
        
            Map<Pair<Integer,Integer>,TimeSeries> res = instance.read(dir, plateNr);
        
            assertEquals(exp,res);
        }
    }     
    
    //from parsers tests
    @Test
    public void testFileParsingPlatePartsWithBorkenFrames() throws Exception {
        //System.out.println("test plates parts");
        
        instance = new TopCountReader(false);
        
        Path dir = Paths.get(this.getClass().getResource("20150121c").toURI());
        assertTrue(Files.isDirectory(dir));
        
        String[] plates = {"1852","1855"};
        
        for (String plateNr : plates) {
            Map<Pair<Integer,Integer>,TimeSeries> res = instance.read(dir, plateNr);
        
            assertNotNull(res);
            assertFalse(res.values().iterator().next().isEmpty());
        }
    }   
    
    //from parser tests
    @Test
    public void testFileParsingFromSarah() throws Exception {
        //System.out.println("test file parsing2");
        
        Path file = Paths.get(this.getClass().getResource("1517.002").toURI());
        assertTrue(Files.isRegularFile(file));
        
        Map<Pair<Integer,Integer>,TimeSeries> res = instance.read(file);
        
        assertNotNull(res);
        TimeSeries ser = res.get(new Pair(1,1));
        assertNotNull(ser);
        
        assertEquals(17.0+13.0/60.0+58.0/3600,ser.getFirst().getTime(),EPS);
        assertEquals(94027,ser.getFirst().getValue(),EPS);
        
    }
    
    //from parsers tests
    @Test
    public void testFileParsingPlatePartsFromSarah() throws Exception {
        //System.out.println("test plates parts");
        
        Path dir = Paths.get(this.getClass().getResource("1517").toURI());
        assertTrue(Files.isDirectory(dir));
        
        String plate = "1517";
        
        Map<Pair<Integer,Integer>,TimeSeries> res = instance.read(dir, plate);

        assertNotNull(res);
        TimeSeries ser = res.get(new Pair(1,1));
        assertEquals(94027,ser.getFirst().getValue(),EPS);
    }   
    
    //from parsers tests
    @Test
    public void testFileParsing() throws Exception {
        
        String fName = "090211plate1236.txt";
        Path file = Paths.get(this.getClass().getResource(fName).toURI());
        assertTrue(Files.exists(file));
        
        int elements = 6*8;
        double firstTop = 2687;
        double firstBottom = 767;
        double topTime = 11+50.0/60;
        double bottomTime = 11+59.0/60;
        double lastTop = 873;
        double lastBottom = 468;
        int lastRow = 8;
        int lastCol = 11;
        
        Map<Pair<Integer,Integer>,TimeSeries> data = instance.read(file);
        assertNotNull(data);
        assertFalse(data.isEmpty());
        
        List<String> labels = new ArrayList<>();
        List<TimeSeries> series = new ArrayList<>();
        for (Map.Entry<Pair<Integer,Integer>,TimeSeries> ent : data.entrySet()) {
            labels.add(ent.getKey().toString());
            series.add(ent.getValue());
        }
        
        List<List<String>> headers = new ArrayList<>();
        labels.add(0,"id");
        headers.add(labels);
        //TimeSeriesFileHandler.saveToText(series, headers,new File("D:/Temp/topcount."+fName+".csv"), ",", ROUNDING_TYPE.DECY);
        
        assertEquals(elements,data.size());
        TimeSeries top = data.get(new Pair<>(1,1));
        TimeSeries bottom = data.get(new Pair<>(lastRow,lastCol));
        
        assertNotNull(top);
        assertNotNull(bottom);
        
        assertEquals(firstTop,top.getFirst().getValue(),EPS);
        assertEquals(topTime,top.getFirst().getTime(),EPS);
        assertEquals(firstBottom,bottom.getFirst().getValue(),EPS);
        assertEquals(bottomTime,bottom.getFirst().getTime(),EPS);
        assertEquals(lastTop,top.getLast().getValue(),EPS);
        assertEquals(lastBottom, bottom.getLast().getValue(),EPS);
        
        fName = "plate0004.txt";
        file = Paths.get(this.getClass().getResource(fName).toURI());
        assertTrue(Files.exists(file));
        
        elements = 12*8;
        firstTop = 17275;
        firstBottom = 16941;
        topTime = 12+48.0/60.0;
        bottomTime = 12+52.0/60.0;
        lastTop = 7464;
        lastBottom = 293;
        lastRow = 8;
        lastCol = 12;
        
        data = instance.read(file);
        assertNotNull(data);
        assertFalse(data.isEmpty());
        
        labels = new ArrayList<>();
        series = new ArrayList<>();
        for (Map.Entry<Pair<Integer,Integer>,TimeSeries> ent : data.entrySet()) {
            labels.add(ent.getKey().toString());
            series.add(ent.getValue());
        }
        
        headers = new ArrayList<>();
        labels.add(0,"id");
        headers.add(labels);
        //TimeSeriesFileHandler.saveToText(series, headers,new File("D:/Temp/topcount."+fName+".csv"), ",", ROUNDING_TYPE.DECY);
        
        assertEquals(elements,data.size());
        top = data.get(new Pair<>(1,1));
        bottom = data.get(new Pair<>(lastRow,lastCol));
        
        assertNotNull(top);
        assertNotNull(bottom);
        
        assertEquals(firstTop,top.getFirst().getValue(),EPS);
        assertEquals(topTime,top.getFirst().getTime(),EPS);
        assertEquals(firstBottom,bottom.getFirst().getValue(),EPS);
        assertEquals(bottomTime,bottom.getFirst().getTime(),EPS);
        assertEquals(lastTop,top.getLast().getValue(),EPS);
        assertEquals(lastBottom, bottom.getLast().getValue(),EPS);
        
        fName = "plate0626.txt";
        file = Paths.get(this.getClass().getResource(fName).toURI());
        assertTrue(Files.exists(file));
        
        elements = 12*8;
        firstTop = 8750;
        firstBottom = 14594;
        topTime = 19+45.0/60.0;
        bottomTime = 19+46.0/60.0;
        lastTop = 7274;
        lastBottom = 4310;
        lastRow = 8;
        lastCol = 12;
        
        data = instance.read(file);
        assertNotNull(data);
        assertFalse(data.isEmpty());
        
        labels = new ArrayList<>();
        series = new ArrayList<>();
        for (Map.Entry<Pair<Integer,Integer>,TimeSeries> ent : data.entrySet()) {
            labels.add(ent.getKey().toString());
            series.add(ent.getValue());
        }
        
        headers = new ArrayList<>();
        labels.add(0,"id");
        headers.add(labels);
        //TimeSeriesFileHandler.saveToText(series, headers,new File("D:/Temp/topcount."+fName+".csv"), ",", ROUNDING_TYPE.DECY);
        
        assertEquals(elements,data.size());
        top = data.get(new Pair<>(1,1));
        bottom = data.get(new Pair<>(lastRow,lastCol));
        
        assertNotNull(top);
        assertNotNull(bottom);
        
        assertEquals(firstTop,top.getFirst().getValue(),EPS);
        assertEquals(topTime,top.getFirst().getTime(),EPS);
        assertEquals(firstBottom,bottom.getFirst().getValue(),EPS);
        assertEquals(bottomTime,bottom.getFirst().getTime(),EPS);
        assertEquals(lastTop,top.getLast().getValue(),EPS);
        assertEquals(lastBottom, bottom.getLast().getValue(),EPS);
        
        
    }
    
    //from parsers tests
    @Test
    public void testFileParsing2() throws Exception {
        //System.out.println("test file parsing2");
        
        Path file = Paths.get(this.getClass().getResource("plate0075.txt").toURI());
        assertTrue(Files.isRegularFile(file));
        
        Map<Pair<Integer,Integer>,TimeSeries> res = instance.read(file);
        
        assertNotNull(res);
        TimeSeries ser = res.get(new Pair(1,1));
        assertNotNull(ser);
        assertEquals(13.0+3.0/60.0+7.0/3600,ser.getFirst().getTime(),EPS);
        assertEquals(109443,ser.getFirst().getValue(),EPS);
        assertEquals(13.0+51.0/60.0+37.0/3600,ser.getTimepoints().get(1).getTime(),EPS);
        assertEquals(92547,ser.getTimepoints().get(1).getValue(),EPS);
        assertEquals(7240,ser.getLast().getValue(),EPS);
        
        ser = res.get(new Pair(7,12));
        assertNotNull(ser);
        assertEquals(148075,ser.getFirst().getValue(),EPS);
        assertEquals(9648,ser.getLast().getValue(),EPS);
        
    }
    
    
    @Test
    public void getFirstPlateNameFindsAPlate() throws Exception {
        
        List<String> names = Arrays.asList(
                "ala",
                "cos.xml",
                "plate065.almost.01",
                "plate3.0486",
                "0654.12"
        );
        
        String res = instance.getFirstPlateName(names.stream());
        assertEquals("plate3",res);
    }
    
    @Test
    public void getFirstPlateNameThrowsFormatExceptionIfNotFound()  {
        
        List<String> names = Arrays.asList(
                "ala",
                "cos.xml",
                "plate065.almost.01"
        );
        
        try {
            instance.getFirstPlateName(names.stream());
            fail("Exception expected");
        } catch (FormatException e){}
    }   
    
    @Test
    public void getFirstPlateFromDirWorks() throws Exception {

        String fname = "1517";
        Path file = getTestPath(fname);

        String res = instance.getFirstPlate(file);
        assertEquals("1517",res);
    }
    
    @Test
    public void findPlateFilesInDir() throws Exception {

        String dirName = "mixed";
        String plate = "1517";
        Path dir = getTestPath(dirName);
        
        List<Path> files = instance.findPlateFiles(dir, plate);
        assertEquals(3,files.size());
        
        assertEquals(Arrays.asList("1517.0000","1517.0001","1517.0002")
                ,files.stream().map(p->p.getFileName().toString()).collect(Collectors.toList()));

    }
    
    @Test
    public void findPlateFilesInPaths() throws Exception {

        
        List<Path> paths = Arrays.asList(
                testFolder.newFolder("2001.01").toPath(),
                testFolder.newFile("2001.01.03").toPath(),
                testFolder.newFile("20.2001.04").toPath(),
                testFolder.newFile("2001.11").toPath(),
                testFolder.newFile("2001.110").toPath(),
                testFolder.newFile("2001.02").toPath(),
                testFolder.newFile("2002.05").toPath()                
        );
        
        String plate = "2001";
        
        List<Path> files = instance.findPlateFiles(paths.stream(), plate);
        
        assertEquals(Arrays.asList("2001.02","2001.11","2001.110")
                ,files.stream().map(p->p.getFileName().toString()).collect(Collectors.toList()));

    }
    
    
    @Test
    public void isZipFileRecognizesZips() throws Exception {
        
        String fname = "mixed.zip";
        Path file = getTestPath(fname);

        assertTrue(instance.isZipFile(file));
        
        fname = "plate0626.txt";
        file = getTestPath(fname);
        assertFalse(instance.isZipFile(file));
        
        fname = "nozip.zip";
        file = getTestPath(fname);
        assertFalse(instance.isZipFile(file));
        
        
    }
    
    @Test
    public void getFirstPlateFindsNamesInZipFile() throws Exception {
        
        String fname = "mixed.zip";
        Path file = getTestPath(fname);     
        
        //subfolders
        ZipFile zip = new ZipFile(file.toFile());
                
        String res = instance.getFirstPlate(zip);
        assertEquals("1517",res);
        
        //simple
        fname = "1517.zip";
        file = getTestPath(fname);     
        zip = new ZipFile(file.toFile());
        
        res = instance.getFirstPlate(zip);
        assertEquals("1517",res);
        
    }
    
    @Test
    public void streamEntriesForPlateGivesSortedPlateEntries() throws Exception {
        
        List<ZipEntry> entries = new ArrayList<>();
        
        ZipEntry e = new ZipEntry("p1.003");
        entries.add(e);
         
        e = new ZipEntry("p1.001/");
        entries.add(e);
        
        e = new ZipEntry("p1.001/p1.011");
        entries.add(e);
        
        e = new ZipEntry("p1.001/p1.2");
        entries.add(e);
        
        e = new ZipEntry("p1.001/001.2");
        entries.add(e);
        
        List<ZipEntry> res = instance.streamEntriesForPlate(entries.stream(),"p1")
                .collect(Collectors.toList());
        
        assertEquals(3,res.size());
        
        List<String> names = res.stream().map(z -> z.getName()).collect(Collectors.toList());
        
        assertEquals(Arrays.asList("p1.001/p1.2","p1.003","p1.001/p1.011"),names);
        
    }
    
}
