package ru.iamdvz.modelika.targeted;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.ColorUtil;
import com.nisovin.magicspells.util.MagicConfig;
import com.ticxo.modelengine.api.model.vfx.VFX;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import ru.iamdvz.modelika.Modelika;
import ru.iamdvz.modelika.utils.VectorUtils;
import ru.iamdvz.modelika.utils.VfxUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MegVfxSpell extends TargetedSpell implements TargetedLocationSpell {
    private String modelId;
    private String boneId;
    private Color vfxColor;
    private final String equationX;
    private final String equationY;
    private final String equationZ;
    private final String entitySpellName;
    private final String groundSpellName;
    private int entitySpellDelay;
    private int renderDistance;
    private int vfxDuration;
    private int tickSpeed;
    private int hitRadius;
    private int groundHitRadius;
    private boolean rotationNewOrigin;
    private boolean spawnOnCaster;
    private boolean orientPitch;
    private boolean vfxEnchant;
    private boolean stopOnHitEntity;
    private boolean stopOnHitGround;
    private Vector rotationFromOrigin;
    private Vector originRotation;
    private Vector rOffset;

    // ~ non-config ~ //
    private Subspell entitySpell;
    private Subspell groundSpell;

    public MegVfxSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        modelId = getConfigString("model-id", null);
        boneId = getConfigString("bone-id", null);
        vfxColor = ColorUtil.getColorFromHexString(getConfigString("vfx-color", "ffffff"));
        equationX = getConfigString("equation-x", "0");
        equationY = getConfigString("equation-y", "0");
        equationZ = getConfigString("equation-z", "0");
        entitySpellName = getConfigString("spell-on-hit-entity", "");
        groundSpellName = getConfigString("spell-on-hit-ground", "");

        entitySpellDelay = getConfigInt("delay-to-spell-on-hit-entity-again", 10);
        renderDistance = getConfigInt("render-distance", 32);
        vfxDuration = getConfigInt("vfx-duration", 20);
        tickSpeed = getConfigInt("tick-speed", 3);
        hitRadius = getConfigInt("hit-radius", 1);
        groundHitRadius = getConfigInt("ground-hit-radius", hitRadius);

        rotationNewOrigin = getConfigBoolean("rotation-from-origin-set-new-origin", true);
        spawnOnCaster = getConfigBoolean("spawn-on-caster", false);
        orientPitch = getConfigBoolean("orient-pitch", true);
        vfxEnchant = getConfigBoolean("vfx-enchant", false);
        stopOnHitEntity = getConfigBoolean("stop-on-hit-entity", false);
        stopOnHitGround = getConfigBoolean("stop-on-hit-ground", true);

        rotationFromOrigin = getConfigVector("rotation-from-origin", "0,0,0");
        originRotation = getConfigVector("origin-rotation", "0,0,0");
        rOffset = getConfigVector("relative-offset", "0,0,0");
    }


    @Override
    public void initialize() {
        super.initialize();
        entitySpell = new Subspell(entitySpellName);
        if (!entitySpell.process()) {
            MagicSpells.error("Meg '" + internalName + "' has an invalid spell-on-hit-entity defined!");
            entitySpell = null;
        }

        groundSpell = new Subspell(groundSpellName);
        if (!groundSpell.process()) {
            MagicSpells.error("Meg '" + internalName + "' has an invalid spell-on-hit-ground defined!");
            groundSpell = null;
        }
    }

    @Override
    public Spell.PostCastAction castSpell(LivingEntity caster, Spell.SpellCastState state, float power, String[] args) {
        vfxSpawn(caster.getLocation(), (Player) caster, power);
        return Spell.PostCastAction.HANDLE_NORMALLY;
    }

    @Override
    public boolean castAtLocation(LivingEntity caster, Location target, float power) {
        vfxSpawn(target, (Player) caster, power);
        return false;
    }

    @Override
    public boolean castAtLocation(Location target, float power) {
        vfxSpawn(target, null, power);
        return false;

    }

    boolean vfxSpawn(Location target, Player player, float power) {
        Entity entity = player;
        Set<UUID> entityTargets = new HashSet<>();
        Expression exprX = new ExpressionBuilder(equationX)
                .variable("t")
                .build()
                .setVariable("t", 0);
        Expression exprY = new ExpressionBuilder(equationY)
                .variable("t")
                .build()
                .setVariable("t", 0);
        Expression exprZ = new ExpressionBuilder(equationZ)
                .variable("t")
                .build()
                .setVariable("t", 0);
        final int[] ticker = {0};

        if (!spawnOnCaster) {
            entity = target.getWorld().spawnEntity(target.add(
                rOffset.getX()*Math.cos(Math.toRadians(target.getYaw()+90)) + rOffset.getZ()*Math.cos(Math.toRadians(target.getYaw())),
                rOffset.getY(),
                rOffset.getX()*Math.sin(Math.toRadians(target.getYaw()+90)) + rOffset.getZ()*Math.sin(Math.toRadians(target.getYaw()))), EntityType.ARMOR_STAND);
            entity.setSilent(true);
            entity.setGravity(false);
            entity.setInvulnerable(true);
            ((ArmorStand) entity).setVisible(false);
            ((ArmorStand) entity).addDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.LEGS, EquipmentSlot.OFF_HAND);
            entity.addScoreboardTag("MODELIKA_MOB");
            entity.addScoreboardTag("MS_ARMOR_STAND");
        }
        playSpellEffects(EffectPosition.TARGET, entity.getLocation());
        VFX vfx = VfxUtils.buildVfx((LivingEntity) entity, modelId, boneId, vfxEnchant, vfxColor, renderDistance, false);
        vfx.create();
        vfx.rotate(new EulerAngle(Math.toRadians(originRotation.getX()), Math.toRadians(originRotation.getY()), Math.toRadians(originRotation.getZ())), rotationNewOrigin);
        Entity finalEntity = entity;
        BukkitTask task = new BukkitRunnable() {
            public void run() {
                playSpellEffects(EffectPosition.SPECIAL, vfx.getPosition().toLocation(vfx.getBase().getWorld()));
                vfx.rotate(new EulerAngle(Math.toRadians(rotationFromOrigin.getX()), Math.toRadians(rotationFromOrigin.getY()), Math.toRadians(rotationFromOrigin.getZ())), false);
                if (orientPitch) {
                    vfx.getPosition().add(VectorUtils.rotateVector(new Vector(exprX.evaluate(), exprY.evaluate(), exprZ.evaluate()), target.getYaw(), target.getPitch()));
                }
                if (!orientPitch) {
                    vfx.getPosition().add(VectorUtils.rotateVector(new Vector(exprX.evaluate(), exprY.evaluate(), exprZ.evaluate()), target.getYaw(), 0));
                }
                exprX.setVariable("t", ticker[0]);
                exprY.setVariable("t", ticker[0]);
                exprZ.setVariable("t", ticker[0]);
                ticker[0]++;
                vfx.update();

                //!! spell-on-hit-entity
                if (entitySpell.isTargetedEntitySpell() && entitySpell != null) {
                    for (Entity entityTarget : vfx.getPosition().toLocation(vfx.getBase().getWorld()).getNearbyEntities(hitRadius, hitRadius, hitRadius)) {
                        if (!entityTargets.contains(entityTarget.getUniqueId()) && !entityTarget.getType().equals(EntityType.ARMOR_STAND) && !entityTarget.getType().equals(EntityType.EXPERIENCE_ORB) && !entityTarget.equals(player)) {
                            entitySpell.castAtEntity(player, (LivingEntity) entityTarget, power);
                            entityTargets.add(entityTarget.getUniqueId());
                            if (stopOnHitEntity) {
                                cancel();
                                stopVfx(vfx, finalEntity, null, this);
                            }
                            if (entitySpellDelay > 0) {
                                Bukkit.getScheduler().runTaskLater(Modelika.getInstance(), () -> {
                                    entityTargets.remove(entityTarget.getUniqueId());
                                }, entitySpellDelay);
                            }
                        }
                    }
                }
                //!! stop-on-hit-ground
                if (stopOnHitGround || groundSpell != null) {
                    Location vfxLocation = vfx.getPosition().toLocation(vfx.getBase().getWorld()).add(0,0.5,0);
                    Set<Material> t = MagicSpells.getTransparentBlocks();
                    if (groundHitRadius == 1 && !t.contains(vfxLocation.getBlock().getType())) {
                        if (groundSpell.isTargetedLocationSpell()) {groundSpell.castAtLocation(player, vfxLocation, power);}
                        cancel();
                        stopVfx(vfx, finalEntity, null, this);
                    }
                    for (Block b : BlockUtils.getNearbyBlocks(vfxLocation, groundHitRadius, groundHitRadius)) {
                        if (!t.contains(b.getType())) {
                            if (groundSpell.isTargetedLocationSpell()) {groundSpell.castAtLocation(player, vfxLocation, power);}
                            cancel();
                            stopVfx(vfx, finalEntity, null, this);
                            break;
                        }
                    }
                }
            }
        }.runTaskTimer(Modelika.getInstance(), 0, tickSpeed);

        Bukkit.getScheduler().runTaskLater(Modelika.getInstance(), () -> {
            stopVfx(vfx, finalEntity, task, null);
        }, vfxDuration);
        return true;
    }

    void stopVfx(VFX vfx, Entity entity, BukkitTask task, BukkitRunnable runnable) {
        if (!vfx.getBase().isDead()) {vfx.destroy();}
        if (entity.getType().equals(EntityType.ARMOR_STAND)) {entity.remove();}
        if (task != null && !task.isCancelled()) {task.cancel();}
        if (runnable != null && !runnable.isCancelled()) {runnable.cancel();}

    }
}
