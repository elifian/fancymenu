package de.keksuccino.fancymenu.menu.fancy.item.items.playerentity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.keksuccino.fancymenu.menu.fancy.item.items.playerentity.render.layers.PlayerEntityCapeLayer;
import de.keksuccino.fancymenu.menu.fancy.item.items.playerentity.render.layers.PlayerEntityRenderLayer;
import de.keksuccino.fancymenu.menu.fancy.item.items.playerentity.render.layers.PlayerEntityShoulderParrotLayer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@SuppressWarnings("all")
@OnlyIn(Dist.CLIENT)
public class PlayerEntityItemRenderer extends PlayerRenderer {

    private static final EntityModelSet ENTITY_MODEL_SET = Minecraft.getInstance().getEntityModels();
    private static final EntityRendererProvider.Context RENDER_CONTEXT = new EntityRendererProvider.Context(Minecraft.getInstance().getEntityRenderDispatcher(), Minecraft.getInstance().getItemRenderer(), Minecraft.getInstance().getBlockRenderer(), Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer(), Minecraft.getInstance().getResourceManager(), ENTITY_MODEL_SET, Minecraft.getInstance().font);

    public final PlayerEntityProperties properties;
    public final PlayerEntityModel playerModel;

    public PlayerEntityItemRenderer(boolean slim) {
        super(RENDER_CONTEXT, slim);
        this.properties = new PlayerEntityProperties(slim);
        this.playerModel = new PlayerEntityModel(RENDER_CONTEXT.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim, this.properties);
        this.addLayer(new PlayerEntityShoulderParrotLayer(this, RENDER_CONTEXT.getModelSet(), this.properties));
        this.addLayer(new PlayerEntityCapeLayer(this, this.properties));
    }

    public void renderPlayerEntityItem(double d11, double d12, double d13, float f11, float f12, PoseStack pose, MultiBufferSource bufferSource, int i11) {
        try {
            Vec3 vec3 = this.getRenderOffset(null, f12);
            double d2 = d11 + vec3.x();
            double d3 = d12 + vec3.y();
            double d0 = d13 + vec3.z();
            pose.pushPose();
            pose.translate(d2, d3, d0);
            this.render(f11, f12, pose, bufferSource, i11);
            pose.translate(-vec3.x(), -vec3.y(), -vec3.z());
            pose.popPose();
        } catch (Exception ex) {
            CrashReport crashreport = CrashReport.forThrowable(ex, "FancyMenu: Rendering player entity item");
            CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
            crashreportcategory1.setDetail("Rotation", f11);
            crashreportcategory1.setDetail("Delta", f12);
            throw new ReportedException(crashreport);
        }
    }

    protected void render(float f11, float f12, PoseStack graphics, MultiBufferSource bufferSource, int i11) {
        this.setModelProperties();
        this.innerRender(f11, f12, graphics, bufferSource, i11);
    }

