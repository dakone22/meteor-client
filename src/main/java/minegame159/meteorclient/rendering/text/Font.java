package minegame159.meteorclient.rendering.text;

import minegame159.meteorclient.rendering.MeshBuilder;
import minegame159.meteorclient.utils.render.ByteTexture;
import minegame159.meteorclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Font {
    private static final HashMap<String, String> CHARS_TO_RENDER;
    static {
        CHARS_TO_RENDER = new HashMap<>();
        CHARS_TO_RENDER.put("DEFAULT_CHARS", " !\"#$%&'()*+,-./0123456789:;<=>?@[\\]^_`{|}~");
        CHARS_TO_RENDER.put("en_us", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        CHARS_TO_RENDER.put("ru_ru", "ЁАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯёабвгдежзийклмнопрстуфхцчшщъыьэюя");
    }
    private static final char DEFAULT_CHAR = CHARS_TO_RENDER.get("DEFAULT_CHARS").charAt(0);  // space char

    public final AbstractTexture texture;

    private final int height;
    private final float scale;
    private final float ascent;
    private final HashMap<Character, CharData> charData;

    public static String getCharsToRender() {
        String charsToRender = CHARS_TO_RENDER.get("DEFAULT_CHARS") + CHARS_TO_RENDER.get("en_us");

        String code = MinecraftClient.getInstance().getLanguageManager().getLanguage().getCode();
        if (code != null && !code.isEmpty() && !code.equals("en_us") && CHARS_TO_RENDER.containsKey(code))
            charsToRender += CHARS_TO_RENDER.get(code);

        char[] _charsToRender = charsToRender.toCharArray();  // sorting chars
        Arrays.sort(_charsToRender);

        return new String(_charsToRender);
    }

    public Font(ByteBuffer buffer, int height) {
        this.height = height;
        String charsToRender = getCharsToRender();
        int bitmap_w = 2048;
        int bitmap_h = 2048;

        // Initialize font
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        STBTruetype.stbtt_InitFont(fontInfo, buffer);

        charData = new HashMap<>();
        STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(charsToRender.length());
        ByteBuffer bitmap = BufferUtils.createByteBuffer(bitmap_w * bitmap_h);

        // Create font texture
        STBTTPackContext packContext = STBTTPackContext.create();
        STBTruetype.stbtt_PackBegin(packContext, bitmap, bitmap_w, bitmap_h, 0, 1);
        STBTruetype.stbtt_PackSetOversampling(packContext, 2, 2);

        //// Getting char ranges
        char[] chars = charsToRender.toCharArray();

        List<Pair<Integer, Integer>> ranges = new ArrayList<>();
        int start = 0, end = 0;
        for (int i = 1; i < chars.length; ++i)
            if (chars[i - 1] == chars[i] - 1) {
                end++;
            } else {
                ranges.add(new Pair<>(start, end));
                start = i;
                end = i;
            }
        ranges.add(new Pair<>(start, end));

        //// https://github.com/LWJGL/lwjgl3/blob/3c92f417252da6f2b6cbfba75db9e4c62bc28a9e/modules/samples/src/test/java/org/lwjgl/demo/stb/TruetypeOversample.java#L98
        int nextPos = 0;
        for (Pair<Integer, Integer> range : ranges) {
            start = range.getLeft();
            end = range.getRight();

            cdata.limit(nextPos + (end - start) + 1);
            cdata.position(nextPos);
            STBTruetype.stbtt_PackFontRange(packContext, buffer, 0, height, charsToRender.charAt(start), cdata);

            nextPos = nextPos + (end - start) + 1;
        }
        //// ---

        STBTruetype.stbtt_PackEnd(packContext);

        // Create texture object and get font scale
        texture = new ByteTexture(bitmap_w, bitmap_h, bitmap, true);
        scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height);

        // Get font vertical ascent
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ascent = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, null, null);
            this.ascent = ascent.get(0);
        }

        // Populate charData array
        for (int i = 0; i < charsToRender.length(); i++) {
            STBTTPackedchar packedChar = cdata.get(i);

            float ipw = 1f / bitmap_w;
            float iph = 1f / bitmap_h;

            charData.put(charsToRender.charAt(i), new CharData(
                    packedChar.xoff(),
                    packedChar.yoff(),
                    packedChar.xoff2(),
                    packedChar.yoff2(),
                    packedChar.x0() * ipw,
                    packedChar.y0() * iph,
                    packedChar.x1() * ipw,
                    packedChar.y1() * iph,
                    packedChar.xadvance()
            ));
        }
    }

    private CharData getCharData(char cp) {
        if (!charData.containsKey(cp)) cp = DEFAULT_CHAR;
        return charData.get(cp);
    }

    public double getWidth(String string, int length) {
        double width = 0;

        for (int i = 0; i < length; i++) {
            CharData c = getCharData(string.charAt(i));

            width += c.xAdvance;
        }

        return width;
    }

    public double getHeight() {
        return height;
    }

    public double render(MeshBuilder mb, String string, double x, double y, Color color, double scale) {
        y += ascent * this.scale * scale;

        for (int i = 0; i < string.length(); i++) {
            CharData c = getCharData(string.charAt(i));

            mb.pos(x + c.x0 * scale, y + c.y0 * scale, 0).color(color).texture(c.u0, c.v0).endVertex();
            mb.pos(x + c.x1 * scale, y + c.y0 * scale, 0).color(color).texture(c.u1, c.v0).endVertex();
            mb.pos(x + c.x1 * scale, y + c.y1 * scale, 0).color(color).texture(c.u1, c.v1).endVertex();

            mb.pos(x + c.x0 * scale, y + c.y0 * scale, 0).color(color).texture(c.u0, c.v0).endVertex();
            mb.pos(x + c.x1 * scale, y + c.y1 * scale, 0).color(color).texture(c.u1, c.v1).endVertex();
            mb.pos(x + c.x0 * scale, y + c.y1 * scale, 0).color(color).texture(c.u0, c.v1).endVertex();

            x += c.xAdvance * scale;
        }

        return x;
    }
}
