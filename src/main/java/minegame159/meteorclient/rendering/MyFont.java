/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2020 Meteor Development.
 */

package minegame159.meteorclient.rendering;

import it.unimi.dsi.fastutil.chars.Char2CharArrayMap;
import it.unimi.dsi.fastutil.chars.Char2CharMap;
import minegame159.meteorclient.utils.ByteTexture;
import minegame159.meteorclient.utils.Color;
import minegame159.meteorclient.utils.Utils;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MyFont {
    private static final String CHARS;
    static {
        char[] chars = (" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~" +
                        "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъыьэюяЁё")
                .toCharArray();
        Arrays.sort(chars);
        CHARS = new String(chars);
    }
    private static final char DEFAULT_CHAR = CHARS.charAt(0);

    private static final int BITMAP_W = 768, BITMAP_H = 768;
    private static final Color SHADOW_COLOR = new Color(60, 60, 60, 180);

    private final MeshBuilder mb = new MeshBuilder(16384);

    public final AbstractTexture texture;

    private final int height;
    private final float scale;
    private final float ascent;
    private final HashMap<Character, CharData> charData;

    private double mScale;

    public MyFont(File file, int height) {
        this.height = height;

        // Read file
        byte[] bytes = Utils.readBytes(file);
        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length).put(bytes);
        ((Buffer) buffer).flip();

        // Initialize font
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        STBTruetype.stbtt_InitFont(fontInfo, buffer);

        // Allocate STBTTPackedchar buffer
        charData = new HashMap<>();
        STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(CHARS.length());
        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);

        // Create font texture
        STBTTPackContext packContext = STBTTPackContext.create();
        STBTruetype.stbtt_PackBegin(packContext, bitmap, BITMAP_W, BITMAP_H, 0, 1);
        STBTruetype.stbtt_PackSetOversampling(packContext, 2, 2);

        //// Getting char ranges
        char[] chars = CHARS.toCharArray();

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
            STBTruetype.stbtt_PackFontRange(packContext, buffer, 0, height, CHARS.charAt(start), cdata);

            nextPos = nextPos + (end - start) + 1;
        }
        //// ---

        STBTruetype.stbtt_PackEnd(packContext);

        // Create texture object and get font scale
        texture = new ByteTexture(BITMAP_W, BITMAP_H, bitmap, true);
        scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height);

        // Get font vertical ascent
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ascent = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, null, null);
            this.ascent = ascent.get(0);
        }

        // Populate charData array
        for (int i = 0; i < CHARS.length(); i++) {
            STBTTPackedchar packedChar = cdata.get(i);

            float ipw = 1f / BITMAP_W;
            float iph = 1f / BITMAP_H;

            charData.put(CHARS.charAt(i), new CharData(
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

        mb.texture = true;
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

    public double getWidth(String string) {
        return getWidth(string, string.length());
    }

    public double getHeight() {
        return height;
    }

    public void begin(double scale) {
        this.mScale = scale;

        mb.begin(null, DrawMode.Triangles, VertexFormats.POSITION_COLOR_TEXTURE);
    }

    public void begin() {
        begin(1);
    }

    public boolean isBuilding() {
        return mb.isBuilding();
    }

    public void end() {
        texture.bindTexture();
        mb.end();
    }

    public void render(String string, double x, double y, Color color) {
        boolean wasBuilding = isBuilding();
        if (!isBuilding()) begin();

        y += ascent * scale * mScale;

        for (int i = 0; i < string.length(); i++) {
            CharData c = getCharData(string.charAt(i));

            mb.pos(x + c.x0 * mScale, y + c.y0 * mScale, 0).color(color).texture(c.u0, c.v0).endVertex();
            mb.pos(x + c.x1 * mScale, y + c.y0 * mScale, 0).color(color).texture(c.u1, c.v0).endVertex();
            mb.pos(x + c.x1 * mScale, y + c.y1 * mScale, 0).color(color).texture(c.u1, c.v1).endVertex();

            mb.pos(x + c.x0 * mScale, y + c.y0 * mScale, 0).color(color).texture(c.u0, c.v0).endVertex();
            mb.pos(x + c.x1 * mScale, y + c.y1 * mScale, 0).color(color).texture(c.u1, c.v1).endVertex();
            mb.pos(x + c.x0 * mScale, y + c.y1 * mScale, 0).color(color).texture(c.u0, c.v1).endVertex();

            x += c.xAdvance * mScale;
        }

        if (!wasBuilding) end();
    }

    public void renderWithShadow(String string, double x, double y, Color color) {
        render(string, x + 1, y + 1, SHADOW_COLOR);
        render(string, x, y, color);
    }

    private static class CharData {
        public final float x0, y0, x1, y1;
        public final float u0, v0, u1, v1;
        public final float xAdvance;

        public CharData(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float xAdvance) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            this.xAdvance = xAdvance;
        }

        public String toString() {
            return String.format("CharData(x0=%f, y0=%f, x1=%f, y1=%f, u0=%f, v0=%f, u1=%f, v1=%f, xA=%f)", x0, y0, x1, y1, u0, v0, u1, v1, xAdvance);
        }
    }
}
