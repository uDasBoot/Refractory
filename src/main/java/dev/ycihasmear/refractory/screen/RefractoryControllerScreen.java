package dev.ycihasmear.refractory.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ycihasmear.refractory.util.ModMathHelpers;
import dev.ycihasmear.refractory.util.ModResourceLocation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

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

    protected int calculatePageNum(float scrollPos, int maxPageNum) {
        return Math.round(scrollPos * maxPageNum);
    }

    private boolean canScroll() {
        return this.menu.canScroll();
    }

    private boolean insideScrollbar(double pMouseX, double pMouseY) {
        int i = this.leftPos + 64;
        int j = this.topPos + 7;
        return pMouseX >= i && pMouseY >= j && pMouseX < (i + 14) && pMouseY < (j + 72);
    }

    private int getYOffset(int startPos, int maxHeight, int height) {
        return this.topPos + startPos + maxHeight - height;
    }

    private int getFluidHeight() {
        return ModMathHelpers.getScaledHeight(70, this.menu.getFluidTank().getFluidAmount(), this.menu.getFluidCapacity());
    }

    private int getEnergyHeight() {
        return ModMathHelpers.getScaledHeight(70, this.menu.getEnergyStored(), this.menu.getEnergyCapacity());
    }

    private float[] getColorForProgress(float progress) {
        float[] color = new float[3];
        if (progress < 0.5f) {
            color[0] = 1.0f;
            color[1] = progress * 2;
            color[2] = 0.0f;
        } else {
            color[0] = 1.0f;
            color[1] = 1.0f;
            color[2] = (progress - 0.5f) * 2;
        }
        return color;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight);

        renderSlots(guiGraphics);
        renderScrollBar(guiGraphics);
        renderProgressOverlay(guiGraphics);
        renderFluidTank(guiGraphics);
        renderEnergyBar(guiGraphics);
    }

    private void renderSlots(GuiGraphics guiGraphics) {
        int pageNum = calculatePageNum(this.menu.getScrollOffset(), (this.menu.getFurnaceSize() - 1) / 2);
        int slotsVisible = this.menu.getFurnaceSize() * 6;
        for (int i = 0; i < RefractoryControllerMenu.MAX_ROWS; i++) {
            for (int j = 0; j < RefractoryControllerMenu.COLUMNS; j++) {
                int slot = j + i * 3;
                int row = (slot % 12) / 3;
                if ((slot / 12) == pageNum && slot < slotsVisible) {
                    guiGraphics.blit(TEXTURE, this.leftPos + 7 + 18 * j, this.topPos + 7 + 18 * row, 192, 0, 18, 18);
                }
            }
        }
    }

    private void renderScrollBar(GuiGraphics guiGraphics) {
        ResourceLocation resourcelocation = this.canScroll() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        int i = this.leftPos + 65;
        int j = this.topPos + 8;
        int k = j + 72;
        guiGraphics.blitSprite(resourcelocation, i, j + (int) ((float) (k - j - 17) * this.scrollOffs), 12, 15);
    }

    private void renderProgressOverlay(GuiGraphics guiGraphics) {
        int pageNum = calculatePageNum(this.menu.getScrollOffset(), (this.menu.getFurnaceSize() - 1) / 2);
        int slotsVisible = this.menu.getFurnaceSize() * 6;
        for (int i = 0; i < RefractoryControllerMenu.MAX_ROWS; i++) {
            for (int j = 0; j < RefractoryControllerMenu.COLUMNS; j++) {
                int slot = j + i * 3;
                int row = (slot % 12) / 3;
                if ((slot / 12) == pageNum && slot < slotsVisible) {
                    int progress = this.menu.getProgressForSlot(slot);
                    int maxProgress = this.menu.getMaxProgress();
                    int progressBarHeight = ModMathHelpers.getScaledHeight(16, progress, maxProgress);

                    if (progress > 0) {
                        float progressPercentage = (float) progress / maxProgress;
                        float[] color = getColorForProgress(progressPercentage);

                        guiGraphics.setColor(color[0], color[1], color[2], 0.5f);
                        int x1 = this.leftPos + 8 + 18 * j;
                        int y1 = this.topPos + (16 - progressBarHeight) + 8 + 18 * row;
                        guiGraphics.fill(x1, y1, x1 + 3, y1 + progressBarHeight, 0xFFFFFFFF);

                        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    }
                }
            }
        }
    }

    private void renderFluidTank(GuiGraphics guiGraphics) {
        FluidTank fluidTank = this.menu.getFluidTank();
        if (fluidTank.isEmpty()) return;

        FluidStack fluidStack = fluidTank.getFluid();
        IClientFluidTypeExtensions fte = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation still = fte.getStillTexture();

        TextureAtlasSprite sprite = this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
        int tintColor = fte.getTintColor();
        int fluidHeight = getFluidHeight();

        float a = ((tintColor >> 24) & 0xFF) / 255.0F;
        float r = ((tintColor >> 16) & 0xFF) / 255.0F;
        float g = ((tintColor >> 8) & 0xFF) / 255.0F;
        float b = (tintColor & 0xFF) / 255.0F;

        guiGraphics.setColor(r, g, b, a);
        guiGraphics.blit(this.leftPos + 84, getYOffset(8, 70, fluidHeight), 0, 62, fluidHeight, sprite);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics) {
        int energyHeight = getEnergyHeight();
        if (energyHeight > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 152, getYOffset(8, 70, energyHeight), 176, 0, 16, energyHeight);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);

        if (isHovering(84, 8, 62, 70, pX, pY) && getFluidHeight() > 0) {
            FluidStack fluidStack = this.menu.getFluidTank().getFluid();
            Component component = MutableComponent.create(fluidStack.getDisplayName().getContents())
                    .append(" (%s/%s) mB".formatted(fluidStack.getAmount(), this.menu.getFluidCapacity()));
            pGuiGraphics.renderTooltip(this.font, component, pX, pY);
        }

        if (isHovering(152, 8, 16, 70, pX, pY)) {
            Component component = Component.literal("(%s/%s) FE".formatted(this.menu.getEnergyStored(),
                    this.menu.getEnergyCapacity()));
            pGuiGraphics.renderTooltip(this.font, component, pX, pY);
        }
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

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (menu.canScroll()) {
            float scrollAmount = (float) pScrollY / (float) (menu.getFurnaceSize() * 2 - RefractoryControllerMenu.ROWS_VISIBLE);
            this.scrollOffs = Mth.clamp(this.scrollOffs - scrollAmount, 0.0F, 1.0F);
            menu.scrollTo(this.scrollOffs);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrolling) {
            int i = topPos + 7;
            int j = i + 72;
            this.scrollOffs = ((float) mouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            menu.scrollTo(this.scrollOffs);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}
