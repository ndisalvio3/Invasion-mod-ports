package invmod.common.nexus;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiNexus extends AbstractContainerScreen<ContainerNexus> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("invasion", "textures/nexusgui.png");
    private static final int TEXTURE_SIZE = 256;

    public GuiNexus(ContainerNexus menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(RenderType::guiTextured, BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);

        int progress = menu.getGenerationProgressScaled(26);
        graphics.blit(RenderType::guiTextured, BACKGROUND, leftPos + 126, topPos + 28 + 26 - progress, 185, 26 - progress, 9, progress, TEXTURE_SIZE, TEXTURE_SIZE);
        progress = menu.getCookProgressScaled(18);
        graphics.blit(RenderType::guiTextured, BACKGROUND, leftPos + 31, topPos + 51, 204, 0, progress, 2, TEXTURE_SIZE, TEXTURE_SIZE);

        if (menu.getMode() == 1 || menu.getMode() == 3) {
            graphics.blit(RenderType::guiTextured, BACKGROUND, leftPos + 19, topPos + 29, 176, 0, 9, 31, TEXTURE_SIZE, TEXTURE_SIZE);
            graphics.blit(RenderType::guiTextured, BACKGROUND, leftPos + 19, topPos + 19, 194, 0, 9, 9, TEXTURE_SIZE, TEXTURE_SIZE);
        } else if (menu.getMode() == 2) {
            graphics.blit(RenderType::guiTextured, BACKGROUND, leftPos + 19, topPos + 29, 176, 31, 9, 31, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        if ((menu.getMode() == 0 || menu.getMode() == 2) && menu.isActivating()) {
            progress = menu.getActivationProgressScaled(31);
            graphics.blit(RenderType::guiTextured, BACKGROUND, leftPos + 19, topPos + 29 + 31 - progress, 176, 31 - progress, 9, progress, TEXTURE_SIZE, TEXTURE_SIZE);
        } else if (menu.getMode() == 4 && menu.isActivating()) {
            progress = menu.getActivationProgressScaled(31);
            graphics.blit(RenderType::guiTextured, BACKGROUND, leftPos + 19, topPos + 29 + 31 - progress, 176, 62 - progress, 9, progress, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("gui.invasion.nexus.level", menu.getNexusLevel()), 46, 6, 4210752, false);
        graphics.drawString(font, Component.translatable("gui.invasion.nexus.kills", menu.getNexusKills()), 96, 60, 4210752, false);
        graphics.drawString(font, Component.translatable("gui.invasion.nexus.radius", menu.getSpawnRadius()), 142, 72, 4210752, false);

        if (menu.getMode() == 1 || menu.getMode() == 3) {
            graphics.drawString(font, Component.translatable("gui.invasion.nexus.activated"), 13, 62, 4210752, false);
            graphics.drawString(font, Component.translatable("gui.invasion.nexus.wave", menu.getCurrentWave()), 55, 37, 4210752, false);
        } else if (menu.getMode() == 2) {
            graphics.drawString(font, Component.translatable("gui.invasion.nexus.power"), 56, 31, 4210752, false);
            graphics.drawString(font, Component.literal(String.valueOf(menu.getNexusPowerLevel())), 61, 44, 4210752, false);
        }

        if (menu.isActivating() && menu.getMode() == 0) {
            graphics.drawString(font, Component.translatable("gui.invasion.nexus.activating"), 13, 62, 4210752, false);
            if (menu.getMode() != 4) {
                graphics.drawString(font, Component.translatable("gui.invasion.nexus.confirm"), 8, 72, 4210752, false);
            }
        }
    }
}
