package club.someoneice.mmmtweaks;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModMain.MODID, name = ModMain.NAME, useMetadata = true)
public class ModMain {
    public static final String MODID = "mmmtweaks";
    public static final String NAME = "MMMTweaks";
    public static final String VERSION = "0.0.1";

    public static final Logger LOG = LogManager.getLogger(NAME);

    @Mod.Instance(MODID)
    public static ModMain INSTANCE;
    public static Config CONFIG_BEAN;
    public boolean obfuscated;

    public static String codecItemStack(ItemStack item) {
        return Item.getIdFromItem(item.getItem()) + ":" + item.getItemDamage();
    }

    public static ItemStack decodecItemStack(String idPair) {
        String[] pair = idPair.split(":");
        Item item = Item.getItemById(Integer.parseInt(pair[0]));
        return new ItemStack(item, 1, Integer.parseInt(pair[1]));
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CONFIG_BEAN = new Config();
        INSTANCE = this;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void commonInit(FMLInitializationEvent event) {
    }

    /* *===* Utils *===*  */

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        this.obfuscated = !(Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    }

    @Mod.EventHandler
    public void onServerStartingEvent(FMLServerStartingEvent event) {
    }
}
