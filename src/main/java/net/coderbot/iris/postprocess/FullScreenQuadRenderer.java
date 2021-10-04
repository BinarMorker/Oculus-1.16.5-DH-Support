package net.coderbot.iris.postprocess;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20C;

/**
 * Renders a full-screen textured quad to the screen. Used in composite / deferred rendering.
 */
public class FullScreenQuadRenderer {
	private final int quadBuffer;

	public static final FullScreenQuadRenderer INSTANCE = new FullScreenQuadRenderer();

	private FullScreenQuadRenderer() {
		this.quadBuffer = createQuad();
	}

	public void render() {
		begin();

		renderQuad();

		end();
	}

	public void begin() {
		RenderSystem.disableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager._glBindBuffer(GL20C.GL_ARRAY_BUFFER, quadBuffer);
		DefaultVertexFormat.POSITION_TEX.setupBufferState(0L);
	}

	public void renderQuad() {
		GlStateManager._drawArrays(GL20C.GL_TRIANGLE_STRIP, 0, 4);
	}

	public static void end() {
		DefaultVertexFormat.POSITION_TEX.clearBufferState();
		GlStateManager._glBindBuffer(GL20C.GL_ARRAY_BUFFER, 0);

		RenderSystem.enableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		RenderSystem.popMatrix();
	}

	/**
	 * Creates and uploads a vertex buffer containing a single full-screen quad
	 */
	private static int createQuad() {
		float[] vertices = new float[] {
			// Vertex 0: Top right corner
			1.0F, 1.0F, 0.0F,
			1.0F, 1.0F,
			// Vertex 1: Top left corner
			-1.0F, 1.0F, 0.0F,
			0.0F, 1.0F,
			// Vertex 2: Bottom right corner
			1.0F, -1.0F, 0.0F,
			1.0F, 0.0F,
			// Vertex 3: Bottom left corner
			-1.0F, -1.0F, 0.0F,
			0.0F, 0.0F
		};

		int buffer = GlStateManager._glGenBuffers();

		GlStateManager._glBindBuffer(GL20C.GL_ARRAY_BUFFER, buffer);
		GL20C.glBufferData(GL20C.GL_ARRAY_BUFFER, vertices, GL20C.GL_STATIC_DRAW);
		GlStateManager._glBindBuffer(GL20C.GL_ARRAY_BUFFER, 0);

		return buffer;
	}
}
