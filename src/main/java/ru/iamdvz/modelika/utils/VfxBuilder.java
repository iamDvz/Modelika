package ru.iamdvz.modelika.utils;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.vfx.VFX;
import org.bukkit.Color;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.EulerAngle;

public class VfxBuilder {

    public static VFX buildVfx(LivingEntity entity, String model, String bone, boolean enchant, Color color, int rDistance, boolean autoTick) {
        VFX vfx = ModelEngineAPI.createVFX(entity);
        vfx.useModel(ModelEngineAPI.getBlueprint(model).getModelId(), bone);
        vfx.setEnchant(enchant);
        vfx.setColor(color);
        vfx.getRangeManager().setRenderDistance(rDistance);
        vfx.setAutoTick(autoTick);
        return vfx;
    }
}
