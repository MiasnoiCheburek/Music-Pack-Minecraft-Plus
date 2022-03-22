package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GuiScrollingLinkingInfo extends ObjectSelectionList<GuiScrollingLinkingInfo.Entry> {

    public List<String> info;
    private final GuiLinkingInfo IN;
    public int index;

    public GuiScrollingLinkingInfo(Minecraft client, int width, int height, int top, int bottom, List<String> info, GuiLinkingInfo IN) {
        super(client, width, height, top, bottom, 32);
        this.info = info;
        this.IN = IN;
        for(int i=0;i<info.size();i++) {
            this.addEntry(new GuiScrollingLinkingInfo.Entry(info.get(i), i));
        }
    }

    public void refreshList(List<String> info) {
        this.info = info;
        this.IN.info = this.info;
        List<GuiScrollingLinkingInfo.Entry> newEntries = new ArrayList<>();
        for(int i=0;i<this.info.size();i++) {
            newEntries.add(new GuiScrollingLinkingInfo.Entry(this.info.get(i), i));
        }
        this.replaceEntries(newEntries);
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
    public void setSelected(@Nullable GuiScrollingLinkingInfo.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(@NotNull PoseStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends ObjectSelectionList.Entry<GuiScrollingLinkingInfo.Entry> {

        private final String info;
        private final int index;

        public Entry(String info, int i) {
            this.info = info;
            this.index = i;
        }

        
        public @NotNull Component getNarration() {
            return new TranslatableComponent("");
         }

        public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
            if (p_231044_5_ == 0) {
                this.select();
                return true;
            } else {
                return false;
            }
        }

        private void select() {
            GuiScrollingLinkingInfo.this.index = this.index;
            GuiScrollingLinkingInfo.this.setSelected(this);
        }

        public void render(@NotNull PoseStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            GuiScrollingLinkingInfo.this.IN.getMinecraft().font.drawShadow(matrix, this.info, (float)(GuiScrollingLinkingInfo.this.width / 2 - GuiScrollingLinkingInfo.this.IN.getMinecraft().font.width(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}