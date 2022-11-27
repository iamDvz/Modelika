package ru.iamdvz.modelika.targeted;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.ColorUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.state.ModelState;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.nms.entity.fake.BoneRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import ru.iamdvz.modelika.Modelika;

import java.util.List;

public class MobVfx extends TargetedSpell implements TargetedLocationSpell {
    private int taskTeleport;
    private int taskHurt;
    private final String modelName;
    private final List<String> partsNamesColors;
    private final Color modelColor;
    private final String animationName;
    private final double animationDuration;
    private final Vector relativeOffset;
    private final boolean teleportToCaster;
    private final boolean orientYaw;
    private final boolean glowing;

    public MobVfx(MagicConfig config, String spellName) {

        super(config, spellName);

        modelName = getConfigString("model-name", null);
        modelColor = ColorUtil.getColorFromHexString(getConfigString("model-color", null));
        partsNamesColors = getConfigStringList("parts-names-colors:", null);
        animationName = getConfigString("animation-name", "death");
        animationDuration = getConfigDouble("animation-duration", ModelEngineAPI.getBlueprint(modelName.toUpperCase()).getAnimations().get(animationName).getLength()*20);
        relativeOffset = getConfigVector("relative-offset", "0,0,0");
        teleportToCaster = getConfigBoolean("teleport-to-caster", false);
        orientYaw = getConfigBoolean("orient-yaw-when-teleport", false);
        glowing = getConfigBoolean("glowing", false);
        if (ModelEngineAPI.getBlueprint(modelName) == null) {
            MagicSpells.error("VFX '" + internalName + "' has an invalid model-name defined!");
        }
        if (ModelEngineAPI.getBlueprint(modelName).getAnimations().get(animationName) == null) {
            MagicSpells.error("VFX '" + internalName + "' has an invalid animation-name defined!");
        }
    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        animatedModelSpawn(caster.getLocation(), (Player) caster);
        playSpellEffects(EffectPosition.CASTER, caster);
        return PostCastAction.HANDLE_NORMALLY;
    }

    @Override
    public boolean castAtLocation(LivingEntity caster, Location target, float power) {
        animatedModelSpawn(target, (Player) caster);
        return false;
    }

    // не используется в тмуспе
    @Override
    public boolean castAtLocation(Location target, float power) {
        animatedModelSpawn(target, null);
        return false;
    }

    private boolean animatedModelSpawn(Location target, Player player) {
        if (ModelEngineAPI.getBlueprint(modelName) == null) {
            return false;
        }
        if (ModelEngineAPI.getBlueprint(modelName).getAnimations().get(animationName) == null) {
            return false;
        }

        Entity mob = target.getWorld().spawnEntity(target.add(
                relativeOffset.getX()*Math.cos(Math.toRadians(target.getYaw()+90)) + relativeOffset.getZ()*Math.cos(Math.toRadians(target.getYaw())),
                relativeOffset.getY(),
                relativeOffset.getX()*Math.sin(Math.toRadians(target.getYaw()+90)) + relativeOffset.getZ()*Math.sin(Math.toRadians(target.getYaw()))), EntityType.ARMOR_STAND);
        playSpellEffects(EffectPosition.TARGET, mob.getLocation());
        mob.setSilent(true);
        mob.setGravity(false);
        mob.setInvulnerable(true);
        ((ArmorStand) mob).setVisible(false);
        ((ArmorStand) mob).addDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.LEGS, EquipmentSlot.OFF_HAND);
        mob.addScoreboardTag("MODELIKA_MOB");
        mob.addScoreboardTag("MS_ARMOR_STAND");

        ActiveModel model = ModelEngineAPI.createActiveModel(modelName);
        model.getRendererHandler().setGlowing(glowing);
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(mob);
        modeledEntity.addModel(model, false);
        modeledEntity.setState(ModelState.valueOf(animationName.toUpperCase()));
        for (BoneRenderer bone : ModelEngineAPI.getModeledEntity(mob.getUniqueId()).getModel(modelName).getRendererHandler().getFakeEntity().values()) {
            bone.setColor(modelColor);
            bone.setGlowing(glowing);
        }
        model.setHurt(true);
        taskHurt = Bukkit.getScheduler().scheduleSyncRepeatingTask(Modelika.getInstance(), () -> { modeledEntity.hurt(); }, 0, 7);

        if (teleportToCaster) {
            Location playerLocation = player.getLocation();
            int orientYawInt = orientYaw ? 1 : 0;
            taskTeleport = Bukkit.getScheduler().scheduleSyncRepeatingTask(Modelika.getInstance(), () -> {
                mob.teleport(player.getLocation().add(relativeOffset.getX()*Math.cos(Math.toRadians((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt)+90 + orientYawInt*player.getLocation().getYaw())) + relativeOffset.getZ()*Math.cos(Math.toRadians((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt) + orientYawInt*player.getLocation().getYaw())),
                        relativeOffset.getY(),
                        relativeOffset.getX()*Math.sin(Math.toRadians((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt)+90 + orientYawInt*player.getLocation().getYaw())) + relativeOffset.getZ()*Math.sin(Math.toRadians((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt) + orientYawInt*player.getLocation().getYaw()))));
            }, 1, 1);
        }
        Bukkit.getScheduler().runTaskLater(Modelika.getInstance(), () -> {
            modeledEntity.destroy();
            mob.remove();
            Bukkit.getScheduler().cancelTask(taskHurt);
            try {
                if (Bukkit.getScheduler().getActiveWorkers().get(taskTeleport).getTaskId() == taskTeleport) {
                    Bukkit.getScheduler().cancelTask(taskTeleport);
                }
            } catch (Exception e) {}
        }, Math.round(animationDuration));
        return true;
    }
}