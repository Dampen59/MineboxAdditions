package io.dampen59.mineboxadditions.features.bestiary;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.utils.ImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BestiaryListWidget extends AbstractSelectionList<BestiaryListWidget.EntryRow> {

    private static final int GIF_SIZE = 256;
    private static final int THUMB_SIZE = 20;

    private final int left;

    public BestiaryListWidget(Minecraft client, int left, int top, int width, int height, int itemHeight) {
        super(client, width, top + height, top, itemHeight);
        this.left = left;
        this.setX(left);
    }

    public int getRowLeft() { return left; }

    @Override
    protected int scrollBarX() { return this.getX() + this.width - 6; }

    @Override
    public int getRowWidth() { return this.width - 6; }

    protected void drawSelectionHighlight(GuiGraphicsExtractor ctx, int y, int ew, int eh, int bc, int fc) {}

    protected void appendClickableNarrations(NarrationElementOutput builder) {}

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {}

    public static class EntryRow extends Entry<EntryRow> {

        private static final Map<String, GifAnimation> animationCache = new HashMap<>();

        private final BestiaryEntry entry;
        private final BestiaryScreen parent;

        public EntryRow(BestiaryEntry entry, BestiaryScreen parent) {
            this.entry = entry;
            this.parent = parent;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            Minecraft mc = Minecraft.getInstance();
            int ex = getX(), ey = getY(), rw = getWidth(), rh = getHeight();

            boolean selected = parent.getSelected() == entry;
            ctx.fill(ex, ey, ex + rw, ey + rh,
                selected ? 0x5544AAFF : hovered ? 0x33FFFFFF : 0x00000000);

            Identifier icon = loadAndCacheTexture(entry);
            if (icon != null) {
                int imgY = ey + (rh - THUMB_SIZE) / 2;
                Matrix3x2f backup = new Matrix3x2f(ctx.pose());
                ctx.pose().translate(ex + 4, imgY);
                ctx.pose().scale((float) THUMB_SIZE / GIF_SIZE, (float) THUMB_SIZE / GIF_SIZE);
                ctx.blit(RenderPipelines.GUI_TEXTURED, icon, 0, 0, 0, 0, GIF_SIZE, GIF_SIZE, GIF_SIZE, GIF_SIZE);
                ctx.pose().set(backup);
            }

            int textX = ex + 4 + THUMB_SIZE + 4;
            ctx.text(mc.font, Component.literal(entry.getName()), textX, ey + 5, 0xFFFFFFFF, false);
            ctx.text(mc.font,
                Component.literal("Lvl " + entry.getLevel() + " - " + entry.getLevelMax() + " • " + entry.getFamily()),
                textX, ey + 15, 0xFFAAAAAA, false);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            parent.setSelected(entry);
            return true;
        }

        @Nullable
        public static Identifier loadAndCacheTexture(BestiaryEntry entry) {
            GifAnimation anim = loadAndCacheAnimation(entry);
            return (!anim.isEmpty()) ? anim.getCurrentFrame() : null;
        }

        @Nullable
        public static Identifier getTexture(String id) {
            GifAnimation anim = animationCache.get(id);
            return (anim != null && !anim.isEmpty()) ? anim.getCurrentFrame() : null;
        }

        public static GifAnimation loadAndCacheAnimation(BestiaryEntry entry) {
            return animationCache.computeIfAbsent(entry.getId(), id -> decodeGif(entry, id));
        }

        private static GifAnimation decodeGif(BestiaryEntry entry, String id) {
            try {
                byte[] bytes = Base64.getDecoder().decode(entry.getImage());
                ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
                Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix("gif");
                if (!readers.hasNext()) return GifAnimation.EMPTY;

                ImageReader reader = readers.next();
                reader.setInput(iis);

                int numFrames = reader.getNumImages(true);
                List<Identifier> frames = new ArrayList<>();
                List<Integer> delays = new ArrayList<>();

                for (int i = 0; i < numFrames; i++) {
                    BufferedImage img = reader.read(i);
                    int delay = extractDelay(reader.getImageMetadata(i));
                    Identifier frameId = ImageUtils.createTextureFromBufferedImage(
                        img, "textures/bestiary/" + id + "_f" + i);
                    if (frameId != null) {
                        frames.add(frameId);
                        delays.add(delay);
                    }
                }

                reader.dispose();
                return frames.isEmpty() ? GifAnimation.EMPTY : new GifAnimation(frames, delays);

            } catch (Exception e) {
                MineboxAdditions.LOGGER.error("[Bestiary] Failed to decode GIF for: {}", id, e);
                return GifAnimation.EMPTY;
            }
        }

        private static int extractDelay(IIOMetadata metadata) {
            try {
                Node root = metadata.getAsTree("javax_imageio_gif_image_1.0");
                NodeList children = root.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node node = children.item(i);
                    if ("GraphicControlExtension".equals(node.getNodeName())) {
                        NamedNodeMap attrs = node.getAttributes();
                        Node dn = attrs.getNamedItem("delayTime");
                        if (dn != null) {
                            return Math.max(Integer.parseInt(dn.getNodeValue()) * 10, 20);
                        }
                    }
                }
            } catch (Exception ignored) {}
            return 100;
        }
    }
}
