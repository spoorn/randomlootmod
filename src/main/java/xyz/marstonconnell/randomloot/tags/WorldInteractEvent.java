package xyz.marstonconnell.randomloot.tags;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class WorldInteractEvent {
	public abstract void effect(ItemStack stack, World worldIn,
			LivingEntity entityLiving, BlockState state, BlockPos pos);
}