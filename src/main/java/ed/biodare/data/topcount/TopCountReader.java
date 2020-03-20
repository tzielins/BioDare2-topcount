/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biodare.data.topcount;

import ed.biodare.data.topcount.dom.DataBlock;
import ed.biodare.data.topcount.err.FileException;
import ed.biodare.data.topcount.err.FormatException;
import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.util.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author tzielins
 */
public class TopCountReader {
    
    final TopCountPlateParser plateParser;
    final TopCountColumnParser columnParser;
    final TopCountUtil util;
    final boolean CHECK_FRAMES_COMPLETENESS;
    
    public TopCountReader() {
        this(true);
    }
    
    public TopCountReader(boolean checkFramesCompletness) {
    
        this.CHECK_FRAMES_COMPLETENESS = checkFramesCompletness;
        
        plateParser = new TopCountPlateParser();
        
        columnParser = new TopCountColumnParser();
        
        util = new TopCountUtil();
    }    
    
    /**
     * Parses the topcount resuls in the file. 
     * It can be individual file or a collection of files in a dir or zip file.
     * The collection of results
     * if formed as series of files PLATE_NR.001, PLATE_NR.002. They are content is
     * joined following the suffix order: 001, 002.
     * Parsing deals with missing files, so the gaps in numerations are allowed.
     * The first PLATE will be selected for which, files matches the signature PLATE.NUMBERS;
     * The time format is either AM/PM or 24h one
     * @param file Individual topcount file, dir with files collection of zip file with files collections
     * @return timeseries indexed by their (ROW,COLUMN) coordinates, rows and columns are 1 based. A1 is (1,1), 
     * H10 is (8,10)
     * @throws IOException
     * @throws FormatException 
     */
    public Map<Pair<Integer,Integer>,TimeSeries> read(Path file) throws IOException, FormatException {
        
        try {
            if (Files.isRegularFile(file))
                return readFromRegularFile(file);

            if (Files.isDirectory(file))
                return readFromDirectory(file);

            throw new IllegalArgumentException("Unsupported file, "+file);
        } catch (FileException e) {
            throw new IOException(e);
        }
        
    }
    
    /**
     * Parses the topcount resuls in the file. 
     * In case of single topcount file the plate name is ignored
     * The collection of results
     * if formed as series of files PLATE_NR.001, PLATE_NR.002. They are content is
     * joined following the suffix order: 001, 002.
     * Parsing deals with missing files, so the gaps in numerations are allowed.
     * The time format is either AM/PM or 24h one
     * @param file Parent directory containing the collection of files or zip file with the files
     * @param plate the results files prefix (expected files are plate.001, plate.002)
     * @return timeseries indexed by their (ROW,COLUMN) coordinates, rows and columns are 1 based. A1 is (1,1), 
     * H10 is (8,10)
     * @throws IOException
     * @throws FormatException 
     */    
    public Map<Pair<Integer,Integer>,TimeSeries> read(Path file, String plate) throws IOException, FormatException {
        
        try {
            if (Files.isRegularFile(file))
                return readFromRegularFile(file, plate);

            if (Files.isDirectory(file))
                return readFromDirectory(file, plate);
            
            throw new IllegalArgumentException("Unsupported file, "+file);
        } catch (FileException e) {
            throw new IOException(e);
        }
    }    
    
    public boolean isSuitableFormat(Path file) throws IOException {
        
        if (Files.isRegularFile(file)) {
            if (!isZipFile(file)) {
                try {
                    return getParser(file) != null;
                } catch (FormatException e) {
                    return false;
                }
            }
        }
        
        try {
            return !read(file).isEmpty();            
        } catch (FormatException e) {
            return false;
        }
    }

    protected Map<Pair<Integer, Integer>, TimeSeries> readFromRegularFile(Path file) throws IOException, FormatException {
        
        if (isZipFile(file))
            return readFromZipFile(file);
        
        
        TopCountParser parser = getParser(file);
        
        return util.blocksToPlateMap(parser.readDataBlocks(file), CHECK_FRAMES_COMPLETENESS);        
    }    
    
    protected Map<Pair<Integer, Integer>, TimeSeries> readFromRegularFile(Path file, String plate) throws IOException, FormatException {
        
        if (isZipFile(file))
            return readFromZipFile(file, plate);

        TopCountParser parser = getParser(file);
        
        return util.blocksToPlateMap(parser.readDataBlocks(file), CHECK_FRAMES_COMPLETENESS);        
    }    
    
    protected Map<Pair<Integer, Integer>, TimeSeries> readFromDirectory(Path dir) throws IOException, FormatException {
        
        String plate = getFirstPlate(dir);
        return readFromDirectory(dir,plate);
    }


    protected Map<Pair<Integer, Integer>, TimeSeries> readFromDirectory(Path dir, String plate) throws FormatException, IOException {
        
        List<Path> files = findPlateFiles(dir,plate);
        
        if (files.isEmpty())
            throw new FormatException("No plate "+plate+" could be found in files: "+dir);
        
        
        TopCountParser parser = getParser(files.get(0));
        
        List<DataBlock> blocks = files.stream()
                .flatMap( path -> parser.readDataBlocks(path).stream())
                .collect(Collectors.toList());
                
        return util.blocksToPlateMap(blocks, CHECK_FRAMES_COMPLETENESS);    
    }

