/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 IceDragon200
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package growthcraft.core.common.inventory;

import growthcraft.core.common.inventory.slot.SlotInput;
import growthcraft.core.common.inventory.slot.SlotPlayer;
import growthcraft.core.common.inventory.slot.SlotPlayerBackpack;
import growthcraft.core.common.inventory.slot.SlotPlayerHotbar;
import growthcraft.core.common.tileentity.IGuiNetworkSync;
import growthcraft.core.util.Platform;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class GrcContainer extends Container
{
	protected static final int SLOT_W = 18;
	protected static final int SLOT_H = 18;

	protected TileEntity tileEntity;

	public GrcContainer(TileEntity te)
	{
		super();
		this.tileEntity = te;
	}

	public boolean mergeWithSlot(Slot slot, ItemStack stack)
	{
		if (stack == null) return false;
		if (stack.stackSize <= 0) return false;

		if (slot.isItemValid(stack))
		{
			if (mergeItemStack(stack, slot.slotNumber, slot.slotNumber + 1, false))
			{
				return true;
			}
		}
		return false;
	}

	public boolean mergeWithPlayer(ItemStack stack)
	{
		int start = -1;
		int end = -1;

		for (Object sub : inventorySlots)
		{
			if (sub instanceof SlotPlayer)
			{
				final SlotPlayer subSlot = (SlotPlayer)sub;
				if (start < 0)
				{
					start = subSlot.slotNumber;
				}
				end = subSlot.slotNumber;
			}
		}
		if (start <= -1 || end <= -1) return false;

		return mergeItemStack(stack, start, end + 1, false);
	}

	public boolean mergeWithInput(ItemStack stack)
	{
		boolean wasMerged = false;
		for (Object sub : inventorySlots)
		{
			if (stack.stackSize <= 0) break;
			if (sub instanceof SlotInput)
			{
				final SlotInput subSlot = (SlotInput)sub;
				wasMerged |= mergeWithSlot(subSlot, stack);
			}
		}
		return wasMerged;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index)
	{
		if (Platform.isClient())
		{
			return null;
		}

		final Slot s = getSlot(index);
		ItemStack itemstack = null;

		if (s != null && s.getHasStack())
		{
			final ItemStack stack = s.getStack();
			itemstack = stack.copy();

			boolean wasMerged = false;

			if (s instanceof SlotPlayer)
			{
				wasMerged |= mergeWithInput(stack);
			}
			else
			{
				wasMerged |= mergeWithPlayer(stack);
			}

			if (wasMerged)
			{
				s.onSlotChange(stack, itemstack);
			}
			else
			{
				return null;
			}

			if (stack.stackSize <= 0)
			{
				s.putStack((ItemStack)null);
			}
			else
			{
				s.onSlotChanged();
			}

			if (stack.stackSize == itemstack.stackSize)
			{
				return null;
			}

			s.onPickupFromSlot(player, stack);
		}
		return itemstack;
	}

	public void bindPlayerHotbar(IInventory playerInventory, int x, int y)
	{
		for (int i = 0; i < 9; ++i)
		{
			addSlotToContainer(new SlotPlayerHotbar(playerInventory, i, x + i * SLOT_W, y));
		}
	}

	public void bindPlayerBackpack(IInventory playerInventory, int x, int y)
	{
		for (int row = 0; row < 3; ++row)
		{
			for (int col = 0; col < 9; ++col)
			{
				final int slotIndex = 9 + col + row * 9;
				addSlotToContainer(new SlotPlayerBackpack(playerInventory, slotIndex, x + col * SLOT_W, y + row * SLOT_H));
			}
		}
	}

	public void bindPlayerInventory(IInventory playerInventory, int x, int y)
	{
		bindPlayerBackpack(playerInventory, x, y);
		bindPlayerHotbar(playerInventory, x, y + 58);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		if (tileEntity instanceof IInventory)
		{
			return ((IInventory)tileEntity).isUseableByPlayer(player);
		}
		return false;
	}

	// crafters
	@Override
	public void addCraftingToCrafters(ICrafting iCrafting)
	{
		super.addCraftingToCrafters(iCrafting);
		if (tileEntity instanceof IGuiNetworkSync)
		{
			((IGuiNetworkSync)tileEntity).sendGUINetworkData(this, iCrafting);
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		if (tileEntity instanceof IGuiNetworkSync)
		{
			final IGuiNetworkSync sync = (IGuiNetworkSync)tileEntity;
			for (int i = 0; i < crafters.size(); i++)
			{
				sync.sendGUINetworkData(this, (ICrafting) crafters.get(i));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int id, int v)
	{
		if (tileEntity instanceof IGuiNetworkSync)
		{
			((IGuiNetworkSync)tileEntity).receiveGUINetworkData(id, v);
		}
	}
}
