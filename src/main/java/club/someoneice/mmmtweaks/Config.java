package club.someoneice.mmmtweaks;

import club.someoneice.pineapplepsychic.config.ConfigBeanV2;

public class Config extends ConfigBeanV2 {
    public static boolean USE_NEW_CRAFTING_SYSTEM = true;
    public static boolean SHOULD_DEPTH_SCAN_RECIPE = false;

    public static boolean USE_NEW_SORTING_CABINET = true;


    public Config() {
        super("mmmtweaks");
        init();
    }

    public void init() {
        USE_NEW_CRAFTING_SYSTEM = this.getBoolean("UseNewCraftingSystem", USE_NEW_CRAFTING_SYSTEM);
        SHOULD_DEPTH_SCAN_RECIPE = this.getBoolean("ShouldDeepScanRecipes", SHOULD_DEPTH_SCAN_RECIPE);


    }
}
