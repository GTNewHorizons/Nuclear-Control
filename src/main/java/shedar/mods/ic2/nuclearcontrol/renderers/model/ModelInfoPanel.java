package shedar.mods.ic2.nuclearcontrol.renderers.model;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Facing;

import org.lwjgl.opengl.GL11;

import shedar.mods.ic2.nuclearcontrol.panel.Screen;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityAdvancedInfoPanel;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelInfoPanel {
	private static final String TEXTURE_FILE = "nuclearcontrol:textures/blocks/infoPanel/panelAdvancedSide.png";
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(TEXTURE_FILE);

	private double[] coordinates = new double[24];
	private static final byte[][] pointMap = {
		{0, 3, 2, 1},
		{4, 5, 6, 7},
		{0, 4, 7, 3},
		{6, 5, 1, 2},
		{5, 4, 0, 1},
		{2, 3, 7, 6} 
	};
	private static final byte[][] normalMap = {
		{0, -1, 0},
		{0, 1, 0},
		{0, 0, -1},
		{0, 0, 1},
		{-1, 0, 0},
		{1, 0, 0} 
	};

	private void assignWithRotation(int rotation, int offset, int sign, int tl,
			int tr, int br, int bl, double dtl, double dtr, double dbr,
			double dbl) {
		switch (rotation) {
		case 0:
			coordinates[tl * 3 + offset] += sign * dtl;
			coordinates[tr * 3 + offset] += sign * dtr;
			coordinates[br * 3 + offset] += sign * dbr;
			coordinates[bl * 3 + offset] += sign * dbl;
			break;
		case 1:
			coordinates[tl * 3 + offset] += sign * dbl;
			coordinates[tr * 3 + offset] += sign * dtl;
			coordinates[br * 3 + offset] += sign * dtr;
			coordinates[bl * 3 + offset] += sign * dbr;
			break;
		case 2:
			coordinates[tl * 3 + offset] += sign * dtr;
			coordinates[tr * 3 + offset] += sign * dbr;
			coordinates[br * 3 + offset] += sign * dbl;
			coordinates[bl * 3 + offset] += sign * dtl;
			break;
		case 3:
			coordinates[tl * 3 + offset] += sign * dbr;
			coordinates[tr * 3 + offset] += sign * dbl;
			coordinates[br * 3 + offset] += sign * dtl;
			coordinates[bl * 3 + offset] += sign * dtr;
			break;

		default:
			break;
		}
	}

	public double[] getDeltas(TileEntityAdvancedInfoPanel panel, Screen screen) {
		boolean isTopBottom = panel.rotateVert != 0;
		boolean isLeftRight = panel.rotateHor != 0;
		double dTopLeft = 0;
		double dTopRight = 0;
		double dBottomLeft = 0;
		double dBottomRight = 0;
		int height = screen.getHeight(panel);
		int width = screen.getWidth(panel);
		double maxDelta = 0;
		if (isTopBottom) {
			if (panel.rotateVert > 0) // |\
			{ // | \
				dBottomRight = dBottomLeft = height
						* Math.tan(Math.PI * panel.rotateVert / 180);
				maxDelta = dBottomLeft;
			} else {
				dTopRight = dTopLeft = height
						* Math.tan(Math.PI * -panel.rotateVert / 180);
				maxDelta = dTopRight;
			}
		}
		if (isLeftRight) {
			if (panel.rotateHor > 0) // -------
			{ // | . '
				dTopRight = dBottomRight = width
						* Math.tan(Math.PI * panel.rotateHor / 180);
				maxDelta = dTopRight;
			} else {
				dTopLeft = dBottomLeft = width
						* Math.tan(Math.PI * -panel.rotateHor / 180);
				maxDelta = dBottomLeft;
			}
		}
		if (isTopBottom && isLeftRight) {
			if (dTopLeft == 0) {
				maxDelta = dBottomRight = dBottomLeft + dTopRight;
			} else if (dTopRight == 0) {
				maxDelta = dBottomLeft = dTopLeft + dBottomRight;
			} else if (dBottomLeft == 0) {
				maxDelta = dTopRight = dTopLeft + dBottomRight;
			} else {
				maxDelta = dTopLeft = dBottomLeft + dTopRight;
			}
		}
		double thickness = panel.thickness / 16D;
		if (maxDelta > thickness) {
			double scale = thickness / maxDelta;
			dTopLeft = scale * dTopLeft;
			dTopRight = scale * dTopRight;
			dBottomLeft = scale * dBottomLeft;
			dBottomRight = scale * dBottomRight;
		}
		double[] res = { dTopLeft, dTopRight, dBottomLeft, dBottomRight };
		return res;
	}

	private void addSlopes(TileEntityAdvancedInfoPanel panel, Screen screen,
			double[] deltas) {
		// if (panel.rotateVert == 0 && panel.rotateHor == 0)
		// return;
		double dTopLeft = deltas[0];
		double dTopRight = deltas[1];
		double dBottomLeft = deltas[2];
		double dBottomRight = deltas[3];
		int facing = panel.facing;
		int rotation = panel.getRotation();
		switch (facing) {
		case 0:
			assignWithRotation(rotation, 1, -1, 4, 7, 6, 5, dTopLeft,
					dTopRight, dBottomRight, dBottomLeft);
			break;
		case 1:
			assignWithRotation(rotation, 1, 1, 3, 0, 1, 2, dTopLeft, dTopRight,
					dBottomRight, dBottomLeft);
			break;
		case 2:
			assignWithRotation(rotation, 2, -1, 5, 6, 2, 1, dTopLeft,
					dTopRight, dBottomRight, dBottomLeft);
			break;
		case 3:
			assignWithRotation(rotation, 2, 1, 7, 4, 0, 3, dTopLeft, dTopRight,
					dBottomRight, dBottomLeft);
			break;
		case 4:
			assignWithRotation(rotation, 0, -1, 6, 7, 3, 2, dTopLeft,
					dTopRight, dBottomRight, dBottomLeft);
			break;
		case 5:
			assignWithRotation(rotation, 0, 1, 4, 5, 1, 0, dTopLeft, dTopRight,
					dBottomRight, dBottomLeft);
			break;
		}
	}

	private void initCoordinates(Block block, Screen screen) {

		//   5 -------6
		//  /|       /|
		// 4 -------7 |
		// | |      | |
		// | 1 -----|-2
	    // |/	    |/
		// 0 ------ 3
		double blockMinX = block.getBlockBoundsMinX();
		double blockMinY = block.getBlockBoundsMinY();
		double blockMinZ = block.getBlockBoundsMinZ();

		double blockMaxX = block.getBlockBoundsMaxX();
		double blockMaxY = block.getBlockBoundsMaxY();
		double blockMaxZ = block.getBlockBoundsMaxZ();

		/* 0 */
		coordinates[0] = screen.minX + blockMinX;
		coordinates[1] = screen.minY + blockMinY;
		coordinates[2] = screen.minZ + blockMinZ;
		/* 1 */
		coordinates[3] = screen.minX + blockMinX;
		coordinates[4] = screen.minY + blockMinY;
		coordinates[5] = screen.maxZ + blockMaxZ;
		/* 2 */
		coordinates[6] = screen.maxX + blockMaxX;
		coordinates[7] = screen.minY + blockMinY;
		coordinates[8] = screen.maxZ + blockMaxZ;
		/* 3 */
		coordinates[9] = screen.maxX + blockMaxX;
		coordinates[10] = screen.minY + blockMinY;
		coordinates[11] = screen.minZ + blockMinZ;
		/* 4 */
		coordinates[12] = screen.minX + blockMinX;
		coordinates[13] = screen.maxY + blockMaxY;
		coordinates[14] = screen.minZ + blockMinZ;
		/* 5 */
		coordinates[15] = screen.minX + blockMinX;
		coordinates[16] = screen.maxY + blockMaxY;
		coordinates[17] = screen.maxZ + blockMaxZ;
		/* 6 */
		coordinates[18] = screen.maxX + blockMaxX;
		coordinates[19] = screen.maxY + blockMaxY;
		coordinates[20] = screen.maxZ + blockMaxZ;
		/* 7 */
		coordinates[21] = screen.maxX + blockMaxX;
		coordinates[22] = screen.maxY + blockMaxY;
		coordinates[23] = screen.minZ + blockMinZ;
	}

	private void addPoint(int point, double u, double v) {
		Tessellator.instance.addVertexWithUV(coordinates[point * 3],
				coordinates[point * 3 + 1], coordinates[point * 3 + 2], u, v);
	}

	private void addPoints(byte[] points, byte[] n, double u1, double u2, double v1, double v2) {
		Tessellator.instance.setNormal(n[0], n[1], n[2]);
		addPoint(points[0], u1, v1);
		addPoint(points[1], u1, v2);
		addPoint(points[2], u2, v2);
		addPoint(points[3], u2, v1);
	}

	private double[] normalize(double[] vec) {
		double len = Math.sqrt((vec[0]*vec[0]) + (vec[1]*vec[1]) + (vec[2]*vec[2]));
		return new double[] {vec[0]/len, vec[1]/len, vec[2]/len};
	}

	private double[] scale(double[] vec, double scale) {
		return new double[] {vec[0]*scale, vec[1]*scale, vec[2]*scale};
	}

	private double[] vectorBetweenPoints(double[] vec1, double[] vec2) {
		return new double[] {vec1[0] - vec2[0], vec1[1] - vec2[1], vec1[2] - vec2[2]};
	}
	private void drawScreenWithBorder(byte[] points, byte[] n, double u1, double u2, double v1, double v2, double border, int facing) {
		Tessellator.instance.setNormal(n[0], n[1], n[2]);
		double[][] UVMap = {{u1, v1},{u1, v2},{u2, v2},{u2, v1}};
		byte[] edges = {points[3], points[0], points[1], points[2], points[3], points[0]};

		for (int i = 1; i < 5; i++) {
			double[] edge1 = scale(normalize(vectorBetweenPoints(
				new double[] {coordinates[edges[i]*3], coordinates[edges[i]*3+1], coordinates[edges[i]*3+2]},
				new double[] {coordinates[edges[i+1]*3], coordinates[edges[i+1]*3+1], coordinates[edges[i+1]*3+2]})), border);
			double[] edge2 = scale(normalize(vectorBetweenPoints(
				new double[] {coordinates[edges[i]*3], coordinates[edges[i]*3+1], coordinates[edges[i]*3+2]},
				new double[] {coordinates[edges[i-1]*3], coordinates[edges[i-1]*3+1], coordinates[edges[i-1]*3+2]})), border);

			Tessellator.instance.addVertexWithUV(
				coordinates[edges[i]*3] - edge1[0] - edge2[0] + 0.001 * Facing.offsetsXForSide[facing],
				coordinates[edges[i]*3 + 1] - edge1[1] - edge2[1] + 0.001 * Facing.offsetsYForSide[facing],
				coordinates[edges[i]*3 + 2] - edge1[2] - edge2[2] + 0.001 * Facing.offsetsZForSide[facing], UVMap[i-1][0], UVMap[i-1][1]);
		}

	}

	private void drawFacing(int facing, int rotation, Screen screen, TileEntityAdvancedInfoPanel panel, Block block, Tessellator tess) {

		IIcon texture = block.getIcon(panel.getWorldObj(), panel.xCoord, panel.yCoord, panel.zCoord, 0);

		double u1 = texture.getMinU();
		double u2 = texture.getMaxU();
		double v1 = texture.getMinV();
		double v2 = texture.getMaxV();
		GL11.glDepthMask(false);
		addPoints(pointMap[facing], normalMap[facing], u1, u2, v1, v2);
		GL11.glDepthMask(true);
		texture = block.getIcon(panel.getWorldObj(), panel.xCoord, panel.yCoord, panel.zCoord, facing);

		u1 = texture.getMinU();
		u2 = texture.getMaxU();
		v1 = texture.getMinV();
		v2 = texture.getMaxV();
		
		drawScreenWithBorder(pointMap[facing], normalMap[facing], u1, u2, v1, v2, 0.05, facing);

	}

	public void renderScreen(Block block, TileEntityAdvancedInfoPanel panel, double x, double y, double z, RenderBlocks renderer) {
		Screen screen = panel.getScreen();
		if (screen == null)
			return;
		initCoordinates(block, screen);
		double[] deltas = getDeltas(panel, screen);
		addSlopes(panel, screen, deltas);

		int facing = panel.getFacing();
		Tessellator tess = Tessellator.instance;

		tess.setBrightness(block.getMixedBrightnessForBlock(panel.getWorldObj(), panel.xCoord, panel.yCoord, panel.zCoord));
		tess.setColorOpaque_F(0.5F, 0.5F, 0.5F);

		if (panel.getTransparencyMode() == 0) { //Check if face should be transparent
			drawFacing(facing, panel.getRotation(), screen, panel, block, tess);
		}
        //
		tess.draw();
		
		//SIDES
		if (panel.getTransparencyMode() == 0) { //Check if block should be transparent
			Tessellator.instance.startDrawingQuads();
			renderer.minecraftRB.renderEngine.bindTexture(TEXTURE_LOCATION);
			Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(panel.getWorldObj(), panel.xCoord, panel.yCoord, panel.zCoord));
			Tessellator.instance.setColorOpaque_F(0.5F, 0.5F, 0.5F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			int dx = screen.getDx() + 1;
			int dy = screen.getDy() + 1;
			int dz = screen.getDz() + 1;

			// bottom
			if (facing != 0) {
				Tessellator.instance.setNormal(0, -1, 0);
				addPoint(0, 0, 0);
				addPoint(3, dx, 0);
				addPoint(2, dx, dz);
				addPoint(1, 0, dz);
			}

			if (facing != 1) {
				Tessellator.instance.setNormal(0, 1, 0);
				addPoint(4, 0, 0);
				addPoint(5, dz, 0);
				addPoint(6, dz, dx);
				addPoint(7, 0, dx);
			}

			if (facing != 2) {
				Tessellator.instance.setNormal(0, 0, -1);
				addPoint(0, 0, 0);
				addPoint(4, dy, 0);
				addPoint(7, dy, dx);
				addPoint(3, 0, dx);
			}

			if (facing != 3) {
				Tessellator.instance.setNormal(0, 0, 1);
				addPoint(6, 0, 0);
				addPoint(5, dx, 0);
				addPoint(1, dx, dy);
				addPoint(2, 0, dy);
			}

			if (facing != 4) {
				Tessellator.instance.setNormal(-1, 0, 0);
				addPoint(5, 0, 0);
				addPoint(4, dz, 0);
				addPoint(0, dz, dy);
				addPoint(1, 0, dy);
			}

			if (facing != 5) {
				Tessellator.instance.setNormal(1, 0, 0);
				addPoint(2, 0, 0);
				addPoint(3, dz, 0);
				addPoint(7, dz, dy);
				addPoint(6, 0, dy);
			}
			Tessellator.instance.draw();
		}
		//RETURN TO MC DRAWING
		Tessellator.instance.startDrawingQuads();
		renderer.minecraftRB.renderEngine.bindTexture(TEXTURE_LOCATION);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Tessellator.instance.setColorOpaque_F(0.5F, 0.5F, 0.5F);
		renderer.minecraftRB.renderEngine.bindTexture(TextureMap.locationBlocksTexture/* blocks texture atlas*/);
	}
}