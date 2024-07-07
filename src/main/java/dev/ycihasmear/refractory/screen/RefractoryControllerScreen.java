package dev.ycihasmear.refractory.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ycihasmear.refractory.Refractory;
import dev.ycihasmear.refractory.util.ModResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;

public class RefractoryControllerScreen extends AbstractContainerScreen<RefractoryControllerMenu> {
    private static final ResourceLocation TEXTURE = ModResourceLocation.modLocation("textures/gui/refractory_controller.png");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");
    private float scrollOffs;
    private boolean scrolling;

    public RefractoryControllerScreen(RefractoryControllerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        this.imageWidth = 176;
        this.imageHeight = 175;
        this.inventoryLabelX = 10000;
        this.titleLabelY = this.inventoryLabelY + 10;
        super.init();
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight);

        renderProgressOverlay(guiGraphics);
        renderScrollBar(guiGraphics);
        renderSlots(guiGraphics);
    }

    private void renderSlots(GuiGraphics guiGraphics) {
        int pageNum = calculatePageNum(this.menu.getScrollOffset(), (this.menu.getFurnaceSize()-1) / 2);
        int slotsVisible = this.menu.getFurnaceSize()*6;
        for (int i = 0; i < RefractoryControllerMenu.MAX_ROWS; i++) {
            for (int j = 0; j < RefractoryControllerMenu.COLUMNS; j++) {
                int slot = j + i * 3;
                int row = (slot % 12) / 3;
                if ((slot / 12) == pageNum && slot < slotsVisible){
                    guiGraphics.blit(TEXTURE, this.leftPos + 7 + 18*j, this.topPos + 7 + 18*row, 192, 0, 18, 18);
                }
            }
        }
    }

    protected int calculatePageNum(float scrollPos, int maxPageNum) {
        return Math.round(scrollPos * maxPageNum);
    }

    private void renderProgressOverlay(GuiGraphics guiGraphics) {
        int pageNum = calculatePageNum(this.menu.getScrollOffset(), (this.menu.getFurnaceSize()-1) / 2);
        int slotsVisible = this.menu.getFurnaceSize()*6;
        for (int i = 0; i < RefractoryControllerMenu.MAX_ROWS; i++) {
            for (int j = 0; j < RefractoryControllerMenu.COLUMNS; j++) {
                int slot = j + i * 3;
                int row = (slot % 12) / 3;
                if ((slot / 12) == pageNum && slot < slotsVisible){
                    if(this.menu.isMelting(slot)) {
                        guiGraphics.fill(this.leftPos + 8 + 18 * j, this.topPos + 8 + 18 * row,
                                this.leftPos + 23 + 18 * j, this.topPos + 23 + 18 * row, rgbaToInt(255, 0, 0, 128));
                    }
                }
            }
        }
    }

    private int rgbaToInt(int r, int g, int b, int a){
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                ((b & 0xFF));
    }

    private void renderScrollBar(GuiGraphics guiGraphics){
        ResourceLocation resourcelocation = this.canScroll() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        int i = this.leftPos + 65;
        int j = this.topPos + 8;
        int k = j+72;
        guiGraphics.blitSprite(resourcelocation, i, j + (int)((float)(k - j - 17) * this.scrollOffs), 12, 15);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0 && this.insideScrollbar(pMouseX, pMouseY)) {
            this.scrolling = this.canScroll();
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) this.scrolling = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    private boolean canScroll(){
        return this.menu.canScroll();
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (menu.canScroll()) {
            float scrollAmount = (float)pScrollY / (float)(menu.getFurnaceSize() * 2 - RefractoryControllerMenu.ROWS_VISIBLE);
            this.scrollOffs = Mth.clamp(this.scrollOffs - scrollAmount, 0.0F, 1.0F);
            menu.scrollTo(this.scrollOffs);
            return true;
        }
        return false;
    }

    protected boolean insideScrollbar(double pMouseX, double pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        int k = i + 64;
        int l = j + 7;
        int i1 = k + 14;
        int j1 = l + 72;
        return pMouseX >= (double)k && pMouseY >= (double)l && pMouseX < (double)i1 && pMouseY < (double)j1;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrolling) {
            int i = topPos + 7;
            int j = i + 72;
            this.scrollOffs = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            menu.scrollTo(this.scrollOffs);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}
