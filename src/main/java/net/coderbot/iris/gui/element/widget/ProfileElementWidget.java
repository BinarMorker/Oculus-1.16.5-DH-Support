package net.coderbot.iris.gui.element.widget;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.OptionSet;
import net.coderbot.iris.shaderpack.option.Profile;
import net.coderbot.iris.shaderpack.option.ProfileSet;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuProfileElement;
import net.coderbot.iris.shaderpack.option.values.OptionValues;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ProfileElementWidget extends BaseOptionElementWidget<OptionMenuProfileElement> {
	private static final MutableComponent PROFILE_LABEL = new TranslatableComponent("options.iris.profile");
	private static final MutableComponent PROFILE_CUSTOM = new TranslatableComponent("options.iris.profile.custom").withStyle(ChatFormatting.YELLOW);

	private Profile next;
	private Profile previous;
	private Component profileLabel;

	public ProfileElementWidget(OptionMenuProfileElement element) {
		super(element);
	}

	@Override
	public void init(ShaderPackScreen screen, NavigationController navigation) {
		super.init(screen, navigation);
		this.setLabel(PROFILE_LABEL);

		ProfileSet profiles = this.element.profiles;
		OptionSet options = this.element.options;
		OptionValues pendingValues = this.element.getPendingOptionValues();

		ProfileSet.ProfileResult result = profiles.scan(options, pendingValues);

		this.next = result.next;
		this.previous = result.previous;
		Optional<String> profileName = result.current.map(p -> p.name);

		this.profileLabel = profileName.map(name -> GuiUtil.translateOrDefault(new TextComponent(name), "profile." + name)).orElse(PROFILE_CUSTOM);
	}

	@Override
	public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		this.updateRenderParams(width, width - (Minecraft.getInstance().font.width(PROFILE_LABEL) + 16));

		this.renderOptionWithValue(poseStack, x, y, width, height, hovered);
	}

	@Override
	protected Component createValueLabel() {
		return this.profileLabel;
	}

	@Override
	public Optional<Component> getCommentTitle() {
		return Optional.of(PROFILE_LABEL);
	}

	@Override
	public String getCommentKey() {
		return "profile.comment";
	}

	@Override
	public boolean applyNextValue() {
		if (this.next == null) {
			return false;
		}

		Iris.queueShaderPackOptionsFromProfile(this.next);

		return true;
	}

	@Override
	public boolean applyPreviousValue() {
		if (this.previous == null) {
			return false;
		}

		Iris.queueShaderPackOptionsFromProfile(this.previous);

		return true;
	}

	@Override
	public boolean applyOriginalValue() {
		return false; // Resetting options is the way to return to the "default profile"
	}

	@Override
	public boolean isValueModified() {
		return false;
	}
}
