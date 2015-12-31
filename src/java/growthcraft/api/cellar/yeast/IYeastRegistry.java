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
package growthcraft.api.cellar.yeast;

import java.util.List;

import growthcraft.api.core.log.ILoggable;

import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

public interface IYeastRegistry extends ILoggable
{
	/**
	 * Adds the given ItemStack as a possible yeast item
	 *
	 * @param yeast - an item
	 */
	void addYeast(ItemStack yeast);

	boolean isYeast(ItemStack yeast);

	/**
	 * Maps the given yeast item to the given biome type, when a Yeast Jar
	 * is placed into a biome of that type, it MAY produce the yeast item.
	 * NOTE. This method SHOULD use addYeast to add the given yeast item to the
	 *       known list.
	 *
	 * @param yeast - an item
	 * @param type - the biome type to add
	 */
	void addYeastToBiomeType(ItemStack yeast, BiomeDictionary.Type type);

	List<ItemStack> getYeastListForBiomeType(BiomeDictionary.Type type);
	List<BiomeDictionary.Type> getBiomeTypesForYeast(ItemStack yeast);
	boolean canYeastFormInBiome(ItemStack yeast, BiomeGenBase biome);
}
