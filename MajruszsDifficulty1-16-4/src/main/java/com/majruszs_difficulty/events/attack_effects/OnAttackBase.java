package com.majruszs_difficulty.events.attack_effects;

import com.majruszs_difficulty.GameState;
import com.majruszs_difficulty.GameState.*;
import com.majruszs_difficulty.MajruszsDifficulty;
import com.majruszs_difficulty.MajruszsHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;

/** Base attack class representing event on which enemies will */
public abstract class OnAttackBase< ClassType extends LivingEntity > {
	protected final Class< ClassType > entityCausingEffect;
	protected final Mode minimumMode;
	protected final boolean shouldBeMultipliedByClampedRegionalDifficulty;
	protected final Effect[] effects;

	public OnAttackBase( Class< ClassType > entityCausingEffect, Mode minimumMode, boolean shouldBeMultipliedByClampedRegionalDifficulty, Effect[] effects ) {
		this.entityCausingEffect = entityCausingEffect;
		this.minimumMode = minimumMode;
		this.shouldBeMultipliedByClampedRegionalDifficulty = shouldBeMultipliedByClampedRegionalDifficulty;
		this.effects = effects;
	}

	public OnAttackBase( Class< ClassType > entityCausingEffect, Mode minimumMode, boolean shouldBeMultipliedByClampedRegionalDifficulty, Effect effect ) {
		this( entityCausingEffect, minimumMode, shouldBeMultipliedByClampedRegionalDifficulty, new Effect[]{ effect } );
	}

	/** Trying to apply all negative effects on entity. */
	public void tryToApplyEffect( LivingEntity attacker, LivingEntity target ) {
		if( !shouldBeExecuted( attacker ) )
			return;

		ServerWorld world = ( ServerWorld )attacker.getEntityWorld();

		for( Effect effect : this.effects ) {
			if( calculateChance( target, world ) < MajruszsDifficulty.RANDOM.nextDouble() )
				continue;

			EffectInstance effectInstance = new EffectInstance( effect, getDurationInTicks(), getAmplifier() );
			if( target.isPotionApplicable( effectInstance ) )
				target.addPotionEffect( effectInstance );
		}
	}

	/** Checking if all conditions are not met. */
	protected boolean shouldBeExecuted( LivingEntity attacker ) {
		if( !( this.entityCausingEffect.isInstance( attacker ) ) )
			return false;

		if( !GameState.atLeast( this.minimumMode ) )
			return false;

		if( !( attacker.world instanceof ServerWorld ) )
			return false;

		return isEnabled();
	}

	/** Checking if event is not disabled by the player. */
	protected abstract boolean isEnabled();

	/** Returns chance of applying negative effect on entity. */
	protected abstract double getChance();

	/** Returns the duration in ticks of the effect. */
	protected abstract int getDurationInTicks();

	/** Returns the level of the effect. */
	protected abstract int getAmplifier();

	/** Calculating final effect chance. (after applying clamped regional difficulty if needed) */
	private double calculateChance( LivingEntity attacker, ServerWorld world ) {
		if( this.shouldBeMultipliedByClampedRegionalDifficulty )
			return getChance() * MajruszsHelper.getClampedRegionalDifficulty( attacker, world );
		else
			return getChance();
	}
}
