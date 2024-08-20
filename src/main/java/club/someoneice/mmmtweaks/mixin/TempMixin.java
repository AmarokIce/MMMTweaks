package club.someoneice.mmmtweaks.mixin;

import club.someoneice.pineapplepsychic.util.Util;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Util.class)
public class TempMixin {
    @Inject(method = "itemStackEquals", at = @At("HEAD"), cancellable = true)
    private static void itemStackEquals(ItemStack A, ItemStack B, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0, argsOnly = true) ItemStack itemStack)  {
        boolean flag =
                (A == null && B == null) ||
                        (A != null && B != null)
                                && A.getItem() == B.getItem()
                                && ((A.getItemDamage() == 32767 || B.getItemDamage() == 32767)
                                || A.getItemDamage() == B.getItemDamage());
        cir.setReturnValue(flag);
    }
}
