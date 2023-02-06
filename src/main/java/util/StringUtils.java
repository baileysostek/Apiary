package util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.lwjgl.BufferUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class StringUtils {

//    private static HashMap<String, String> fileCache = new HashMap<>();

    private static String RESOURCES_DIRECTORY = "/res/";
    private static String NATIVES_DIRECTORY   = "/natives/";

    private static String PATH = new File("").getAbsolutePath().replaceAll("\\\\", "/");

    public static byte[] loadByteArray(String filePath){
        try{
            String path = PATH + RESOURCES_DIRECTORY + filePath;
            System.out.println("Looking for file on disk at: "+path);
            File file = new File(path);
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            byte[] buf = new byte[(int) file.length()];
            // Read in our file to our buffer
            in.read(buf);
            return buf;
        } catch (FileNotFoundException e) {
            System.out.println("Error: could not find file.");
        } catch (IOException e) {
            System.out.println("Error: IOException.");
        }

        return null;
    }

    public static String loadNoCache(String filePath){
        StringBuilder fileData = new StringBuilder();
        try{
            String path = PATH + RESOURCES_DIRECTORY  + filePath;
            System.out.println("Looking for file on disk at: "+path);
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while((line = reader.readLine()) != null){
                fileData.append(line).append("\n");
            }
            reader.close();
            return fileData.toString();
        } catch (FileNotFoundException e) {
            System.out.println("Error: could not find file.");
        } catch (IOException e) {
            System.out.println("Error: IOException.");
        }

        return null;
    }

    public static String load(String filePath){
        return loadNoCache(filePath);
    }

    public static boolean exists(String filePath){
        String path = PATH + RESOURCES_DIRECTORY  + filePath;
        return new File(path).exists();
    }

    /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * @param resource   the resource to read
     * @param bufferSize the initial buffer size
     *
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     */
    public static ByteBuffer loadRaw(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

//        String stringPath = resource.replaceFirst("/", "");
        while(resource.contains("//")){
            resource = resource.replace("//", "/");
        }

        String stringPath = (resource).replaceAll("/", "\\\\");

        System.out.println("Try load:" + stringPath);

        final Path path = Paths.get(stringPath);

        if ( Files.isReadable(path) ) {
            System.out.println("File is readable:");
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int)fc.size() + 1);
                while ( fc.read(buffer) != -1 ) ;
            }
        } else {
            System.out.println("File is NOT readable:");
            try (
                    InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
                    ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = BufferUtils.createByteBuffer(bufferSize);

                while ( true ) {
                    int bytes = rbc.read(buffer);
                    if ( bytes == -1 )
                        break;
                    if ( buffer.remaining() == 0 )
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                }
            }
        }

        buffer.flip();
        return buffer;
    }

    public static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    public static String getPathToResources(){
        return PATH + RESOURCES_DIRECTORY ;
    }

    public static String getPathToNatives(){
        return PATH + NATIVES_DIRECTORY;
    }

    public static String getRelativePath(){
        return PATH;
    }

    public static void write(String data, String filePath){
        try {
            String path = new File("").getAbsolutePath() + RESOURCES_DIRECTORY + filePath;
            FileWriter myWriter = new FileWriter(path);
            myWriter.write(data);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(String[] data, String filePath){
        write(unify(data), filePath);
    }

    public static void write(byte[] rawData, String filePath){
        try {
            File outputFile =  new File(PATH + filePath);
            System.out.println("Writing File:" + outputFile.getAbsolutePath());
            outputFile.createNewFile();
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(rawData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean makeDirectory(String dirPath){
        String path = new File("").getAbsolutePath() + RESOURCES_DIRECTORY + dirPath;
        File file = new File(path);
        return file.mkdirs();
    }

    public static boolean directoryExists(String dirPath){
        String path = new File("").getAbsolutePath() + RESOURCES_DIRECTORY + dirPath;
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    //Take a String array and smoosh it into a single string.
    public static String unify(String[] data){
        String out = "";
        for(String s : data){
            out = out + s;
        }
        return out;
    }

    public static String padStart(String message, int length, String padding){
        return pad(message, length, padding, true);
    }

    public static String padEnd(String message, int length, String padding){
        return pad(message, length, padding, false);
    }

    private static String pad(String message, int length, String padding, boolean isPadStart){
        String out = message;

        if(padding.isEmpty()){
            padding = " ";
        }

        char letter;
        for(int i = 0; i < Math.max(0, length - message.length()); i++){
            letter = padding.charAt(i % padding.length());
            out = isPadStart ? (letter + out) : (out + letter);
        }
        return out;
    }

    private static final String TEMPLATE_START_STRING = "{{";
    private static final String TEMPLATE_END_STRING   = "}}";
    public static String format(String template, Map<String, Object> substitutions){
        int start = 0;

        while(template.contains(TEMPLATE_START_STRING)&&template.contains("}}")){
            start = template.indexOf(TEMPLATE_START_STRING);
            int end = template.indexOf("}}");
            String to_substitute = template.substring(start + TEMPLATE_START_STRING.length(), end);
//            System.out.println(to_substitute);

            String substitution = "";

            // If our map contains an entry for this string, substitute it.
            if(substitutions.containsKey(to_substitute)){
                substitution = substitutions.get(to_substitute).toString();
            }

            template = template.substring(0, start) + substitution + template.substring(end + TEMPLATE_END_STRING.length());
        }

        return template;
    }

}
