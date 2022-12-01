package ru.iamdvz.modelika.utils

import com.ticxo.modelengine.api.ModelEngineAPI
import com.ticxo.modelengine.api.model.vfx.VFX
import org.bukkit.Color
import org.bukkit.entity.LivingEntity

class VfxUtils {
    companion object {
        @JvmStatic
        fun buildVfx(entity: LivingEntity, model: String, bone: String,
                     enchant: Boolean, color: Color, rDistance: Int, autoTick: Boolean): VFX {
            val vfx = ModelEngineAPI.createVFX(entity)
            vfx.useModel(ModelEngineAPI.getBlueprint(model).modelId, bone)
            vfx.isEnchant = enchant
            vfx.color = color
            vfx.rangeManager.renderDistance = rDistance
            vfx.isAutoTick = autoTick
            return vfx
        }
    }
}