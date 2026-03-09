package graphics.ui;

import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.system.MemoryUtil;
import util.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.lwjgl.system.MemoryUtil.NULL;

public class FontLoader {
    private HashMap<String, ByteBuffer> loadedFonts = new HashMap<>();

    private static FontLoader fontLoader;
    private long vg;

    private FontLoader(){
        vg = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_STENCIL_STROKES);

        if (vg == NULL) {
            throw new RuntimeException("Could not init nanovg.");
        }else{
            System.out.println("NanoVG handle:" + vg);
        }
    }

    public void loadFont(String filePath, String id){
        int result = 0;
        try {
            ByteBuffer fontBuffer = StringUtils.loadRaw(StringUtils.getPathToResources() + filePath, 450 * 1024);
            result = NanoVG.nvgCreateFontMem(vg, id, fontBuffer, 0);
            loadedFonts.put(id, fontBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(result < 0){
            System.out.println("Font load error:" + result);
        }else{
            System.out.println("Loaded font.");
        }
    }

    public boolean hasFont(String id){
        return loadedFonts.containsKey(id);
    }

    public void cleanup(){
        for(ByteBuffer data : loadedFonts.values()){
            MemoryUtil.memFree(data);
        }
    }

    public static void initialize() {
        if(fontLoader == null){
            fontLoader = new FontLoader();
        }
    }

    public static FontLoader getInstance() {
        return fontLoader;
    }

}
