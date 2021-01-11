package com.majruszs_difficulty.items;

import com.majruszs_difficulty.RegistryHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;

/** Simple bandage item that removes bleeding effect and gives regeneration for few seconds. */
@Mod.EventBusSubscriber
public class BandageItem extends Item {
	public BandageItem() {
		super( ( new Properties() ).maxStackSize( 16 )
			.group( RegistryHandler.ITEM_GROUP ) );
	}

	/** Using bandage on right click. (self healing) */
	@Override
	public ActionResult< ItemStack > onItemRightClick( World world, PlayerEntity player, Hand hand ) {
		ItemStack itemStack = player.getHeldItem( hand );

		removeBleedingIfPossible( itemStack, player, player );

		return ActionResult.func_233538_a_( itemStack, world.isRemote() );
	}

	/** Adding tooltip with information for what bandage is used. */
	@Override
	@OnlyIn( Dist.CLIENT )
	public void addInformation( ItemStack stack, @Nullable World world, List< ITextComponent > toolTip, ITooltipFlag flag ) {
		toolTip.add( new TranslationTextComponent( "item.majruszs_difficulty.bandage.item_tooltip" ).mergeStyle( TextFormatting.GRAY ) );
	}

	/** Using bandage on right click. (other entity healing) */
	@SubscribeEvent
	public static void onRightClick( PlayerInteractEvent.EntityInteract event ) {
		if( !( event.getTarget() instanceof LivingEntity ) )
			return;

		if( removeBleedingIfPossible( event.getItemStack(), event.getPlayer(), ( LivingEntity )event.getTarget() ) )
			event.setCancellationResult( ActionResultType.SUCCESS );
	}

	/**
	 Removes bleeding from target if it is possible.

	 @param bandage Bandage item.
	 @param player  Player that is right clicking.
	 @param target  Target that will be healed. (may be the same player)

	 @return Returns information (boolean) if effect was removed.
	 */
	protected static boolean removeBleedingIfPossible( ItemStack bandage, PlayerEntity player, LivingEntity target ) {
		if( !( target.isPotionActive( RegistryHandler.BLEEDING.get() ) && bandage.getItem() instanceof BandageItem ) )
			return false;

		if( !player.abilities.isCreativeMode )
			bandage.shrink( 1 );

		player.addStat( Stats.ITEM_USED.get( bandage.getItem() ) );

		target.removePotionEffect( RegistryHandler.BLEEDING.get() );
		target.removeActivePotionEffect( RegistryHandler.BLEEDING.get() );
		target.world.playSound( null, target.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.AMBIENT, 1.0f, 1.0f );

		return true;
	}
}