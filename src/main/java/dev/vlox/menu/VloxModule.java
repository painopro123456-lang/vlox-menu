package dev.vlox.menu;

public final class VloxModule {
    private final String name;
    private final String category;
    private final String icon;
    private boolean enabled;

    public VloxModule(String name, String category, String icon) {
        this.name = name;
        this.category = category;
        this.icon = icon;
    }

    public String name() {
        return name;
    }

    public String category() {
        return category;
    }

    public String icon() {
        return icon;
    }

    public boolean enabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }
}
