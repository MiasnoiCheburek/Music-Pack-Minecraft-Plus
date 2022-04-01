package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class GuiScrollingLoopsInfo extends ObjectSelectionList<GuiScrollingLoopsInfo.Entry> {

    public int size;
    public List<String> info;
    private final GuiLoopInfo IN;
    public int index;

    public GuiScrollingLoopsInfo(Minecraft client, int width, int height, int top, int bottom, List<String> info, GuiLoopInfo IN) {
        super(client, width, height, top, bottom, 32);
        this.size = info.size();
        this.info = info;
        this.IN = IN;
        for (String s : info) {
            this.addEntry(new Entry(s));
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 20;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    @Override
    public void setSelected(@Nullable GuiScrollingLoopsInfo.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(@NotNull PoseStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends ObjectSelectionList.Entry<GuiScrollingLoopsInfo.Entry> {

        private final String info;

        public Entry(String info) {
            this.info = info;
        }

        public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
            if (p_231044_5_ == 0) {
                this.select();
                return true;
            } else {
                return false;
            }
        }

        private int getIndex(String s){
            return GuiScrollingLoopsInfo.this.IN.info.indexOf(s);
        }

        public @NotNull Component getNarration() {
            return new TranslatableComponent("");
        }

        private void select() {
            GuiScrollingLoopsInfo.this.index = this.getIndex(this.info);
            GuiScrollingLoopsInfo.this.setSelected(this);
        }

        public void render(@NotNull PoseStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            GuiScrollingLoopsInfo.this.IN.getMinecraft().font.drawShadow(matrix, this.info, (float)(GuiScrollingLoopsInfo.this.width / 2 - GuiScrollingLoopsInfo.this.IN.getMinecraft().font.width(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}