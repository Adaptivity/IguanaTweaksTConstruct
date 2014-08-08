package iguanaman.iguanatweakstconstruct.restriction;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.restriction.RestrictionHelper;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.CastingRecipe;
import tconstruct.library.crafting.PatternBuilder;
import tconstruct.library.event.PartBuilderEvent;
import tconstruct.library.event.SmelteryCastEvent;
import tconstruct.library.tools.BowstringMaterial;
import tconstruct.library.tools.CustomMaterial;
import tconstruct.library.tools.FletchingMaterial;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.util.IPattern;
import tconstruct.library.util.IToolPart;
import tconstruct.tools.items.ToolPart;

import java.util.List;
import java.util.Set;

public class PartRestrictionHandler {
    @SubscribeEvent
    public void onPartBuilding(PartBuilderEvent.NormalPart event)
    {
        PatternBuilder.ItemKey key = PatternBuilder.instance.getItemKey(event.material);
        if(key == null)
            return;
        PatternBuilder.MaterialSet set = ((PatternBuilder.MaterialSet)PatternBuilder.instance.materialSets.get(key.key));
        if(set == null)
            return;

        ToolMaterial mat = TConstructRegistry.getMaterial(set.materialID);

        if(RestrictionHelper.isRestricted(event.pattern, mat))
            event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent
    public void onPartCasting(SmelteryCastEvent.CastingTable event)
    {
        // null checks
        if(event.recipe == null || event.recipe.output == null)
            return;
        CastingRecipe recipe = event.recipe;
        ItemStack output = recipe.output;

        if(output.getItem() == null || !(output.getItem() instanceof IToolPart))
            return;

        IToolPart part = (IToolPart)output.getItem();
        ToolMaterial mat = TConstructRegistry.getMaterial(part.getMaterialID(output));
        if(RestrictionHelper.isRestricted(event.recipe.cast, mat))
            event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent
    public void onItemToolTip(ItemTooltipEvent event) {
        if (event.entityPlayer == null)
            return;

        // we're only interested if it's a tool part
        if (!(event.itemStack.getItem() instanceof IPattern))
            return;

        boolean foundMat = false;
        // regular materials
        List<ToolMaterial> materials = RestrictionHelper.getPatternMaterials(event.itemStack);
        if(materials != null) {
            for (ToolMaterial mat : materials) {
                event.toolTip.add(mat.style() + mat.name());
            }
            foundMat = true;
        }

        // custom materials
        List<CustomMaterial> customMaterials = RestrictionHelper.getPatternCustomMaterials(event.itemStack);
        if(customMaterials != null) {
            for (CustomMaterial mat : customMaterials) {
                event.toolTip.add(mat.input.getDisplayName());
            }
            foundMat = true;
        }

        if(!foundMat)
            event.toolTip.add(EnumChatFormatting.DARK_RED + "No known Materials");
    }
}
