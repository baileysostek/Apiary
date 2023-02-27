package util;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.nfd.NativeFileDialog;

import static org.lwjgl.system.MemoryStack.stackPush;

public class FileManager {
    private static FileManager instance;

    private FileManager() {

    }

    public static void initialize() {
        if (instance == null) {
            instance = new FileManager();
        }
    }

    public static FileManager getInstance() {
        return instance;
    }

    public String openFilePicker(String path, String[] accepted_extensions) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);

            String comma_delimited_accepted_extensions = String.join(",", accepted_extensions);

            return checkResult(NativeFileDialog.NFD_OpenDialog(comma_delimited_accepted_extensions, path, pp), pp);
        }catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

    private String checkResult(int result, PointerBuffer path) {
        switch (result) {
            case NativeFileDialog.NFD_OKAY:
                System.out.println("Success!");
                String file_path = path.getStringUTF8(0);
//                NativeFileDialog.nNFD_PathSet_Free(path.get(0));
                return file_path;
            case NativeFileDialog.NFD_CANCEL:
                System.out.println("User pressed cancel.");
                break;
            default: // NFD_ERROR
                System.err.format("Error: %s\n", NativeFileDialog.NFD_GetError());
        }
        return "";
    }
}