    protected void innerRender(float f11, float f12, PoseStack graphics, MultiBufferSource bufferSource, int i11) {

        graphics.pushPose();

        boolean shouldSit = this.properties.shouldSit;
        this.playerModel.riding = shouldSit;
        this.playerModel.young = this.properties.isBaby;
        float f = Mth.rotLerp(f12, this.properties.yBodyRotO, this.properties.yBodyRot);
        float f1 = Mth.rotLerp(f12, this.properties.yHeadRotO, this.properties.yHeadRot);
        float f2 = f1 - f;

        float f6 = Mth.lerp(f12, this.properties.xRotO, this.properties.xRot);

        if (this.properties.hasPose(Pose.SLEEPING)) {
            Direction direction = this.properties.getBedOrientation();
            if (direction != null) {
                float f4 = this.properties.getEyeHeight(Pose.STANDING) - 0.1F;
                graphics.translate((float)(-direction.getStepX()) * f4, 0.0F, (float)(-direction.getStepZ()) * f4);
            }
        }

        float f7 = f12;
        this.setupRotations(graphics, f7, f, f12);
        graphics.scale(-1.0F, -1.0F, 1.0F);
        this.scale(graphics, f12);
        graphics.translate(0.0F, -1.501F, 0.0F);
        float f8 = 0.0F;
        float f5 = 0.0F;
        if (!shouldSit) {
            f8 = Mth.lerp(f12, this.properties.animationSpeedOld, this.properties.animationSpeed);
            f5 = this.properties.animationPosition - this.properties.animationSpeed * (1.0F - f12);
            if (this.properties.isBaby) {
                f5 *= 3.0F;
            }
            if (f8 > 1.0F) {
                f8 = 1.0F;
            }
        }

        this.playerModel.setupAnimWithoutEntity(f5, f8, f7, f2, f6);
        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = !this.properties.invisible;
        boolean flag1 = false;
        boolean glowing = this.properties.glowing;
        RenderType rendertype = this.getRenderType(null, flag, flag1, glowing);
        if (rendertype != null) {
            VertexConsumer vertexconsumer = bufferSource.getBuffer(rendertype);
            int i = OverlayTexture.pack(OverlayTexture.u(this.getWhiteOverlayProgress(null, f12)), OverlayTexture.v(false));
            this.playerModel.renderToBuffer(graphics, vertexconsumer, i11, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
        }

        if (!this.properties.isSpectator()) {
            for(RenderLayer renderlayer : this.layers) {
                if (renderlayer instanceof PlayerEntityRenderLayer) {
                    renderlayer.render(graphics, bufferSource, i11, null, f5, f8, f12, f7, f2, f6);
                }
            }
        }

        graphics.popPose();

        if (this.properties.showDisplayName) {
            this.renderNameTag(null, this.properties.displayName, graphics, bufferSource, i11);
        }

    }

    protected void scale(PoseStack graphics, float f11) {
        float f = 0.9375F;
        graphics.scale(0.9375F, 0.9375F, 0.9375F);
    }

    private void setModelProperties() {
        PlayerEntityModel playermodel = this.playerModel;
        if (this.properties.isSpectator()) {
            playermodel.setAllVisible(false);
            playermodel.head.visible = true;
            playermodel.hat.visible = true;
        } else {
            playermodel.setAllVisible(true);
            playermodel.hat.visible = this.properties.isModelPartShown(PlayerModelPart.HAT);
            playermodel.jacket.visible = this.properties.isModelPartShown(PlayerModelPart.JACKET);
            playermodel.leftPants.visible = this.properties.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
            playermodel.rightPants.visible = this.properties.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
            playermodel.leftSleeve.visible = this.properties.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
            playermodel.rightSleeve.visible = this.properties.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
            playermodel.crouching = this.properties.isCrouching();
            HumanoidModel.ArmPose humanoidmodel$armpose = getArmPose(this.properties, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose humanoidmodel$armpose1 = getArmPose(this.properties, InteractionHand.OFF_HAND);
            if (humanoidmodel$armpose.isTwoHanded()) {
                humanoidmodel$armpose1 = this.properties.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
            }

            if (this.properties.getMainArm() == HumanoidArm.RIGHT) {
                playermodel.rightArmPose = humanoidmodel$armpose;
                playermodel.leftArmPose = humanoidmodel$armpose1;
            } else {
                playermodel.rightArmPose = humanoidmodel$armpose1;
                playermodel.leftArmPose = humanoidmodel$armpose;
            }
        }
    }

    private static HumanoidModel.ArmPose getArmPose(PlayerEntityProperties props, InteractionHand interactionHand) {
        ItemStack itemstack = props.getItemInHand(interactionHand);
        if (itemstack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (props.getUsedItemHand() == interactionHand && props.getUseItemRemainingTicks() > 0) {
                UseAnim useanim = itemstack.getUseAnimation();
                if (useanim == UseAnim.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }
                if (useanim == UseAnim.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }
                if (useanim == UseAnim.SPEAR) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }
                if (useanim == UseAnim.CROSSBOW && interactionHand == props.getUsedItemHand()) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }
                if (useanim == UseAnim.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }
                if (useanim == UseAnim.TOOT_HORN) {
                    return HumanoidModel.ArmPose.TOOT_HORN;
                }
            } else if (itemstack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(itemstack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
            return HumanoidModel.ArmPose.ITEM;
        }
    }

    @Override
    public Vec3 getRenderOffset(@Nullable AbstractClientPlayer entity, float f11) {
        return this.properties.isCrouching() ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
    }

    @Nullable
    protected RenderType getRenderType(@Nullable AbstractClientPlayer entity, boolean visible, boolean isVisibleToPlayer, boolean glowing) {
        ResourceLocation resourcelocation = this.getTextureLocation(entity);
        if (isVisibleToPlayer) {
            return RenderType.itemEntityTranslucentCull(resourcelocation);
        } else if (visible) {
            return this.playerModel.renderType(resourcelocation);
        } else {
            return glowing ? RenderType.outline(resourcelocation) : null;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(@Nullable AbstractClientPlayer entity) {
        return this.properties.getSkinTextureLocation();
    }

    @Override
    protected void renderNameTag(@Nullable AbstractClientPlayer entity, Component content, PoseStack graphics, MultiBufferSource bufferSource, int p_114502_) {
        boolean flag = !this.properties.isCrouching();
        float f = this.properties.getDimensions().height + 0.5F;
        int i = 0;
        graphics.pushPose();
        graphics.translate(0.0F, f, 0.0F);
        graphics.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f graphics4f = graphics.last().pose();
        float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int)(f1 * 255.0F) << 24;
        Font font = this.getFont();
        float f2 = (float)(-font.width(content) / 2);
        
        font.drawInBatch(content, f2, (float)i, 553648127, false, graphics4f, bufferSource, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, p_114502_);
        if (flag) {
            font.drawInBatch(content, f2, (float)i, -1, false, graphics4f, bufferSource, Font.DisplayMode.NORMAL, 0, p_114502_);
        }
        
        graphics.popPose();
    }

    protected void setupRotations(PoseStack graphics, float f11, float f12, float f13) {
        if (!this.properties.hasPose(Pose.SLEEPING)) {
            graphics.mulPose(Axis.YP.rotationDegrees(180.0F - f12));
        }
    }

}
