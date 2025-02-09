package mod.azure.hwg.item.weapons;

import com.google.common.collect.Lists;
import mod.azure.azurelib.common.api.common.animatable.GeoItem;
import mod.azure.azurelib.common.internal.client.RenderProvider;
import mod.azure.azurelib.common.internal.common.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.common.internal.common.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.common.internal.common.core.animation.AnimatableManager;
import mod.azure.azurelib.common.internal.common.core.animation.Animation;
import mod.azure.azurelib.common.internal.common.core.animation.AnimationController;
import mod.azure.azurelib.common.internal.common.core.animation.RawAnimation;
import mod.azure.azurelib.common.internal.common.core.object.PlayState;
import mod.azure.azurelib.common.internal.common.util.AzureLibUtil;
import mod.azure.hwg.client.render.GunRender;
import mod.azure.hwg.entity.projectiles.GrenadeEntity;
import mod.azure.hwg.item.ammo.GrenadeEmpItem;
import mod.azure.hwg.item.enums.GunTypeEnum;
import mod.azure.hwg.util.Helper;
import mod.azure.hwg.util.registry.HWGItems;
import mod.azure.hwg.util.registry.HWGSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class GrenadeLauncherItem extends HWGGunLoadedBase implements GeoItem {

    public static final Predicate<ItemStack> EMP = stack -> stack.getItem() == HWGItems.G_EMP;
    public static final Predicate<ItemStack> GRENADES = EMP.or(stack -> stack.getItem() == HWGItems.G_FRAG).or(stack -> stack.getItem() == HWGItems.G_NAPALM).or(stack -> stack.getItem() == HWGItems.G_SMOKE).or(stack -> stack.getItem() == HWGItems.G_STUN);
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    private boolean charged = false;
    private boolean loaded = false;

    public GrenadeLauncherItem() {
        super(new Item.Properties().stacksTo(1).durability(31));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    private static void shoot(Level world, LivingEntity shooter, InteractionHand hand, ItemStack stack, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated) {
        if (!world.isClientSide) {
            var nade = getGrenadeEntity(world, shooter, projectile);
            var vec3d = shooter.getUpVector(1.0F);
            var quaternionf = new Quaternionf().setAngleAxis(simulated * ((float) Math.PI / 180), vec3d.x, vec3d.y, vec3d.z);
            var vec3d2 = shooter.getViewVector(1.0f);
            var vector3f = vec3d2.toVector3f().rotate(quaternionf);
            vector3f.rotate(quaternionf);
            ((AbstractArrow) nade).shoot(vector3f.x, vector3f.y, vector3f.z, speed, divergence);

            stack.hurtAndBreak(1, shooter, p -> p.broadcastBreakEvent(shooter.getUsedItemHand()));
            world.addFreshEntity(nade);

            world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), HWGSounds.GLAUNCHERFIRE, SoundSource.PLAYERS, 1.0F, 0.9F);
        }
    }

    @NotNull
    private static GrenadeEntity getGrenadeEntity(Level world, LivingEntity shooter, ItemStack projectile) {
        var emp = projectile.getItem() == HWGItems.G_EMP;
        var frag = projectile.getItem() == HWGItems.G_FRAG;
        var napalm = projectile.getItem() == HWGItems.G_NAPALM;
        var stun = projectile.getItem() == HWGItems.G_STUN;
        var nade = new GrenadeEntity(world, projectile, shooter, shooter.getX(), shooter.getEyeY() - 0.15000000596046448D, shooter.getZ(), true);
        nade.setState(0);
        if (emp) {
            nade.setVariant(1);
        } else if (frag) {
            nade.setVariant(2);
        } else if (napalm) {
            nade.setVariant(3);
        } else if (stun) {
            nade.setVariant(5);
        } else {
            nade.setVariant(4);
        }
        return nade;
    }

    private static boolean loadProjectiles(LivingEntity shooter, ItemStack projectile) {
        var i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, projectile);
        var j = i == 0 ? 1 : 3;
        var bl = shooter instanceof Player player && player.getAbilities().instabuild;
        var itemStack = shooter.getProjectile(projectile);
        var itemStack2 = itemStack.copy();

        for (int k = 0; k < j; ++k) {
            if (k > 0) itemStack = itemStack2.copy();

            if (itemStack.isEmpty() && bl) {
                itemStack = new ItemStack(HWGItems.G_SMOKE);
                itemStack2 = itemStack.copy();
            }

            if (!loadProjectile(shooter, projectile, itemStack, k > 0, bl)) return false;
        }
        return true;
    }

    private static boolean loadProjectile(LivingEntity shooter, ItemStack crossbow, ItemStack projectile, boolean simulated, boolean creative) {
        if (projectile.isEmpty()) return false;
        else {
            var bl = creative && projectile.getItem() instanceof GrenadeEmpItem;
            ItemStack itemStack2;
            if (!bl && !creative && !simulated) {
                itemStack2 = projectile.split(1);
                if (projectile.isEmpty() && shooter instanceof Player player)
                    player.getInventory().removeItem(projectile);
            } else itemStack2 = projectile.copy();

            putProjectile(crossbow, itemStack2);
            return true;
        }
    }

    public static boolean isCharged(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("Charged");
    }

    public static void setCharged(ItemStack stack, boolean charged) {
        stack.getOrCreateTag().putBoolean("Charged", charged);
    }

    private static void putProjectile(ItemStack crossbow, ItemStack projectile) {
        var nbt = crossbow.getOrCreateTag();
        ListTag list;
        if (nbt.contains("ChargedProjectiles", 9)) list = nbt.getList("ChargedProjectiles", 10);
        else list = new ListTag();

        var nbtnew = new CompoundTag();
        projectile.save(nbtnew);
        list.add(nbtnew);
        nbt.put("ChargedProjectiles", list);
    }

    private static List<ItemStack> getProjectiles(ItemStack crossbow) {
        List<ItemStack> list = Lists.newArrayList();
        var nbt = crossbow.getTag();
        if (nbt != null && nbt.contains("ChargedProjectiles", 9)) {
            var list2 = nbt.getList("ChargedProjectiles", 10);
            if (list2 != null) for (var i = 0; i < list2.size(); ++i) {
                var nbt2 = list2.getCompound(i);
                list.add(ItemStack.of(nbt2));
            }
        }
        return list;
    }

    private static void clearProjectiles(ItemStack crossbow) {
        var tag = crossbow.getTag();
        if (tag != null) {
            var list = tag.getList("ChargedProjectiles", 9);
            list.clear();
            tag.put("ChargedProjectiles", list);
        }
    }

    public static boolean hasProjectile(ItemStack crossbow, Item projectile) {
        return getProjectiles(crossbow).stream().anyMatch(s -> s.getItem() == projectile);
    }

    public static void shootAll(Level world, LivingEntity entity, InteractionHand hand, ItemStack stack, float speed, float divergence) {
        var list = getProjectiles(stack);
        var fs = getSoundPitches(entity.level().random);

        for (int i = 0; i < list.size(); ++i) {
            var itemStack = list.get(i);
            var bl = entity instanceof Player player && player.getAbilities().instabuild;
            if (!itemStack.isEmpty()) {
                if (i == 0) shoot(world, entity, hand, stack, itemStack, fs[i], bl, speed, divergence, 0.0F);
                else if (i == 1) shoot(world, entity, hand, stack, itemStack, fs[i], bl, speed, divergence, -10.0F);
                else if (i == 2) shoot(world, entity, hand, stack, itemStack, fs[i], bl, speed, divergence, 10.0F);
            }
        }
        postShoot(world, entity, stack);
    }

    private static float[] getSoundPitches(RandomSource random) {
        var bl = random.nextBoolean();
        return new float[]{1.0F, getSoundPitch(bl, random), getSoundPitch(!bl, random)};
    }

    private static float getSoundPitch(boolean flag, RandomSource random) {
        var f = flag ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f;
    }

    private static void postShoot(Level world, LivingEntity entity, ItemStack stack) {
        clearProjectiles(stack);
    }

    public static int getPullTime(ItemStack stack) {
        var i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack);
        return 25 - 5 * i;
    }

    private static float getSpeed(ItemStack stack) {
        return stack.getItem() == Items.CROSSBOW && hasProjectile(stack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", event -> PlayState.CONTINUE).triggerableAnim("firing", RawAnimation.begin().then("firing", Animation.LoopType.PLAY_ONCE)).triggerableAnim("loading", RawAnimation.begin().then("loading", Animation.LoopType.PLAY_ONCE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return GRENADES;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return GRENADES;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return Tiers.IRON.getRepairIngredient().test(ingredient) || super.isValidRepairItem(stack, ingredient);
    }

    @Override
    public int getDefaultProjectileRange() {
        return 16;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        var itemStack = user.getItemInHand(hand);
        if (isCharged(itemStack) && itemStack.getDamageValue() < (itemStack.getMaxDamage() - 1) && !user.getCooldowns().isOnCooldown(this)) {
            shootAll(world, user, hand, itemStack, getSpeed(itemStack), 1.0F);
            user.getCooldowns().addCooldown(this, 25);
            setCharged(itemStack, false);
            if (!world.isClientSide)
                triggerAnim(user, GeoItem.getOrAssignId(itemStack, (ServerLevel) world), "controller", "firing");
            var isInsideWaterBlock = user.level().isWaterAt(user.blockPosition());
            Helper.spawnLightSource(user, isInsideWaterBlock);
            return InteractionResultHolder.consume(itemStack);
        } else if (!user.getProjectile(itemStack).isEmpty()) {
            if (!isCharged(itemStack)) {
                this.charged = false;
                this.loaded = false;
                user.startUsingItem(hand);
            }
            return InteractionResultHolder.consume(itemStack);
        } else return InteractionResultHolder.fail(itemStack);
    }

    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!isCharged(stack) && loadProjectiles(user, stack)) {
            setCharged(stack, true);
            var soundCategory = user instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            world.playSound(null, user.getX(), user.getY(), user.getZ(), HWGSounds.GLAUNCHERRELOAD, soundCategory, 0.5F, 1.0F);
            if (!world.isClientSide)
                triggerAnim(user, GeoItem.getOrAssignId(stack, (ServerLevel) world), "controller", "loading");
            ((Player) user).getCooldowns().addCooldown(this, 15);
        }
    }

    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClientSide) {
            var f = (float) (stack.getUseDuration() - remainingUseTicks) / (float) getPullTime(stack);
            if (f < 0.2F) {
                this.charged = false;
                this.loaded = false;
            }

            if (f >= 0.2F && !this.charged) this.charged = true;

            if (f >= 0.5F && !this.loaded) this.loaded = true;
        }
    }

    public int getUseDuration(ItemStack stack) {
        return getPullTime(stack) + 3000;
    }

    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        var list = getProjectiles(stack);
        if (isCharged(stack) && !list.isEmpty()) {
            var itemStack = list.get(0);
            tooltip.add((Component.translatable("Ammo")).append(" ").append(itemStack.getDisplayName()));
            if (context.isAdvanced() && itemStack.getItem() == GRENADES) {
                List<Component> list2 = Lists.newArrayList();
                HWGItems.G_EMP.appendHoverText(itemStack, world, list2, context);
                if (!list2.isEmpty()) {
                    for (int i = 0; i < list2.size(); ++i)
                        list2.set(i, (Component.literal("  ")).append(list2.get(i)).withStyle(ChatFormatting.GRAY));
                    tooltip.addAll(list2);
                }
            }

        }
        tooltip.add(Component.translatable("hwg.ammo.reloadgrenades").withStyle(ChatFormatting.ITALIC));
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private final GunRender<GrenadeLauncherItem> renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null)
                    return new GunRender<GrenadeLauncherItem>("grenade_launcher", GunTypeEnum.NADELAUNCHER);
                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

}
