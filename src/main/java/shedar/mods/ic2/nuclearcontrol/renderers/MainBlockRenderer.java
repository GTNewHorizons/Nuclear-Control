package shedar.mods.ic2.nuclearcontrol.renderers;

import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;
import shedar.mods.ic2.nuclearcontrol.IRotation;
import shedar.mods.ic2.nuclearcontrol.blocks.BlockNuclearControlMain;
import shedar.mods.ic2.nuclearcontrol.panel.Screen;
import shedar.mods.ic2.nuclearcontrol.renderers.model.ModelInfoPanel;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityAdvancedInfoPanel;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityAdvancedInfoPanelExtender;

@SideOnly(Side.CLIENT)
public class MainBlockRenderer implements ISimpleBlockRenderingHandler {

    private int modelId;

    /**
     * Advanced screens only draw their box from the core's chunk. When the core leaves the view frustum the box
     * disappears even though the extenders are still visible. An extender whose chunk is visible then draws the box
     * instead. The box geometry is identical to the core's, so overlapping copies resolve cleanly in the depth buffer.
     */
    private static final WeakHashMap<TileEntityAdvancedInfoPanel, TileEntityAdvancedInfoPanelExtender> SCREEN_BOX_SOURCES = new WeakHashMap<>();

    private static boolean isChunkInFrustum(int x, int y, int z) {
        Frustrum frustrum = new Frustrum();
        frustrum.setPosition(
                RenderManager.instance.renderPosX,
                RenderManager.instance.renderPosY,
                RenderManager.instance.renderPosZ);
        int cx = x >> 4;
        int cy = y >> 4;
        int cz = z >> 4;
        return frustrum.isBoxInFrustum(cx * 16, cy * 16, cz * 16, cx * 16 + 16, cy * 16 + 16, cz * 16 + 16);
    }

    public static void updateScreenBoxFallback(TileEntityAdvancedInfoPanel core,
            TileEntityAdvancedInfoPanelExtender extender, World world) {
        if (isChunkInFrustum(core.xCoord, core.yCoord, core.zCoord)) return;
        TileEntityAdvancedInfoPanelExtender source = SCREEN_BOX_SOURCES.get(core);
        if (source != null && isChunkInFrustum(source.xCoord, source.yCoord, source.zCoord)) return;
        SCREEN_BOX_SOURCES.put(core, extender);
        world.markBlockRangeForRenderUpdate(
                extender.xCoord - 1,
                extender.yCoord - 1,
                extender.zCoord - 1,
                extender.xCoord + 1,
                extender.yCoord + 1,
                extender.zCoord + 1);
    }

    public MainBlockRenderer(int modelId) {
        this.modelId = modelId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int model, RenderBlocks renderer) {
        if (model == modelId) {
            float[] size = BlockNuclearControlMain.getBlockBounds(metadata);
            block.setBlockBounds(size[0], size[1], size[2], size[3], size[4], size[5]);
            renderer.setRenderBoundsFromBlock(block);
            Tessellator tesselator = Tessellator.instance;
            GL11.glPushMatrix();
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            tesselator.startDrawingQuads();
            tesselator.setNormal(0.0F, -1.0F, 0.0F);
            renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, metadata));
            tesselator.draw();
            tesselator.startDrawingQuads();
            tesselator.setNormal(0.0F, 1.0F, 0.0F);
            renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, metadata));
            tesselator.draw();
            tesselator.startDrawingQuads();
            tesselator.setNormal(0.0F, 0.0F, -1.0F);
            renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, metadata));
            tesselator.draw();
            tesselator.startDrawingQuads();
            tesselator.setNormal(0.0F, 0.0F, 1.0F);
            renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, metadata));
            tesselator.draw();
            tesselator.startDrawingQuads();
            tesselator.setNormal(-1.0F, 0.0F, 0.0F);
            renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, metadata));
            tesselator.draw();
            tesselator.startDrawingQuads();
            tesselator.setNormal(1.0F, 0.0F, 0.0F);
            renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, metadata));
            tesselator.draw();
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            GL11.glPopMatrix();
        }
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int model,
            RenderBlocks renderer) {
        if (model == modelId) {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof IRotation) {
                switch (((IRotation) tileEntity).getFacing()) {
                    case 0:
                        renderer.uvRotateBottom = ((IRotation) tileEntity).getRotation();
                        break;
                    case 1:
                        renderer.uvRotateTop = ((IRotation) tileEntity).getRotation();
                        break;
                    case 2:
                        renderer.uvRotateEast = ((IRotation) tileEntity).getRotation();
                        break;
                    case 3:
                        renderer.uvRotateWest = ((IRotation) tileEntity).getRotation();
                        break;
                    case 4:
                        renderer.uvRotateNorth = ((IRotation) tileEntity).getRotation();
                        break;
                    case 5:
                        renderer.uvRotateSouth = ((IRotation) tileEntity).getRotation();
                        break;

                }
            }
            if (tileEntity instanceof TileEntityAdvancedInfoPanel) {
                TileEntityAdvancedInfoPanel advancedCore = (TileEntityAdvancedInfoPanel) tileEntity;
                if (advancedCore.getScreen() != null)
                    new ModelInfoPanel().renderScreen(block, advancedCore, x, y, z, renderer);
                else renderer.renderStandardBlock(block, x, y, z);

            } else if (tileEntity instanceof TileEntityAdvancedInfoPanelExtender) {
                TileEntityAdvancedInfoPanelExtender advancedExtender = (TileEntityAdvancedInfoPanelExtender) tileEntity;
                boolean wasRendered = false;

                if (IC2NuclearControl.instance.screenManager == null
                        || IC2NuclearControl.instance.screenManager.getScreens().get(
                                IC2NuclearControl.instance.screenManager.getWorldKey(advancedExtender.getWorldObj()))
                                == null) {
                    wasRendered = true;
                } else {
                    for (Screen screen : IC2NuclearControl.instance.screenManager.getScreens().get(
                            IC2NuclearControl.instance.screenManager.getWorldKey(advancedExtender.getWorldObj()))) {
                        if (screen != null && screen.isBlockPartOf(advancedExtender)) {
                            wasRendered = true;
                        }
                    }
                }

                if (!wasRendered) {
                    renderer.renderStandardBlock(block, x, y, z);
                } else {
                    Screen screen = advancedExtender.getScreen();
                    TileEntity core = screen == null ? null : screen.getCore(advancedExtender.getWorldObj());
                    if (core instanceof TileEntityAdvancedInfoPanel
                            && SCREEN_BOX_SOURCES.get(core) == advancedExtender) {
                        new ModelInfoPanel().renderScreen(block, (TileEntityAdvancedInfoPanel) core, x, y, z, renderer);
                    }
                }

            } else {
                renderer.renderStandardBlock(block, x, y, z);
            }

            renderer.uvRotateBottom = 0;
            renderer.uvRotateEast = 0;
            renderer.uvRotateNorth = 0;
            renderer.uvRotateSouth = 0;
            renderer.uvRotateTop = 0;
            renderer.uvRotateWest = 0;
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int i) {
        return true;
    }

    @Override
    public int getRenderId() {
        return IC2NuclearControl.instance.modelId;
    }
}
