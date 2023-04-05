package graphics;

import org.lwjgl.opengl.ATIMeminfo;
import org.lwjgl.opengl.NVXGPUMemoryInfo;

public enum GPUType {
    AMD(ATIMeminfo.GL_VBO_FREE_MEMORY_ATI),
    NVIDIA(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX)
    ;

    private int query_address;

    GPUType(int query_address) {
        this.query_address = query_address;
    }

    protected int getQueryAddress(){
        return this.query_address;
    }
}
