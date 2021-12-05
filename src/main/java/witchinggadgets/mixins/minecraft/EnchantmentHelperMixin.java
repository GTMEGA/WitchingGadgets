package witchinggadgets.mixins.minecraft;

import lombok.val;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import witchinggadgets.common.WGContent;
import witchinggadgets.common.items.baubles.ItemMagicalBaubles;

import static baubles.api.BaublesApi.getBaubles;
import static witchinggadgets.common.WGConfig.coremod_allowEnchantModifications;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    private static int ringLuckBonus;

    @Shadow
    public static int getEnchantmentLevel(int p_77506_0_, ItemStack p_77506_1_) {
        return 0;
    }

    @Inject(method = {"getLootingModifier(Lnet/minecraft/entity/EntityLivingBase;)I",
            "getFortuneModifier(Lnet/minecraft/entity/EntityLivingBase;)I"},
            at = @At(value = "HEAD"),
            require = 1)
    private static void getFortruneOrLootingModifierHead(EntityLivingBase entityLivingBase,
                                                         CallbackInfoReturnable<Integer> cir) {
        if (coremod_allowEnchantModifications && entityLivingBase instanceof EntityPlayer)
            ringLuckBonus = playerLuck((EntityPlayer) entityLivingBase);
    }

    private static int playerLuck(EntityPlayer entityPlayer) {
        val baubles = getBaubles(entityPlayer);
        for (int i = 0; i < baubles.getSizeInventory(); i++) {
            val baubleItemStack = baubles.getStackInSlot(i);
            if (baubleItemStack == null)
                continue;
            if (!baubleItemStack.getItem().equals(WGContent.ItemMagicalBaubles))
                continue;
            if (!ItemMagicalBaubles.subNames[baubleItemStack.getItemDamage()].equals("ringLuck"))
                continue;

            return 2;
        }
        return 0;
    }

    @Redirect(method = {"getLootingModifier(Lnet/minecraft/entity/EntityLivingBase;)I",
            "getFortuneModifier(Lnet/minecraft/entity/EntityLivingBase;)I"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;getEnchantmentLevel " +
                            "(ILnet/minecraft/item/ItemStack;)I"),
            require = 1)
    private static int getFortruneOrLootingModifierEnchantmentLevelRedirect(int effectID, ItemStack itemStack) {
        val totalLuck = getEnchantmentLevel(effectID, itemStack) + ringLuckBonus;
        ringLuckBonus = 0;
        return totalLuck;
    }
}
