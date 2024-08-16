package club.someoneice.mmmtweaks.mixin;

import club.someoneice.mmmtweaks.Config;
import club.someoneice.mmmtweaks.crystal_crafting.CraftingHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import project.studio.manametalmod.core.AutoCrystal;
import project.studio.manametalmod.tileentity.TileEntityCrystalZ;

import java.util.Random;

@Mixin(AutoCrystal.class)
public class AutoCrystalMixin {
    @Inject(method = "effectCraftV2", at = @At("HEAD"), remap = false, cancellable = true)
    public void blockActivated(TileEntityCrystalZ tile, int time, Random random, World world, CallbackInfo ci) {
        if (!Config.USE_NEW_CRAFTING_SYSTEM) {
            return;
        }

        CraftingHandler.checkRecipe(tile, time);
        ci.cancel();
    }
}
