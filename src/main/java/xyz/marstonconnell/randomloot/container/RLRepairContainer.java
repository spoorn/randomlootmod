package xyz.marstonconnell.randomloot.container;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.AbstractRepairContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.SmithingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.TextFormatting;
import xyz.marstonconnell.randomloot.RandomLootMod;
import xyz.marstonconnell.randomloot.init.ItemFactory;
import xyz.marstonconnell.randomloot.init.RLItems;
import xyz.marstonconnell.randomloot.tags.BasicTag;
import xyz.marstonconnell.randomloot.tags.TagHelper;
import xyz.marstonconnell.randomloot.tools.BaseTool;
import xyz.marstonconnell.randomloot.tools.IRLTool;
import xyz.marstonconnell.randomloot.tools.RLBowItem;
import xyz.marstonconnell.randomloot.utils.Registration;

public class RLRepairContainer extends Container {

	protected final IInventory craftResult = new CraftResultInventory();
	protected final IInventory slots = new Inventory(3) {
		/**
		 * For tile entities, ensures the chunk containing the tile entity is saved to
		 * disk later - the game won't think it hasn't changed and skip it.
		 */
		public void markDirty() {
			super.markDirty();
			RLRepairContainer.this.onCraftMatrixChanged(this);
		}
	};

	protected final IWorldPosCallable worldPos;
	protected final PlayerEntity player;

	public RLRepairContainer(int p_i231590_1_, PlayerInventory p_i231590_2_) {
		this(Registration.EDITOR_CONTAINER.get(), p_i231590_1_, p_i231590_2_, IWorldPosCallable.DUMMY);
	}