    protected String getFirstPlate(Path dir) throws IOException, FormatException {
        
        try (Stream<Path> paths = Files.list(dir)) {

            return getFirstPlateName(
                    paths.filter( Files::isRegularFile)
                        .map( p -> p.getFileName().toString())
            );
        
        }        
    }
    
    protected String getFirstPlateName(Stream<String> fName) throws FormatException {
        
        
        Pattern pattern = Pattern.compile("\\w+\\.\\d+");
        

        return fName
                    .filter( n -> pattern.matcher(n).matches())
                    .findFirst()
                    .map( n -> n.substring(0, n.indexOf(".")))
                    .orElseThrow( ()-> new FormatException("No plate name pattern could be found in files"));
        
    }    

    protected List<Path> findPlateFiles(Path dir, String plate) throws IOException {
                
        try (Stream<Path> paths = Files.list(dir)) {
            return findPlateFiles(paths,plate);
        }
    }
    
    protected List<Path> findPlateFiles(Stream<Path> paths, String plate)  {
        Pattern pattern = util.platePattern(plate);
        
        Comparator<Path> byNr = Comparator.comparing((path) -> {
            final String fName = path.getFileName().toString();
            final String nrPart = fName.substring(fName.lastIndexOf(".")+1);
            return Integer.parseInt(nrPart);
        });
        
        return paths.filter(Files::isRegularFile)
                    .filter( p ->  pattern.matcher(p.getFileName().toString()).matches())
                    .sorted(byNr)
                    .collect(Collectors.toList());        
    }

    protected boolean isZipFile(Path file) {
        
        //String fName = file.getFileName().toString();
        //if (!fName.endsWith(".zip") && !fName.endsWith(".gz"))
        //    return false;
        
        try(ZipFile zip = new ZipFile(file.toFile())) {
            return zip.size() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    protected Map<Pair<Integer, Integer>, TimeSeries> readFromZipFile(Path file) throws IOException, FileException, FormatException {
        
        try (ZipFile zip = new ZipFile(file.toFile())) {
            String plate = getFirstPlate(zip);
            return readFromZipFile(zip, plate);
        }
    }
    
    protected Map<Pair<Integer, Integer>, TimeSeries> readFromZipFile(Path file, String plate) throws IOException, FileException, FormatException {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            return readFromZipFile(zip, plate);
        }
    }
    

    protected Map<Pair<Integer, Integer>, TimeSeries> readFromZipFile(ZipFile zip, String plate) throws FileException, FormatException, IOException {
        
        List<? extends ZipEntry> entries = zipEntriesForPlate(zip,plate);

        TopCountParser parser = getParser(entries.get(0),zip);
        
        List<DataBlock> dataBlocks = entries.stream()
                .map( z -> zipToReader(z,zip))
                .flatMap( reader -> parser.readDataBlocks(reader).stream())
                .collect(Collectors.toList());
                
        return util.blocksToPlateMap(dataBlocks,CHECK_FRAMES_COMPLETENESS);
        
    }
    
    protected BufferedReader zipToReader(ZipEntry entry, ZipFile zip) {
        try {
            return new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));
        } catch (IOException ex) {
            throw new FileException("Cannot read zip content: "+ex.getMessage());
        }                
    }
    
    protected List<? extends ZipEntry> zipEntriesForPlate(ZipFile zip, String plate) {
        
        try (Stream<? extends ZipEntry> entries = zip.stream()) {
        
            return streamEntriesForPlate(entries,plate).collect(Collectors.toList());
        }
    }   
    
    protected Stream<? extends ZipEntry> streamEntriesForPlate(Stream<? extends ZipEntry> entries, String plate) {
        
        Pattern pattern = util.platePattern(plate);
        
        Comparator<Pair<String,ZipEntry>> byNr = Comparator.comparing((Pair<String,ZipEntry> pair) -> {
            final String nrPart = pair.getLeft().substring(pair.getLeft().lastIndexOf(".")+1);
            return Integer.parseInt(nrPart);
        });
        
        return entries
                    .filter( e -> !e.isDirectory())
                    .map ( e -> new Pair<>(Paths.get(e.getName()).getFileName().toString(),e))
                    .filter( e -> pattern.matcher(e.getLeft()).matches()) 
                    .sorted(byNr)
                    .map(Pair::getRight)
                    ;
    }     

    protected String getFirstPlate(ZipFile zip) throws FormatException {
        
        try (Stream<? extends ZipEntry> entries = zip.stream()) {
            
            return getFirstPlateName(
                entries.filter(e -> !e.isDirectory())
                   .map( e -> Paths.get(e.getName()))
                   .map( p -> p.getFileName().toString())
            );
        }
    }

    protected TopCountParser getParser(ZipEntry entry, ZipFile zip) throws IOException {
        
        if (plateParser.isSuitableFormat(zipToReader(entry, zip)))
            return plateParser;
        
        if (columnParser.isSuitableFormat(zipToReader(entry, zip)))
            return columnParser;
            
        throw new FormatException("Unrecognized file format for Topcount data: "+entry.getName());    
    }

    protected TopCountParser getParser(Path file) throws IOException {
        
        if (plateParser.isSuitableFormat(file))
            return plateParser;
        
        if (columnParser.isSuitableFormat(file))
            return columnParser;
            
        throw new FormatException("Unrecognized file format for Topcount data: "+file);    
    }
}
