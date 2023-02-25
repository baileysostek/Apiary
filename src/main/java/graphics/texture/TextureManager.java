package graphics.texture;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL46;
import util.ThreadUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class TextureManager {

    private static TextureManager instance;

    private TextureManager(){

    }

    public static void initialize(){
        if(instance == null){
            instance = new TextureManager();
        }
    }

    public static TextureManager getInstance(){
        return instance;
    }

    public int genTexture() {
        return (int) ThreadUtils.runOnMainThread(() -> {
            return GL46.glGenTextures();
        });
    }

    /**
     * Adapted from https://computergraphics.stackexchange.com/questions/4936/lwjgl-opengl-get-bufferedimage-from-texture-id
     * @param texture_id
     * @param file_name
     */
    public void saveTextureToFile(int texture_id, String file_name){
        // Bind the texture
        GL43.glBindTexture(GL43.GL_TEXTURE_2D, texture_id);

        int format = GL43.glGetTexLevelParameteri(GL43.GL_TEXTURE_2D, 0, GL43.GL_TEXTURE_INTERNAL_FORMAT);
        int width  = GL43.glGetTexLevelParameteri(GL43.GL_TEXTURE_2D, 0, GL43.GL_TEXTURE_WIDTH);
        int height = GL43.glGetTexLevelParameteri(GL43.GL_TEXTURE_2D, 0, GL43.GL_TEXTURE_HEIGHT);

        int channels = (format == GL43.GL_RGB) ? 3 : 4;

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * channels);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        GL43.glGetTexImage(GL43.GL_TEXTURE_2D, 0, GL43.GL_RGBA, GL43.GL_UNSIGNED_BYTE, buffer);

        // Set the pixels of the Buffered Image to our byteBuffer
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                int i = (x + y * width) * channels;

                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = 255;
//                if (channels == 4)
//                    a = buffer.get(i + 3) & 0xFF;

//                RGBA
                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try {
            ImageIO.write(image, "PNG", new File(file_name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
