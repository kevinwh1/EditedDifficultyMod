package com.majruszsdifficulty.items;

import com.google.common.collect.ImmutableList;
import com.majruszsdifficulty.Registries;
import com.majruszsdifficulty.events.TreasureBagOpenedEvent;
import com.majruszsdifficulty.treasurebags.LootProgressManager;
import com.mlib.annotations.AutoInstance;
import com.mlib.attributes.AttributeHandler;
import com.mlib.data.SerializableStructure;
import com.mlib.effects.SoundHandler;
import com.mlib.gamemodifiers.contexts.*;
import com.mlib.items.ItemHelper;
import com.mlib.text.TextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class SoulJarItem extends Item {
	static final float DAMAGE_BONUS = 2.5f;
	static final float MOVEMENT_BONUS = 0.15f;
	static final float RANGE_BONUS = 0.5f;
	static final int ARMOR_BONUS = 2;
	static final float MINING_BONUS = 0.15f;
	static final int LUCK_BONUS = 1;
	static final AttributeHandler MOVEMENT_ATTRIBUTE = new AttributeHandler( "51e7e4fb-e8b4-4c90-ab8a-e8c334e206be", "SoulJarMovementBonus", Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL );
	static final AttributeHandler ARMOR_ATTRIBUTE = new AttributeHandler( "7d2d7767-51da-46cc-8081-80fda32d4126", "SoulJarArmorBonus", Attributes.ARMOR, AttributeModifier.Operation.ADDITION );
	static final AttributeHandler REACH_ATTRIBUTE = new AttributeHandler( "23868877-961b-44c9-89c3-376e5c06dbd1", "SoulJarReachBonus", ForgeMod.REACH_DISTANCE.get(), AttributeModifier.Operation.ADDITION );
	static final AttributeHandler RANGE_ATTRIBUTE = new AttributeHandler( "a45d6f34-5b78-4d7c-b60a-03fe6400f8cd", "SoulJarRangeBonus", ForgeMod.ATTACK_RANGE.get(), AttributeModifier.Operation.ADDITION );
	static final AttributeHandler LUCK_ATTRIBUTE = new AttributeHandler( "a2a496f4-3799-46eb-856c-1ba992f67912", "SoulJarLuckBonus", Attributes.LUCK, AttributeModifier.Operation.ADDITION );

	public static ItemStack randomItemStack( int bonusCount ) {
		ItemStack itemStack = new ItemStack( Registries.SOUL_JAR.get() );
		BonusInfo bonusInfo = new BonusInfo( itemStack.getOrCreateTag() );
		bonusInfo.bonusCount = bonusCount;
		bonusInfo.write( itemStack.getOrCreateTag() );

		return itemStack;
	}

	public SoulJarItem() {
		super( new Properties().stacksTo( 1 ).rarity( Rarity.UNCOMMON ) );
	}

	@Override
	public InteractionResultHolder< ItemStack > use( Level level, Player player, InteractionHand hand ) {
		ItemStack itemStack = player.getItemInHand( hand );
		BonusInfo bonusInfo = new BonusInfo( itemStack.getOrCreateTag() );
		if( bonusInfo.bonusMask == 0b0 ) {
			bonusInfo.randomize();
			bonusInfo.write( itemStack.getOrCreateTag() );
			SoundHandler.ENCHANT.play( level, player.position() );

			return InteractionResultHolder.sidedSuccess( itemStack, level.isClientSide() );
		}

		return InteractionResultHolder.pass( itemStack );
	}

	@AutoInstance
	public static class Handler {
		public Handler() {
			new OnItemEquipped.Context( this::updateAttributes )
				.addCondition( data->data.entity instanceof LivingEntity );

			new OnPreDamaged.Context( this::increaseDamage )
				.addCondition( data->data.target instanceof Mob mob && mob.getMobType() == MobType.UNDEAD )
				.addCondition( data->data.attacker != null )
				.addCondition( data->hasBonus( data.attacker, BonusType.DAMAGE ) );

			new OnBreakSpeed.Context( this::increaseSpeed )
				.addCondition( data->hasBonus( data.player, BonusType.MINING ) );

			new OnItemAttributeTooltip.Context( this::addTooltip )
				.addCondition( data->data.itemStack.getItem() instanceof SoulJarItem );

			new OnItemTooltip.Context( this::addTooltip )
				.addCondition( data->data.itemStack.getItem() instanceof SoulJarItem );
		}

		private static boolean hasBonus( @Nullable Entity entity, BonusType bonusType ) {
			ItemStack itemStack = entity instanceof LivingEntity livingEntity ? livingEntity.getItemBySlot( EquipmentSlot.OFFHAND ) : ItemStack.EMPTY;
			if( !( itemStack.getItem() instanceof SoulJarItem ) )
				return false;

			return new BonusInfo( itemStack.getOrCreateTag() )
				.getBonusTypes()
				.contains( bonusType );
		}

		private void updateAttributes( OnItemEquipped.Data data ) {
			LivingEntity entity = ( LivingEntity )data.entity;
			float speedBonus = hasBonus( data.entity, BonusType.MOVEMENT ) ? MOVEMENT_BONUS : 0.0f;
			float armorBonus = hasBonus( data.entity, BonusType.ARMOR ) ? ARMOR_BONUS : 0.0f;
			float rangeBonus = hasBonus( data.entity, BonusType.RANGE ) ? RANGE_BONUS : 0.0f;
			float luckBonus = hasBonus( data.entity, BonusType.LUCK ) ? LUCK_BONUS : 0.0f;

			MOVEMENT_ATTRIBUTE.setValueAndApply( entity, speedBonus );
			ARMOR_ATTRIBUTE.setValueAndApply( entity, armorBonus );
			REACH_ATTRIBUTE.setValueAndApply( entity, rangeBonus );
			RANGE_ATTRIBUTE.setValueAndApply( entity, rangeBonus );
			LUCK_ATTRIBUTE.setValueAndApply( entity, luckBonus );
		}

		private void increaseDamage( OnPreDamaged.Data data ) {
			data.extraDamage += DAMAGE_BONUS * 2.5f;
			data.spawnMagicParticles = true;
		}

		private void increaseSpeed( OnBreakSpeed.Data data ) {
			data.event.setNewSpeed( data.event.getNewSpeed() + MINING_BONUS * data.event.getOriginalSpeed() );
		}

		private void addTooltip( OnItemAttributeTooltip.Data data ) {
			BonusInfo bonusInfo = new BonusInfo( data.itemStack.getOrCreateTag() );
			if( bonusInfo.bonusMask != 0b0 ) {
				for( BonusType bonusType : bonusInfo.getBonusTypes() ) {
					data.add( EquipmentSlot.OFFHAND, bonusType.getBonusComponent( 1.0f ) );
				}
			}
		}

		private void addTooltip( OnItemTooltip.Data data ) {
			BonusInfo bonusInfo = new BonusInfo( data.itemStack.getOrCreateTag() );
			for( BonusType bonusType : ImmutableList.copyOf( bonusInfo.getBonusTypes() ).reverse() ) {
				data.tooltip.add( 1, bonusType.getSoulComponent() );
			}
			data.tooltip.addAll( 1, bonusInfo.getHintComponents() );
		}
	}

	public static class BonusInfo extends SerializableStructure {
		public int bonusMask = 0b0;
		public int bonusCount = 2;

		public BonusInfo( CompoundTag tag ) {
			super( "SoulJar" );

			this.define( "BonusMask", ()->this.bonusMask, x->this.bonusMask = x );
			this.define( "BonusCount", ()->this.bonusCount, x->this.bonusCount = x );

			this.read( tag );
		}

		public void randomize() {
			this.bonusMask = 0b0;
			List< BonusType > bonusTypes = new ArrayList<>( Arrays.stream( BonusType.values() ).toList() );
			Collections.shuffle( bonusTypes );
			bonusTypes.stream()
				.limit( this.bonusCount )
				.forEach( bonusType->this.bonusMask |= bonusType.bit );
		}

		public List< BonusType > getBonusTypes() {
			return Arrays.stream( BonusType.values() )
				.filter( bonusType->( bonusType.bit & this.bonusMask ) != 0 )
				.toList();
		}

		public List< Component > getHintComponents() {
			List< Component > components = new ArrayList<>();
			if( this.bonusMask == 0b0 ) {
				Component bonusCount = Component.literal( "" + this.bonusCount ).withStyle( ChatFormatting.GREEN );
				components.add( Component.translatable( "item.majruszsdifficulty.soul_jar.item_tooltip1", bonusCount ).withStyle( ChatFormatting.GRAY ) );
				components.add( Component.translatable( "item.majruszsdifficulty.soul_jar.item_tooltip2" ).withStyle( ChatFormatting.GRAY ) );
			} else {
				components.add( Component.translatable( "item.majruszsdifficulty.soul_jar.item_tooltip3" ).withStyle( ChatFormatting.GRAY ) );
			}

			return components;
		}
	}

	public enum BonusType {
		DAMAGE( 1 << 0, "item.majruszsdifficulty.soul_jar.smite", ChatFormatting.RED, multiplier->TextHelper.signed( DAMAGE_BONUS * multiplier ) ),
		MOVEMENT( 1 << 1, "item.majruszsdifficulty.soul_jar.movement", ChatFormatting.YELLOW, multiplier->TextHelper.signedPercent( MOVEMENT_BONUS * multiplier ) ),
		RANGE( 1 << 2, "item.majruszsdifficulty.soul_jar.range", ChatFormatting.LIGHT_PURPLE, multiplier->TextHelper.signed( RANGE_BONUS * multiplier ) ),
		ARMOR( 1 << 3, "item.majruszsdifficulty.soul_jar.armor", ChatFormatting.BLUE, multiplier->TextHelper.signed( ( int )( ARMOR_BONUS * multiplier ) ) ),
		MINING( 1 << 4, "item.majruszsdifficulty.soul_jar.mining", ChatFormatting.AQUA, multiplier->TextHelper.signedPercent( MINING_BONUS * multiplier ) ),
		LUCK( 1 << 5, "item.majruszsdifficulty.soul_jar.luck", ChatFormatting.GREEN, multiplier->TextHelper.signed( ( int )( LUCK_BONUS * multiplier ) ) );

		final int bit;
		final String id;
		final ChatFormatting soulFormatting;
		final Function< Float, String > valueProvider;

		BonusType( int bit, String id, ChatFormatting soulFormatting, Function< Float, String > valueProvider ) {
			this.bit = bit;
			this.id = id;
			this.soulFormatting = soulFormatting;
			this.valueProvider = valueProvider;
		}

		public Component getBonusComponent( float multiplier ) {
			return Component.translatable( String.format( "%s.bonus", this.id ), this.valueProvider.apply( multiplier ) )
				.withStyle( ChatFormatting.BLUE );
		}

		public Component getSoulComponent() {
			return Component.translatable( String.format( "%s.soul", this.id ) )
				.withStyle( this.soulFormatting );
		}
	}
}
