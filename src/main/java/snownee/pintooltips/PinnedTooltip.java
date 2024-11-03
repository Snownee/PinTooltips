package snownee.pintooltips;

import static snownee.pintooltips.util.SimpleTooltipPositioner.TOOLTIP_PADDING;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.duck.PTContainerScreen;
import snownee.pintooltips.mixin.interact.ClientTextTooltipAccess;
import snownee.pintooltips.mixin.pin.GuiGraphicsAccess;
import snownee.pintooltips.util.DummyHoveredSlot;
import snownee.pintooltips.util.SimpleTooltipPositioner;

public final class PinnedTooltip {
	private final Vector2d position;
	private final Vector2i size;
	private final List<Component> content;
	private final List<ClientTooltipComponent> components;
	private final Vector2d offset;
	private final @Nullable DummyHoveredSlot hoveredSlot;
	private final Map<Rect2i, ClientTooltipComponent> linesPosition;

	public PinnedTooltip(
			Vector2d position,
			Vector2i size,
			List<Component> content,
			List<ClientTooltipComponent> components,
			Vector2d offset,
			@Nullable DummyHoveredSlot hoveredSlot) {
		this.position = position;
		this.size = size;
		this.content = content;
		this.components = components;
		this.offset = offset;
		this.hoveredSlot = hoveredSlot;
		this.linesPosition = new Reference2ObjectOpenHashMap<>();
	}

	public PinnedTooltip(
			Vector2d position,
			List<Component> content,
			List<ClientTooltipComponent> components,
			int screenWidth,
			int screenHeight,
			Font font,
			ItemStack itemStack
	) {
		this(
				position,
				new Vector2i(),
				content,
				components,
				new Vector2d(),
				itemStack.isEmpty() ? null : new DummyHoveredSlot(itemStack.copy()));
		offset.set(getPositionerOffset(screenWidth, screenHeight, position.x(), position.y()));
		updateSize(screenWidth, screenHeight, font);
	}

	public boolean isHovering(double mouseX, double mouseY) {
		var x = mouseX + offset.x();
		var y = mouseY + offset.y();
		return x >= position.x() - TOOLTIP_PADDING && x <= position.x() + size.x() + TOOLTIP_PADDING
				&& y >= position.y() - TOOLTIP_PADDING && y <= position.y() + size.y() + TOOLTIP_PADDING;
	}

	public void updateSize(int screenWidth, int screenHeight, Font font) {
		var width = 0;
		var height = 0;
		linesPosition.clear();
		for (var component : components) {
			var componentWidth = component.getWidth(font);
			var componentHeight = component.getHeight();
			linesPosition.put(new Rect2i(0, height, componentWidth, componentHeight), component);
			width = Math.max(width, componentWidth);
			height += componentHeight;
		}
		if (width != size.x() && height != size.y()) {
			size.set(width, height);
			offset.set(getPositionerOffset(screenWidth, screenHeight, position.x, position.y));
		}
	}

	public void render(Screen screen, Font font, GuiGraphics context, int mouseX, int mouseY, float tickDelta) {
		context.pose().pushPose();
		updateSize(screen.width, screen.height, font);
		var inContainer = hoveredSlot() != null && screen instanceof PTContainerScreen;
		if (inContainer) {
			((PTContainerScreen) screen).pin_tooltips$setDummyHoveredSlot(hoveredSlot());
		}

		((GuiGraphicsAccess) context).callRenderTooltipInternal(
				font,
				components(),
				(int) position().x(),
				(int) position().y(),
				SimpleTooltipPositioner.INSTANCE);

		if (isHovering(mouseX, mouseY)) {
			var relativeX = (int) (mouseX - position().x() + offset.x);
			var relativeY = (int) (mouseY - position().y() + offset.y);
			var line = linesPosition.keySet().stream().filter(rect -> rect.contains(relativeX, relativeY)).findFirst().orElse(null);
			var component = linesPosition.get(line);
			if (component instanceof ClientTextTooltip textTooltip) {
				var style = font.getSplitter().componentStyleAtWidth(((ClientTextTooltipAccess) textTooltip).getText(), relativeX);
				context.pose().translate(0,0,1);
				context.renderComponentHoverEffect(font, style, mouseX, mouseY);
			}
		}

		if (inContainer) {
			((PTContainerScreen) screen).pin_tooltips$dropDummyHoveredSlot();
		}
		context.pose().popPose();
	}

	public Vector2d getPositionerOffset(int screenWidth, int screenHeight, double x, double y) {
		var offset = SimpleTooltipPositioner.INSTANCE.positionTooltip(screenWidth, screenHeight, (int) x, (int) y, size.x(), size.y());
		var deltaX = x - offset.x();
		var deltaY = y - offset.y();
		return new Vector2d(deltaX, deltaY);
	}

	public void setPosition(int screenWidth, int screenHeight, double x, double y) {
		offset.set(getPositionerOffset(screenWidth, screenHeight, x, y));
		position.set(x, y);
	}

	public Vector2d position() {return position;}

	public Vector2ic size() {return size;}

	public List<ClientTooltipComponent> components() {return components;}

	public @Nullable DummyHoveredSlot hoveredSlot() {return hoveredSlot;}
}
