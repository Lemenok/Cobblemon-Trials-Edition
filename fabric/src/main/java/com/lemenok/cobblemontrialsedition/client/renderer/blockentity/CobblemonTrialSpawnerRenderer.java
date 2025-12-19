package com.lemenok.cobblemontrialsedition.client.renderer.blockentity;

import com.lemenok.cobblemontrialsedition.block.custom.CobblemonTrialSpawnerBlock;
import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawner;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CobblemonTrialSpawnerRenderer implements BlockEntityRenderer<CobblemonTrialSpawnerEntity> {
    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public CobblemonTrialSpawnerRenderer(BlockEntityRendererProvider.Context arg) {
        this.itemRenderer = arg.getItemRenderer();
    }

    @Override
    public void render(CobblemonTrialSpawnerEntity cobblemonTrialSpawnerEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        Level level = cobblemonTrialSpawnerEntity.getLevel();
        if (level != null) {
            CobblemonTrialSpawner cobblemonTrialSpawner = cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner();
            CobblemonTrialSpawnerData cobblemonTrialSpawnerData = cobblemonTrialSpawner.getData();
            ItemStack itemStack = cobblemonTrialSpawnerData.getOrCreateDisplayEntity(cobblemonTrialSpawnerEntity
                    .getBlockState().getValue(CobblemonTrialSpawnerBlock.OMINOUS), cobblemonTrialSpawner, level, cobblemonTrialSpawner.getState());
            if (itemStack != null) {
                if (!itemStack.isEmpty()) {
                    this.random.setSeed(ItemEntityRenderer.getSeedForItemStack(itemStack));
                    renderItemInside(f, level, poseStack, multiBufferSource, i, itemStack, this.itemRenderer,
                            (float) cobblemonTrialSpawnerData.getOSpin(), (float) cobblemonTrialSpawnerData.getSpin(), this.random);
                }
            }
        }
    }

    public static void renderItemInside(float f, Level level, PoseStack poseStack, MultiBufferSource multiBufferSource,
                                        int i, ItemStack itemStack, ItemRenderer itemRenderer, float g, float h,
                                        RandomSource randomSource) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.4F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(f, g, h)));
        ItemEntityRenderer.renderMultipleFromCount(itemRenderer, poseStack, multiBufferSource, i, itemStack, randomSource, level);
        poseStack.popPose();
    }
}
