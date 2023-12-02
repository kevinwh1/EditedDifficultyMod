package com.majruszsdifficulty.items;

import com.majruszlibrary.text.TextHelper;
import com.majruszsdifficulty.MajruszsDifficulty;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class CreativeModeTabs {
	private static final Component PRIMARY = TextHelper.translatable( "itemGroup.majruszsdifficulty.primary" );

	public static Supplier< CreativeModeTab > primary() {
		return ()->CreativeModeTab.builder( CreativeModeTab.Row.TOP, 0 )
			.title( PRIMARY )
			.displayItems( CreativeModeTabs::definePrimaryItems )
			.icon( ()->new ItemStack( MajruszsDifficulty.Items.FRAGILE_END_STONE.get() ) )
			.build();
	}

	private static void definePrimaryItems( CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output ) {
		Stream.of(
			MajruszsDifficulty.Items.INFERNAL_SPONGE,
			MajruszsDifficulty.Items.SOAKED_INFERNAL_SPONGE,
			MajruszsDifficulty.Items.ENDERIUM_BLOCK,
			MajruszsDifficulty.Items.ENDERIUM_SHARD_ORE,
			MajruszsDifficulty.Items.FRAGILE_END_STONE,
			MajruszsDifficulty.Items.INFESTED_END_STONE,
			MajruszsDifficulty.Items.BANDAGE,
			MajruszsDifficulty.Items.GOLDEN_BANDAGE,
			MajruszsDifficulty.Items.CLOTH,
			MajruszsDifficulty.Items.UNDEAD_BATTLE_STANDARD,
			MajruszsDifficulty.Items.SOUL_JAR,
			MajruszsDifficulty.Items.RECALL_POTION,
			MajruszsDifficulty.Items.EVOKER_FANG_SCROLL,
			MajruszsDifficulty.Items.SONIC_BOOM_SCROLL,
			MajruszsDifficulty.Items.CERBERUS_FANG,
			MajruszsDifficulty.Items.ENDER_POUCH,
			MajruszsDifficulty.Items.ENDERIUM_SHARD_LOCATOR,
			MajruszsDifficulty.Items.ENDERIUM_SHARD,
			MajruszsDifficulty.Items.ENDERIUM_INGOT,
			MajruszsDifficulty.Items.ENDERIUM_SMITHING_TEMPLATE
		).map( item->new ItemStack( item.get() ) ).forEach( output::accept );

		Stream.of( Items.TIPPED_ARROW, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION ).forEach( item->
			Stream.of(
				MajruszsDifficulty.Potions.WITHER,
				MajruszsDifficulty.Potions.WITHER_LONG,
				MajruszsDifficulty.Potions.WITHER_STRONG
			).forEach( potion->output.accept( PotionUtils.setPotion( new ItemStack( item ), potion.get() ) ) )
		);

		Stream.of(
			MajruszsDifficulty.Items.WITHER_SWORD,
			MajruszsDifficulty.Items.ENDERIUM_SWORD,
			MajruszsDifficulty.Items.ENDERIUM_SHOVEL,
			MajruszsDifficulty.Items.ENDERIUM_PICKAXE,
			MajruszsDifficulty.Items.ENDERIUM_AXE,
			MajruszsDifficulty.Items.ENDERIUM_HOE,
			MajruszsDifficulty.Items.TATTERED_HELMET,
			MajruszsDifficulty.Items.TATTERED_CHESTPLATE,
			MajruszsDifficulty.Items.TATTERED_LEGGINGS,
			MajruszsDifficulty.Items.TATTERED_BOOTS,
			MajruszsDifficulty.Items.ENDERIUM_HELMET,
			MajruszsDifficulty.Items.ENDERIUM_CHESTPLATE,
			MajruszsDifficulty.Items.ENDERIUM_LEGGINGS,
			MajruszsDifficulty.Items.ENDERIUM_BOOTS,
			MajruszsDifficulty.Items.ANGLER_TREASURE_BAG,
			MajruszsDifficulty.Items.ELDER_GUARDIAN_TREASURE_BAG,
			MajruszsDifficulty.Items.ENDER_DRAGON_TREASURE_BAG,
			MajruszsDifficulty.Items.PILLAGER_TREASURE_BAG,
			MajruszsDifficulty.Items.UNDEAD_ARMY_TREASURE_BAG,
			MajruszsDifficulty.Items.WARDEN_TREASURE_BAG,
			MajruszsDifficulty.Items.WITHER_TREASURE_BAG,
			MajruszsDifficulty.Items.CERBERUS_SPAWN_EGG,
			MajruszsDifficulty.Items.CREEPERLING_SPAWN_EGG,
			MajruszsDifficulty.Items.CURSED_ARMOR_SPAWN_EGG,
			MajruszsDifficulty.Items.GIANT_SPAWN_EGG,
			MajruszsDifficulty.Items.ILLUSIONER_SPAWN_EGG,
			MajruszsDifficulty.Items.TANK_SPAWN_EGG
		).map( item->new ItemStack( item.get() ) ).forEach( output::accept );
	}
}
