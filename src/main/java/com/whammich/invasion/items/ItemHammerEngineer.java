package com.whammich.invasion.items;

import invmod.common.nexus.TileEntityNexus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class ItemHammerEngineer extends Item {
    private static final int NEXUS_REPAIR_AMOUNT = 10;

    public ItemHammerEngineer(Item.Properties properties) {
        super(properties.pickaxe(ToolMaterial.IRON, 2.0F, -2.8F));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityNexus nexus) {
            if (nexus.addPowerLevel(NEXUS_REPAIR_AMOUNT)) {
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.7F, 1.2F);
                damageHammer(context.getItemInHand(), player, context.getHand());
                player.displayClientMessage(
                    Component.literal("Nexus repaired: +" + NEXUS_REPAIR_AMOUNT + " power"),
                    true
                );
            } else {
                player.displayClientMessage(Component.literal("Nexus repair failed."), true);
            }
            return InteractionResult.SUCCESS;
        }

        BlockPos placePos = pos.relative(context.getClickedFace());
        if (!level.isEmptyBlock(placePos)) {
            return InteractionResult.PASS;
        }

        ItemStack materialStack = ItemStack.EMPTY;
        BlockState placeState = null;

        if (player.isShiftKeyDown()) {
            Direction face = context.getClickedFace();
            if (face.getAxis().isHorizontal()) {
                ItemStack ladderStack = findInventoryItem(player, stack -> stack.is(Blocks.LADDER.asItem()));
                if (!ladderStack.isEmpty()) {
                    BlockState ladderState = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, face);
                    if (ladderState.canSurvive(level, placePos)) {
                        materialStack = ladderStack;
                        placeState = ladderState;
                    }
                }
            }
        }

        if (placeState == null) {
            ItemStack plankStack = findInventoryItem(player, stack -> stack.is(ItemTags.PLANKS));
            if (!plankStack.isEmpty()) {
                Block plankBlock = Block.byItem(plankStack.getItem());
                if (plankBlock != Blocks.AIR) {
                    materialStack = plankStack;
                    placeState = plankBlock.defaultBlockState();
                }
            }
        }

        if (placeState == null) {
            ItemStack cobbleStack = findInventoryItem(player, stack -> stack.is(Blocks.COBBLESTONE.asItem()));
            if (!cobbleStack.isEmpty()) {
                materialStack = cobbleStack;
                placeState = Blocks.COBBLESTONE.defaultBlockState();
            }
        }

        if (placeState == null) {
            player.displayClientMessage(Component.literal("No build materials available."), true);
            return InteractionResult.PASS;
        }

        if (!placeState.canSurvive(level, placePos)) {
            return InteractionResult.PASS;
        }

        level.setBlock(placePos, placeState, Block.UPDATE_ALL);
        playPlaceSound(level, placePos, placeState);
        if (!player.isCreative()) {
            materialStack.shrink(1);
            damageHammer(context.getItemInHand(), player, context.getHand());
        }
        return InteractionResult.SUCCESS;
    }

    private void damageHammer(ItemStack stack, Player player, net.minecraft.world.InteractionHand hand) {
        if (player.isCreative()) {
            return;
        }
        EquipmentSlot slot = hand == net.minecraft.world.InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, player, slot);
    }

    private ItemStack findInventoryItem(Player player, Predicate<ItemStack> matcher) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && matcher.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void playPlaceSound(Level level, BlockPos pos, BlockState state) {
        SoundType sound = state.getSoundType(level, pos, null);
        level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, sound.getVolume(), sound.getPitch());
    }
}
