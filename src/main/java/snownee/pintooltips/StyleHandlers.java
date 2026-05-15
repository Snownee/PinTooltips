package snownee.pintooltips;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStackTemplate;
import snownee.pintooltips.util.ComponentDecorator;

public class StyleHandlers {
	private static final Map<Class<?>, StyleHandler<?>> MAP = Maps.newConcurrentMap();

	static {
		register(
				ClientBundleTooltip.class, new StyleHandler<>() {
					@Override
					public Style handle(PinnedTooltip tooltip, ClientBundleTooltip component, Font font, int x, int y, int w) {
						ClickEvent clickEvent = null;
						HoverEvent hoverEvent = null;
						List<ItemStackTemplate> shownItems = component.getShownItems(component.contents.getNumberOfItemsToShow());
						int index = getShownItemIndexAt(component, x, y, w, shownItems);
						if (index < 0 || index >= shownItems.size()) {
							index = -1;
						} else {
							hoverEvent = new HoverEvent.ShowItem(shownItems.get(index));
						}
						Slot slot = tooltip.itemSlot();
						if (PinTooltipsConfig.bundleInteraction && slot != null) {
							CompoundTag tag = new CompoundTag();
							tag.putInt("slot", slot.index);
							tag.putInt("index", index);
							clickEvent = new ClickEvent.Custom(ComponentDecorator.BUNDLE_ACTION, Optional.of(tag));
						}
						return Style.EMPTY.withHoverEvent(hoverEvent).withClickEvent(clickEvent);
					}

					private static int getShownItemIndexAt(
							ClientBundleTooltip component,
							int x,
							int y,
							int w,
							List<ItemStackTemplate> shownItems) {
						boolean isOverflowing = component.contents.size() > 12;
						int xStartPos = ClientBundleTooltip.getContentXOffset(w) + 96;
						int yStartPos = component.gridSizeY() * 24;
						int slotNumber = 1;

						for (int rowNumber = 1; rowNumber <= component.gridSizeY(); rowNumber++) {
							for (int columnNumber = 1; columnNumber <= 4; columnNumber++) {
								// 计算当前格子的左上角坐标
								int drawX = xStartPos - columnNumber * 24;
								int drawY = yStartPos - rowNumber * 24;

								// 检查鼠标坐标是否在当前 24x24 的格子范围内
								if (x >= drawX && x < drawX + 24 && y >= drawY && y < drawY + 24) {
									// 如果是“更多物品”计数文本位置，则不视为物品索引
									if (ClientBundleTooltip.shouldRenderSurplusText(isOverflowing, columnNumber, rowNumber)) {
										return -1;
									}
									// 如果是渲染物品的槽位
									if (ClientBundleTooltip.shouldRenderItemSlot(shownItems, slotNumber)) {
										return shownItems.size() - slotNumber;
									}
								}

								if (!ClientBundleTooltip.shouldRenderSurplusText(isOverflowing, columnNumber, rowNumber) &&
										ClientBundleTooltip.shouldRenderItemSlot(shownItems, slotNumber)) {
									++slotNumber;

								}
							}
						}
						return -1;
					}
				});
	}

	public static Optional<StyleHandler<?>> get(ClientTooltipComponent component) {
		return Optional.ofNullable(MAP.get(component.getClass()));
	}

	public static <T extends ClientTooltipComponent> void register(Class<? extends T> clazz, StyleHandler<T> handler) {
		MAP.put(clazz, handler);
	}
}
