package net.iamaprogrammer.reimaginedmenus.gui.widgets;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TabButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Environment(EnvType.CLIENT)
public class OptionsTabWidget extends AbstractParentElement implements Drawable, Element, Selectable {
    private static final Text USAGE_NARRATION_TEXT = Text.translatable("narration.tab_navigation.usage");
    private final GridWidget grid;
    private final TabManager tabManager;
    private final ImmutableList<Tab> tabs;
    private final ImmutableList<TabButtonWidget> tabButtons;


    OptionsTabWidget(TabManager tabManager, Iterable<Tab> tabs, int posX, int posY) {
        this.tabManager = tabManager;
        this.tabs = ImmutableList.copyOf(tabs);
        this.grid = new GridWidget(0, 0);
        this.grid.setPosition(posX, posY);
        this.grid.getMainPositioner().alignHorizontalCenter();
        ImmutableList.Builder<TabButtonWidget> builder = ImmutableList.builder();
        int i = 0;
        Iterator var6 = tabs.iterator();

        while(var6.hasNext()) {
            Tab tab = (Tab)var6.next();
            builder.add((TabButtonWidget)this.grid.add(new TabButtonWidget(tabManager, tab, 0, 24), 0, i++));
        }
        this.tabButtons = builder.build();
    }

    public static net.iamaprogrammer.reimaginedmenus.gui.widgets.OptionsTabWidget.Builder builder(TabManager tabManager, int posX, int posY) {
        return new net.iamaprogrammer.reimaginedmenus.gui.widgets.OptionsTabWidget.Builder(tabManager, posX, posY);
    }

    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (this.getFocused() != null) {
            this.getFocused().setFocused(focused);
        }
    }

    public void setFocused(@Nullable Element focused) {
        super.setFocused(focused);
        if (focused instanceof TabButtonWidget tabButtonWidget) {
            this.tabManager.setCurrentTab(tabButtonWidget.getTab(), true);
        }
    }

    public @Nullable GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        if (!this.isFocused()) {
            TabButtonWidget tabButtonWidget = this.getCurrentTabButton();
            if (tabButtonWidget != null) {
                return GuiNavigationPath.of(this, GuiNavigationPath.of(tabButtonWidget));
            }
        }

        return navigation instanceof GuiNavigation.Tab ? null : super.getNavigationPath(navigation);
    }

    public List<? extends Element> children() {
        return this.tabButtons;
    }

    public Selectable.SelectionType getType() {
        return (Selectable.SelectionType)this.tabButtons.stream().map(ClickableWidget::getType).max(Comparator.naturalOrder()).orElse(SelectionType.NONE);
    }

    public void appendNarrations(NarrationMessageBuilder builder) {
        Optional<TabButtonWidget> optional = this.tabButtons.stream().filter(ClickableWidget::isHovered).findFirst().or(() -> {
            return Optional.ofNullable(this.getCurrentTabButton());
        });
        optional.ifPresent((button) -> {
            this.appendNarrations(builder.nextMessage(), button);
            button.appendNarrations(builder);
        });
        if (this.isFocused()) {
            builder.put(NarrationPart.USAGE, USAGE_NARRATION_TEXT);
        }

    }

    protected void appendNarrations(NarrationMessageBuilder builder, TabButtonWidget button) {
        if (this.tabs.size() > 1) {
            int i = this.tabButtons.indexOf(button);
            if (i != -1) {
                builder.put(NarrationPart.POSITION, Text.translatable("narrator.position.tab", new Object[]{i + 1, this.tabs.size()}));
            }
        }

    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {}

    public ScreenRect getNavigationFocus() {
        return this.grid.getNavigationFocus();
    }

    public void init() {}

    public void selectTab(int index, boolean clickSound) {
        if (this.isFocused()) {
            this.setFocused((Element)this.tabButtons.get(index));
        } else {
            this.tabManager.setCurrentTab((Tab)this.tabs.get(index), clickSound);
        }
    }

    public boolean trySwitchTabsWithKey(int keyCode) {
        if (Screen.hasControlDown()) {
            int i = this.getTabForKey(keyCode);
            if (i != -1) {
                this.selectTab(MathHelper.clamp(i, 0, this.tabs.size() - 1), true);
                return true;
            }
        }
        return false;
    }

    private int getTabForKey(int keyCode) {
        if (keyCode >= 49 && keyCode <= 57) {
            return keyCode - 49;
        } else {
            if (keyCode == 258) {
                int i = this.getCurrentTabIndex();
                if (i != -1) {
                    int j = Screen.hasShiftDown() ? i - 1 : i + 1;
                    return Math.floorMod(j, this.tabs.size());
                }
            }
            return -1;
        }
    }

    private int getCurrentTabIndex() {
        Tab tab = this.tabManager.getCurrentTab();
        int i = this.tabs.indexOf(tab);
        return i != -1 ? i : -1;
    }

    private @Nullable TabButtonWidget getCurrentTabButton() {
        int i = this.getCurrentTabIndex();
        return i != -1 ? (TabButtonWidget)this.tabButtons.get(i) : null;
    }

    @Environment(EnvType.CLIENT)
    public static class Builder {
        private final TabManager tabManager;
        private final List<Tab> tabs = new ArrayList<>();
        private final int posX;
        private final int posY;

        Builder(TabManager tabManager, int x, int y) {
            this.tabManager = tabManager;
            this.posX = x;
            this.posY = y;
        }

        public net.iamaprogrammer.reimaginedmenus.gui.widgets.OptionsTabWidget.Builder tabs(Tab... tabs) {
            Collections.addAll(this.tabs, tabs);
            return this;
        }

        public net.iamaprogrammer.reimaginedmenus.gui.widgets.OptionsTabWidget build() {
            return new net.iamaprogrammer.reimaginedmenus.gui.widgets.OptionsTabWidget(this.tabManager, this.tabs, this.posX, this.posY);
        }
    }
}
