package graphics.texture;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL46;
import util.Promise;
import util.ThreadUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class TextureManager {

    private static TextureManager instance;

    // Map of loaded images
    private final HashMap<String, Integer> loaded_textures = new HashMap<>();

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

    public int load(String path){
        return load(path, FilterOption.NEAREST);
    }

    public int load(String path, FilterOption filter){
        return (int) ThreadUtils.runOnMainThread(() -> {
            if(loaded_textures.containsKey(path)){
                return loaded_textures.get(path);
            }else{
                String asset_path = path.replaceAll("\\\\", "/");

                System.out.println("Try load file:" + asset_path);

                File imageFile = new File(asset_path);
                if(!imageFile.exists()){
                    return 0;
                }

                try {
                    BufferedImage buffered_image = ImageIO.read(imageFile);
                    int width = buffered_image.getWidth();
                    int height = buffered_image.getHeight();

                    int[] pixels = buffered_image.getRGB(0, 0, width, height, null, 0, width);
                    int texture_id = genTexture();

                    ByteBuffer pixel_buffer = BufferUtils.createByteBuffer(pixels.length * 4);
                    for (int pixel : pixels) {
                        pixel_buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                        pixel_buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
                        pixel_buffer.put((byte) (pixel & 0xFF));         // B
                        pixel_buffer.put((byte) (pixel >> 24));          // A
                    }
                    pixel_buffer.flip();

                    // Add to the map of loaded images
                    this.loaded_textures.put(asset_path, texture_id);

                    GL46.glBindTexture(GL46.GL_TEXTURE_2D, texture_id);

                    GL46.glGenerateMipmap(GL46.GL_TEXTURE_2D);

                    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
                    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);

                    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, filter.getFilterOption());
                    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, filter.getFilterOption());

                    GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA, width, height, 0,  GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, pixel_buffer);
                    GL46.glBindTexture(GL46.GL_TEXTURE_2D, 0);

                    // Delete our buffer
                    pixel_buffer.clear();

                    return texture_id;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return 0;
        });
    }

    public int genTexture() {
        return (int) ThreadUtils.runOnMainThread(() -> {
            return GL46.glGenTextures();
        });
    }

    // This function is Thread safe and Async
    /**
     * Adapted from https://computergraphics.stackexchange.com/questions/4936/lwjgl-opengl-get-bufferedimage-from-texture-id
     * @param texture_id
     * @param file_name
     */
    public void saveTextureToFile(int texture_id, String file_name) {
        ThreadUtils.runOnMainThread(() -> {
            // Bind the texture
            GL43.glBindTexture(GL43.GL_TEXTURE_2D, texture_id);

            int format = GL43.glGetTexLevelParameteri(GL43.GL_TEXTURE_2D, 0, GL43.GL_TEXTURE_INTERNAL_FORMAT);
            int width  = GL43.glGetTexLevelParameteri(GL43.GL_TEXTURE_2D, 0, GL43.GL_TEXTURE_WIDTH);
            int height = GL43.glGetTexLevelParameteri(GL43.GL_TEXTURE_2D, 0, GL43.GL_TEXTURE_HEIGHT);

            int channels = (format == GL43.GL_RGB) ? 3 : 4;

            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * channels);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            GL43.glGetTexImage(GL43.GL_TEXTURE_2D, 0, GL43.GL_RGBA, GL43.GL_UNSIGNED_BYTE, buffer);

            return new Promise(() -> {
                // Set the pixels of the Buffered Image to our byteBuffer
                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        int i = (x + y * width) * channels;

                        int r = buffer.get(i) & 0xFF;
                        int g = buffer.get(i + 1) & 0xFF;
                        int b = buffer.get(i + 2) & 0xFF;
                        int a = 255;
                        if (channels == 4) {
                            a = buffer.get(i + 3) & 0xFF;
                        }

                        image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                    }
                }

                try {
                    ImageIO.write(image, "PNG", new File(file_name));
                    System.out.println("Done.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
