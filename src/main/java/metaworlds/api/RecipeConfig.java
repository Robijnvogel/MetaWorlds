package metaworlds.api;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

public class RecipeConfig {
	public static class RecipePlaceHolderDef
	{
		public Character placeHolder;
		public String placeHolderItem;
		
		public RecipePlaceHolderDef(Character placeHolderChar, String itemName)
		{
			this.placeHolder = placeHolderChar;
			this.placeHolderItem = itemName;
		}
	}
	
	public final boolean isValid;//Should the recipe be added or not?
	
	public final ItemStack itemToCraft;
	public final boolean isShaped;//Shaped or shapeless?
	public final String[] stringsToParse;
	public final RecipePlaceHolderDef[] parsedPlaceHolders;
	
	public RecipeConfig(Configuration sourceConfig, String recipeName, ItemStack craftedItem, boolean defaultShaped, String[] defaultShape, RecipePlaceHolderDef[] placeholders)
	{
		this.itemToCraft = craftedItem;
		
		String[] defaultString = new String[4 + 2 * placeholders.length];
		defaultString[0] = defaultShaped ? "shaped" : "shapeless";
		defaultString[1] = defaultShape[0];
		defaultString[2] = defaultShape[1];
		defaultString[3] = defaultShape[2];
		
		int i = 4;
		for (RecipePlaceHolderDef curDef : placeholders)
		{
			defaultString[i++] = curDef.placeHolder.toString();
			defaultString[i++] = curDef.placeHolderItem;
		}
		
		stringsToParse = sourceConfig.get("recipe", recipeName, defaultString).getStringList();
		
		//Check length - at least one placeholder needed to be valid
		if (stringsToParse.length < 6 || ((stringsToParse.length - 4) % 2) != 0 || 
				(!stringsToParse[0].equalsIgnoreCase("shaped") && !stringsToParse[0].equalsIgnoreCase("shapeless")))
		{
			isValid = false;
			isShaped = false;
			parsedPlaceHolders = null;
			return;
		}
		
		isShaped = stringsToParse[0].equalsIgnoreCase("shaped");
		parsedPlaceHolders = new RecipePlaceHolderDef[(stringsToParse.length - 4) / 2];
		for (i = 0; i < parsedPlaceHolders.length; ++i)
		{
			char newPlaceHolderChar = stringsToParse[4 + i*2].charAt(0);
			String newPlaceHolderItemName = stringsToParse[4 + i*2 + 1];
			
			RecipePlaceHolderDef curPlaceHolder = new RecipePlaceHolderDef(newPlaceHolderChar, newPlaceHolderItemName);
			
			parsedPlaceHolders[i] = curPlaceHolder;
		}
		
		isValid = true;
	}
	
	public boolean addRecipeToGameRegistry()
	{
		if (!this.isValid)
			return false;

		if (this.isShaped)
		{
			Object[] varargs = new Object[this.stringsToParse.length - 1];
			varargs[0] = stringsToParse[1];
			varargs[1] = stringsToParse[2];
			varargs[2] = stringsToParse[3];
			int i = 3;
			for (RecipePlaceHolderDef curPlaceHolderDef : this.parsedPlaceHolders)
			{
				varargs[i++] = curPlaceHolderDef.placeHolder;
				
				ItemStack itemStack = getItemByName(curPlaceHolderDef.placeHolderItem);
				
				if (itemStack == null)
				    return false;
				
				varargs[i++] = itemStack;
			}
			
			//GameRegistry.addShapedRecipe(new ItemStack(this.itemToCraft, 1, 0), varargs);
			GameRegistry.addShapedRecipe(this.itemToCraft, varargs);
			
			//GameRegistry.addShapedRecipe(new ItemStack(this.itemIDtoCraft, 1, 0), stringsToParse[1], stringsToParse[2], stringsToParse[3], )
		}
		else
		{
			String recipeContents = (stringsToParse[1] + stringsToParse[2] + stringsToParse[3]);
			
			List argsList = new LinkedList();
			
			for (RecipePlaceHolderDef curPlaceHolderDef : this.parsedPlaceHolders)
			{
				Pattern pattern = Pattern.compile(curPlaceHolderDef.placeHolder.toString());
				Matcher matcher = pattern.matcher(recipeContents);
				
				ItemStack curItem = getItemByName(curPlaceHolderDef.placeHolderItem);
				if (curItem == null)
				    return false;
				
				while (matcher.find())
					argsList.add(curItem);
			}
			
			GameRegistry.addShapelessRecipe(this.itemToCraft, argsList.toArray());
			//GameRegistry.addShapelessRecipe(new ItemStack(this.itemIDtoCraft, 1, 0), argsList.toArray());
		}
		
		return true;
	}
	
	public static ItemStack getItemByName(String itemName)
	{
	    Block block = Block.getBlockFromName(itemName);
	    if (block != null)
	        return new ItemStack(block, 1, 0);
	    
	    Item item = (Item)Item.itemRegistry.getObject(itemName);
	    if (item != null)
	        return new ItemStack(item, 1, 0);
	    
	    return null;
	}
}
