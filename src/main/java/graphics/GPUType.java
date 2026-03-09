package graphics;

import org.lwjgl.opengl.ATIMeminfo;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.NVXGPUMemoryInfo;
import org.lwjgl.system.MemoryUtil;

public enum GPUType {
    AMD(() -> {
        long available_memory = GL43.glGetInteger(ATIMeminfo.GL_VBO_FREE_MEMORY_ATI) * 1000L;
        if(!ShaderManager.getInstance().checkForError("No drivers found for an AMD GPU.")){
            return available_memory;
        }
        return 0;
    }),
    INTEL(() -> {
        return 0;
    }),
    NVIDIA(() -> {
        long available_memory = GL43.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX) * 1000L;
        if(!ShaderManager.getInstance().checkForError("No drivers found for an AMD GPU.")){
            return available_memory;
        }
        return 0;
    })
    ;

    @FunctionalInterface
    interface GetAvailableMemory {
        long call();
    }
    private GetAvailableMemory get_available_memory;

    GPUType(GetAvailableMemory get_available_memory) {
        this.get_available_memory = get_available_memory;
    }

    protected long getAvailableMemoryInBytes(){
        return get_available_memory.call();
    }
}
