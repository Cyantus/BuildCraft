/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.plug.FacadeBlockStateInfo;
import buildcraft.transport.plug.FacadeInstance;
import buildcraft.transport.plug.FacadePhasedState;
import buildcraft.transport.plug.FacadeStateManager;
import buildcraft.transport.plug.PluggableFacade;

public class ItemPluggableFacade extends ItemBC_Neptune implements IItemPluggable, IFacadeItem {
    public ItemPluggableFacade(String id) {
        super(id);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

	@Nullable
    public ItemStack createItemStack(FacadeInstance state) {
        ItemStack item = new ItemStack(this);
        NBTTagCompound nbt = NBTUtilBC.getItemData(item);
        state.writeToNbt(nbt, "states");
        return item;
    }

    public static FacadeInstance getStates(@Nullable ItemStack item) {
        NBTTagCompound nbt = NBTUtilBC.getItemData(item);
        return FacadeInstance.readFromNbt(nbt, "states");
    }

    @Nullable
    @Override
    public ItemStack getFacadeForBlock(IBlockState state) {
        FacadeBlockStateInfo info = FacadeStateManager.validFacadeStates.get(state);
        if (info == null) {
            return StackUtil.EMPTY;
        } else {
            return createItemStack(FacadeInstance.createSingle(info, false));
        }
    }

    @Override
    public PipePluggable onPlace(@Nullable ItemStack stack, IPipeHolder holder, EnumFacing side, EntityPlayer player,
        EnumHand hand) {
        FacadeInstance fullState = getStates(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), fullState.phasedStates[0].stateInfo.state);
        return new PluggableFacade(BCTransportPlugs.facade, holder, side, fullState);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems) {
        // Add a single phased facade as a default
        FacadePhasedState[] states = {//
            FacadeStateManager.getInfoForBlock(Blocks.STONE).createPhased(false, null),//
            FacadeStateManager.getInfoForBlock(Blocks.PLANKS).createPhased(false, EnumDyeColor.RED),//
            FacadeStateManager.getInfoForBlock(Blocks.LOG).createPhased(false, EnumDyeColor.CYAN),//
        };
        FacadeInstance inst = new FacadeInstance(states);
        subItems.add(createItemStack(inst));

        for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
            if (info.isVisible) {
                subItems.add(createItemStack(FacadeInstance.createSingle(info, false)));
                subItems.add(createItemStack(FacadeInstance.createSingle(info, true)));
            }
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FacadeInstance fullState = getStates(stack);
        if (fullState.type == FacadeType.Basic) {
            String displayName = getFacadeStateDisplayName(fullState.phasedStates[0]);
            return super.getItemStackDisplayName(stack) + ": " + displayName;
        } else {
            return LocaleUtil.localize("item.FacadePhased.name");
        }
    }

    public static String getFacadeStateDisplayName(FacadePhasedState state) {
        ItemStack assumedStack = state.stateInfo.requiredStack;
        String s = "";
        if (assumedStack != null) {
        	if (assumedStack.getItem() != null) {
        		s = assumedStack.getDisplayName();
        	}
        }
        if (state.isHollow) {
            s += " (" + LocaleUtil.localize("item.Facade.state_hollow") + ")";
        }
        return s;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        FacadeInstance states = getStates(stack);
        if (states.type == FacadeType.Phased) {
            String stateString = LocaleUtil.localize("item.FacadePhased.state");
            FacadePhasedState defaultState = null;
            for (FacadePhasedState state : states.phasedStates) {
                if (state.activeColour == null) {
                    defaultState = state;
                    continue;
                }
                tooltip.add(String.format(stateString, LocaleUtil.localizeColour(state.activeColour),
                    getFacadeStateDisplayName(state)));
            }
            if (defaultState != null) {
                tooltip.add(1, String.format(LocaleUtil.localize("item.FacadePhased.state_default"),
                    getFacadeStateDisplayName(defaultState)));
            }
        } else {
            String propertiesStart = TextFormatting.GRAY + "" + TextFormatting.ITALIC;
            FacadeBlockStateInfo info = states.phasedStates[0].stateInfo;
            BlockUtil.getPropertiesStringMap(info.state, info.varyingProperties)
                .forEach((name, value) -> tooltip.add(propertiesStart + name + " = " + value));
        }
    }

    // IFacadeItem

    @Override
    public ItemStack createFacadeStack(IFacade facade) {
        return createItemStack((FacadeInstance) facade);
    }

    @Override
    public IFacade getFacade(ItemStack facade) {
        return getStates(facade);
    }
}
