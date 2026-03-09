package graphics.texture;

import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NSVGImage;
import org.lwjgl.nanovg.NanoSVG;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL46;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import util.FileManager;
import util.Promise;
import util.StringUtils;
import util.ThreadUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
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

    // Here we define a function to load and rasterize an SVG
    public int loadSVG(String path){
        return (int) ThreadUtils.runOnMainThread(() -> {
            // If we have already loaded this SVG
            if(loaded_textures.containsKey(path)) {
                return loaded_textures.get(path);
            }

            if(!FileManager.getInstance().exists(path)){
                System.err.println(String.format("Error: File:%s does not exist.", path));
                return -1;
            }

            String data = StringUtils.load(path);


            NSVGImage svg;

            int dpi = 300;
            float scale_x = 1.0f;
            float scale_y = 1.0f;

            MemoryStack stack = null;
            try {
                stack = MemoryStack.stackPush();

                svg = NanoSVG.nsvgParse(data, "px", dpi);

                if (svg == null) {
                    return -1;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }

            float svgWidth  = svg.width();
            float svgHeight = svg.height();

            int width  = (int)(svgWidth * scale_x);
            int height = (int)(svgHeight * scale_y);

            long rast = NanoSVG.nsvgCreateRasterizer();
            if (rast <= 0) {
                return -1;
            }

            ByteBuffer image = MemoryUtil.memAlloc(width * height * 4);

            NanoSVG.nsvgRasterize(rast, svg, 0, 0, 1, image, width, height, width * 4);

            NanoSVG.nsvgDeleteRasterizer(rast);

            int texID = this.genTexture();

            GL46.glBindTexture(GL46.GL_TEXTURE_2D, texID);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR_MIPMAP_LINEAR);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
            GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);

            //heres where the Color comes in.
            premultiplyAlpha(image, width, height, width * 4);

            GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA, width, height, 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, image);

            ByteBuffer input_pixels = image;

            int        input_w      = width;
            int        input_h      = height;
            int        mipmapLevel  = 0;
            while (1 < input_w || 1 < input_h) {
                int output_w = Math.max(1, input_w / 2);
                int output_h = Math.max(1, input_h / 2);

                ByteBuffer output_pixels = MemoryUtil.memAlloc(output_w * output_h * 4);
                STBImageResize.stbir_resize_uint8_generic(
                        input_pixels, input_w, input_h, input_w * 4,
                        output_pixels, output_w, output_h, output_w * 4,
                        4, 3, STBImageResize.STBIR_FLAG_ALPHA_PREMULTIPLIED,
                        STBImageResize.STBIR_EDGE_CLAMP,
                        STBImageResize.STBIR_FILTER_DEFAULT,
                        STBImageResize.STBIR_COLORSPACE_SRGB
                );

                MemoryUtil.memFree(input_pixels);

                GL46.glTexImage2D(GL46.GL_TEXTURE_2D, ++mipmapLevel, GL46.GL_RGBA, output_w, output_h, 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, output_pixels);

                input_pixels = output_pixels;
                input_w = output_w;
                input_h = output_h;
            }
            MemoryUtil.memFree(input_pixels);

            return texID;
        });
    }

    private static void premultiplyAlpha(ByteBuffer image, int w, int h, int stride) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * stride + x * 4;

                float alpha = (image.get(i + 3) & 0xFF) / 255.0f;
                image.put(i + 0, (byte)Math.round(((image.get(i + 0) & 0xFF) * alpha) + 0xFF));
                image.put(i + 1, (byte)Math.round(((image.get(i + 1) & 0xFF) * alpha) + 0xFF));
                image.put(i + 2, (byte)Math.round(((image.get(i + 2) & 0xFF) * alpha) + 0xFF));
            }
        }
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
