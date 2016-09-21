package robin.metaworlds.api;

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
		public int placeHolderItemID;
		
		public RecipePlaceHolderDef(Character placeHolderChar, int itemID)
		{
			this.placeHolder = placeHolderChar;
			this.placeHolderItemID = itemID;
		}
	}
	
	public final boolean isValid;//Should the recipe be added or not?
	
	public final int itemIDtoCraft;
	public final boolean isShaped;//Shaped or shapeless?
	public final String[] stringsToParse;
	public final RecipePlaceHolderDef[] parsedPlaceHolders;
	
	public RecipeConfig(Configuration sourceConfig, String recipeName, int craftedItemID, boolean defaultShaped, String[] defaultShape, RecipePlaceHolderDef[] placeholders)
	{
		this.itemIDtoCraft = craftedItemID;
		
		String[] defaultString = new String[4 + 2 * placeholders.length];
		defaultString[0] = defaultShaped ? "shaped" : "shapeless";
		defaultString[1] = defaultShape[0];
		defaultString[2] = defaultShape[1];
		defaultString[3] = defaultShape[2];
		
		int i = 4;
		for (RecipePlaceHolderDef curDef : placeholders)
		{
			defaultString[i++] = curDef.placeHolder.toString();
			defaultString[i++] = String.valueOf(curDef.placeHolderItemID);
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
			int newPlaceHolderItemID = Integer.parseInt(stringsToParse[4 + i*2 + 1]);
			
			RecipePlaceHolderDef curPlaceHolder = new RecipePlaceHolderDef(newPlaceHolderChar, newPlaceHolderItemID);
			
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
				varargs[i++] = new ItemStack(Item.getItemById(curPlaceHolderDef.placeHolderItemID), 1, 0);
			}
			
			GameRegistry.addShapedRecipe(new ItemStack(Item.getItemById(this.itemIDtoCraft), 1, 0), varargs);
			
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
				
				ItemStack curItem = new ItemStack(Item.getItemById(curPlaceHolderDef.placeHolderItemID), 1, 0);
				
				while (matcher.find())
					argsList.add(curItem);
			}
			
			GameRegistry.addShapelessRecipe(new ItemStack(Item.getItemById(this.itemIDtoCraft), 1, 0), argsList.toArray());
		}
		
		return true;
	}
}