	public RLRepairContainer(@Nullable ContainerType<?> containerType, int id, PlayerInventory playerInv,
			IWorldPosCallable worldPos) {
		super(containerType, id);
		this.worldPos = worldPos;
		this.player = playerInv.player;
		this.addSlot(new Slot(this.slots, 0, 27, 47));
		this.addSlot(new Slot(this.slots, 1, 76, 47));
		this.addSlot(new Slot(this.slots, 2, 8, 47) {
			public boolean isItemValid(ItemStack stack) {
				Item i = stack.getItem();

				return i.equals(RLItems.best_shard);

			}
		});
		this.addSlot(new Slot(this.craftResult, 3, 134, 47) {
			/**
			 * Check if the stack is allowed to be placed in this slot, used for armor slots
			 * as well as furnace fuel.
			 */
			public boolean isItemValid(ItemStack stack) {
				return false;
			}

			/**
			 * Return whether this slot's stack can be taken from this slot.
			 */
			public boolean canTakeStack(PlayerEntity playerIn) {
				return RLRepairContainer.this.isRecipe(playerIn);
			}

			public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {


				return RLRepairContainer.this.craftOutput(thePlayer, stack);
			}
		});

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(playerInv, k, 8 + k * 18, 142));
		}

	}

	private void shrinkSlot(int slot) {
		ItemStack itemstack = this.slots.getStackInSlot(slot);
		itemstack.shrink(1);
		this.slots.setInventorySlotContents(slot, itemstack);
	}

	protected boolean isRecipe(PlayerEntity player) {
		return ((slots.getStackInSlot(0).getItem() instanceof IRLTool)
				&& slots.getStackInSlot(1).getItem() == RLItems.TRAIT_HOLDER && slots.getStackInSlot(2).isEmpty())
				|| (slots.getStackInSlot(0).getItem() instanceof IRLTool
						&& slots.getStackInSlot(2).getItem().equals(RLItems.best_shard)
						&& slots.getStackInSlot(1).isEmpty());
	}
	
	@Override
	protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
		if(stack.getItem() instanceof IRLTool) {
			BaseTool.setLore(stack);

		}
		return super.mergeItemStack(stack, startIndex, endIndex, reverseDirection);
		
	}

	protected ItemStack craftOutput(PlayerEntity player, ItemStack stack) {
		this.shrinkSlot(0);
		this.shrinkSlot(1);
		this.shrinkSlot(2);
		
		
		ItemStack itemstack = this.getSlot(3).getStack();
		System.out.println("Crafting: " + stack.getDisplayName());
		
		
		
		
		this.worldPos.consume((p_234653_0_, p_234653_1_) -> {
			p_234653_0_.playEvent(1044, p_234653_1_, 0);
		});
		
		if(stack.getItem() instanceof IRLTool) {
			BaseTool.setLore(stack);

		}
		

		return stack;
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		super.onCraftMatrixChanged(inventoryIn);
		if (inventoryIn == this.slots) {
			this.updateRepairOutput();
		}

	}

	/**
	 * called when the Anvil Input Slot changes, calculates the new result and puts
	 * it in the output slot
	 */
	public void updateRepairOutput() {
		if (this.isRecipe(this.player)) {
			ItemStack out = slots.getStackInSlot(0).copy();

			ItemStack mod = slots.getStackInSlot(1);

			if (!mod.isEmpty()) {

				List<BasicTag> tags = TagHelper.getAllTags(mod);
				for (BasicTag tag : tags) {
					if (!TagHelper.checkForTag(out, tag)) {
						TagHelper.addTag(out, tag.name);
					}
				}

				BaseTool.setLore(out);
			}else {
				ItemStack edit = slots.getStackInSlot(2);

				if(edit.getItem().equals(RLItems.best_shard)) {
					int oldXp = BaseTool.getXP(out);
					BaseTool.changeXP(out, BaseTool.getMaxXP(out));
					BaseTool.changeXP(out, oldXp);
					
					
					CompoundNBT nbt;
					if (out.hasTag()) {
						nbt = out.getTag();
					} else {
						nbt = new CompoundNBT();
					}

					ListNBT lore = new ListNBT();

					
					lore.add(StringNBT.valueOf("{\"text\":\""+ TextFormatting.DARK_PURPLE +"Upgraded stats hidden.\"}"));
					CompoundNBT display = nbt.getCompound("display");

					display.put("Lore", lore);

					nbt.put("display", display);
					out.setTag(nbt);

				}
			}

			craftResult.setInventorySlotContents(0, out);
		} else {
			craftResult.setInventorySlotContents(0, new ItemStack(Items.AIR));
		}

	}

	/**
	 * Called when the container is closed.
	 */
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		this.worldPos.consume((p_234647_2_, p_234647_3_) -> {
			this.clearContainer(playerIn, p_234647_2_, this.slots);
		});
	}

	/**
	 * Determines whether supplied player can use this container
	 */
	public boolean canInteractWith(PlayerEntity playerIn) {
		return this.worldPos.applyOrElse((p_234646_2_, p_234646_3_) -> {
			return !this.checkBlock(p_234646_2_.getBlockState(p_234646_3_)) ? false
					: playerIn.getDistanceSq((double) p_234646_3_.getX() + 0.5D, (double) p_234646_3_.getY() + 0.5D,
							(double) p_234646_3_.getZ() + 0.5D) <= 64.0D;
		}, true);
	}

	protected boolean checkBlock(BlockState blockState) {
		return blockState.isIn(Registration.EDITOR.get());
	}

	protected boolean checkItem(ItemStack stack) {
		return true;
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this
	 * moves the stack between the player inventory and the other inventory(s).
	 */
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index == 3) {
				if (!this.mergeItemStack(itemstack1, 4, 40, true)) {

					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemstack1, itemstack);
			} else if (index != 0 && index != 1 && index != 2) {
				if (index >= 4 && index < 40) {
					int i = 0;
					
					if(itemstack.getItem().equals(RLItems.best_shard)) {
						i = 2;
					}else if(itemstack.getItem().equals(RLItems.TRAIT_HOLDER)) {
						i = 1;
					}
					
					if (!this.mergeItemStack(itemstack1, i, 3, false)) {
						return ItemStack.EMPTY;
					}
				}
			} else if (!this.mergeItemStack(itemstack1, 4, 40, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemstack1);
		}

		
		
		
		return itemstack;
	}

}