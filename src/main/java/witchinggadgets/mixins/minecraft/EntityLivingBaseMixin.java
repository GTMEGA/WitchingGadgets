package witchinggadgets.mixins.minecraft;

import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import witchinggadgets.common.items.armor.ItemPrimordialArmor;

import java.lang.reflect.Field;

import static java.lang.Math.*;
import static thaumcraft.common.config.Config.*;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin {
    private static boolean isInDev = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    private static Field potionAmplifierField;
    private static Field potionDurationField;

    @Shadow
    public abstract ItemStack getEquipmentInSlot(int p_71124_1_);

    @SneakyThrows
    @Inject(method = "onNewPotionEffect(Lnet/minecraft/potion/PotionEffect;)V", at = @At(value = "RETURN"), require = 1)
    private void onNewPotionEffectReturn(PotionEffect potionEffect, CallbackInfo ci) {
        if (thiz().worldObj.isRemote) return;

        val potionID = potionEffect.getPotionID();
        if (!(potionID == potionVisExhaustID || potionID == potionThaumarhiaID || potionID == potionUnHungerID ||
                potionID == potionBlurredID || potionID == potionSunScornedID || potionID == potionInfVisExhaustID ||
                potionID == potionDeathGazeID))
            return;

        var ordo = 0;
        for (int i = 1; i <= 4; i++) {
            val armor = getEquipmentInSlot(i);
            if (armor == null)
                continue;

            val item = armor.getItem();
            if (item == null)
                continue;

            if (!(item instanceof ItemPrimordialArmor))
                continue;

            if (((ItemPrimordialArmor) item).getAbility(armor) == 5)
                ordo++;
        }
        if (ordo <= 0)
            return;

        setupReflection();

        val potionAmplifier = potionAmplifierField.getInt(potionEffect);
        if (potionAmplifier > 0)
            potionAmplifierField.setInt(potionEffect, max(0, potionAmplifier - ordo));

        val potionDuration = potionDurationField.getInt(potionEffect);
        if (potionDuration > 0)
            potionAmplifierField.setInt(potionEffect, potionDuration / (ordo + 1));
    }

    private EntityLivingBase thiz() {
        return (EntityLivingBase) (Object) this;
    }

    @SneakyThrows
    private static void setupReflection() {
        if (potionAmplifierField == null) {
            potionAmplifierField = PotionEffect.class.getDeclaredField(isInDev ? "amplifier" : "field_76461_c");
            if (!potionAmplifierField.isAccessible())
                potionAmplifierField.setAccessible(true);
        }

        if (potionDurationField == null) {
            potionDurationField = PotionEffect.class.getDeclaredField(isInDev ? "duration" : "field_76460_b");
            if (!potionDurationField.isAccessible())
                potionDurationField.setAccessible(true);
        }
    }
}
