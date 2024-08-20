package club.someoneice.mmmtweaks.mixin;

import club.someoneice.mmmtweaks.Config;
import club.someoneice.mmmtweaks.sorting_cabinet.SortingHandler;
import net.minecraft.world.ChunkPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import project.studio.manametalmod.tileentity.TileEntityLogisticsCore;

@Mixin(TileEntityLogisticsCore.class)
public class SortingCabinetMixin {
    @Unique int time = 0;

    @Inject(method = "func_145845_h", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (++time >= 20 * 5) {
            time = 0;
        } else {
            ci.cancel();
        }

        TileEntityLogisticsCore thiz = (TileEntityLogisticsCore) (Object) this;
        if (thiz.getWorldObj().isRemote) {
            return;
        }

        if (!Config.USE_NEW_CRAFTING_SYSTEM) {
            return;
        }

        SortingHandler.scanInventory(thiz, thiz.getWorldObj(), new ChunkPosition(thiz.xCoord, thiz.yCoord, thiz.zCoord));
        ci.cancel();
    }
}
