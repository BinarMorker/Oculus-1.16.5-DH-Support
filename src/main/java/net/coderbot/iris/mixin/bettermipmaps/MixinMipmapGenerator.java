package net.coderbot.iris.mixin.bettermipmaps;

import com.mojang.blaze3d.platform.NativeImage;
import net.coderbot.iris.helpers.ColorSRGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.texture.MipmapGenerator;

/**
 * Implements a significantly enhanced mipmap downsampling filter.
 *
 * <p>This algorithm combines ideas from vanilla Minecraft -- using linear color spaces instead of sRGB for blending) --
 * with ideas from OptiFine -- using the alpha values for weighting in downsampling -- to produce a novel downsampling
 * algorithm for mipmapping that produces minimal visual artifacts.</p>
 *
 * <p>This implementation fixes a number of issues with other implementations:</p>
 *
 * <li>
 *     <ul>OptiFine blends in sRGB space, resulting in brightness losses.</ul>
 *     <ul>Vanilla applies gamma correction to alpha values, which has weird results when alpha values aren't the same.</ul>
 *     <ul>Vanilla computes a simple average of the 4 pixels, disregarding the relative alpha values of pixels. In
 *         cutout textures, this results in a lot of pixels with high alpha values and dark colors, causing visual
 *         artifacts.</ul>
 * </li>
 */
@Mixin(MipmapGenerator.class)
public class MixinMipmapGenerator {

	/**
	 * @author coderbot
	 * @reason replace the vanilla blending function with our improved function
	 */
	@Overwrite
	private static int alphaBlend(int one, int two, int three, int four, boolean checkAlpha) {
		// First blend horizontally, then blend vertically.
		//
		// This works well for the case where our change is the most impactful (grass side overlays)
		return weightedAverageColor(weightedAverageColor(one, two), weightedAverageColor(three, four));
	}

	@Unique
	private static int weightedAverageColor(int one, int two) {
		int alphaOne = NativeImage.getA(one);
		int alphaTwo = NativeImage.getA(two);

		// In the case where the alpha values of the same, we can get by with an unweighted average.
		if (alphaOne == alphaTwo) {
			return averageRgb(one, two, alphaOne);
		}

		// If one of our pixels is fully transparent, ignore it.
		// We just take the value of the other pixel as-is. To compensate for not changing the color value, we
		// divide the alpha value by 4 instead of 2.
		if (alphaOne == 0) {
			return (two & 0x00FFFFFF) | ((alphaTwo >> 2) << 24);
		}

		if (alphaTwo == 0) {
			return (one & 0x00FFFFFF) | ((alphaOne >> 2) << 24);
		}

		// Use the alpha values to compute relative weights of each color.
		float scale = 1.0f / (alphaOne + alphaTwo);
		float relativeWeightOne = alphaOne * scale;
		float relativeWeightTwo = alphaTwo * scale;

		// Convert the color components into linear space, then multiply the corresponding weight.
		float oneR = ColorSRGB.srgbToLinear(NativeImage.getR(one)) * relativeWeightOne;
		float oneG = ColorSRGB.srgbToLinear(NativeImage.getG(one)) * relativeWeightOne;
		float oneB = ColorSRGB.srgbToLinear(NativeImage.getB(one)) * relativeWeightOne;

		float twoR = ColorSRGB.srgbToLinear(NativeImage.getR(two)) * relativeWeightTwo;
		float twoG = ColorSRGB.srgbToLinear(NativeImage.getG(two)) * relativeWeightTwo;
		float twoB = ColorSRGB.srgbToLinear(NativeImage.getB(two)) * relativeWeightTwo;

		// Combine the color components of each color
		float linearR = oneR + twoR;
		float linearG = oneG + twoG;
		float linearB = oneB + twoB;

		// Take the average alpha of both alpha values
		int averageAlpha = (alphaOne + alphaTwo) >> 1;

		// Convert to sRGB and pack the colors back into an integer.
		return ColorSRGB.linearToSrgb(linearR, linearG, linearB, averageAlpha);
	}

	// Computes a non-weighted average of the two sRGB colors in linear space, avoiding brightness losses.
	@Unique
	private static int averageRgb(int a, int b, int alpha) {
		float ar = ColorSRGB.srgbToLinear(NativeImage.getR(a));
		float ag = ColorSRGB.srgbToLinear(NativeImage.getG(a));
		float ab = ColorSRGB.srgbToLinear(NativeImage.getB(a));

		float br = ColorSRGB.srgbToLinear(NativeImage.getR(b));
		float bg = ColorSRGB.srgbToLinear(NativeImage.getG(b));
		float bb = ColorSRGB.srgbToLinear(NativeImage.getB(b));

		return ColorSRGB.linearToSrgb((ar + br) * 0.5f, (ag + bg) * 0.5f, (ab + bb) * 0.5f, alpha);
	}
}
