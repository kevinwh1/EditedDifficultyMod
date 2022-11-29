package com.majruszsdifficulty.entities;

import com.majruszsdifficulty.Registries;
import com.mlib.Random;
import com.mlib.annotations.AutoInstance;
import com.mlib.config.DoubleConfig;
import com.mlib.gamemodifiers.Condition;
import com.mlib.gamemodifiers.GameModifier;
import com.mlib.gamemodifiers.contexts.OnEntityTick;
import com.mlib.time.TimeHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

public class BlackWidowEntity extends Spider {
	public static Supplier< EntityType< BlackWidowEntity > > createSupplier() {
		return ()->EntityType.Builder.of( BlackWidowEntity::new, MobCategory.MONSTER )
			.sized( 0.7f, 0.35f )
			.build( "black_widow" );
	}

	public static AttributeSupplier getAttributeMap() {
		return Mob.createMobAttributes()
			.add( Attributes.MAX_HEALTH, 12.0 )
			.add( Attributes.MOVEMENT_SPEED, 0.3 )
			.add( Attributes.ATTACK_DAMAGE, 4.0 )
			.build();
	}

	public BlackWidowEntity( EntityType< ? extends BlackWidowEntity > type, Level world ) {
		super( type, world );
	}

	@Override
	public int getExperienceReward() {
		return Random.nextInt( 3 );
	}

	@Override
	public void playSound( SoundEvent sound, float volume, float pitch ) {
		if( !this.isSilent() ) {
			float finalVolume = volume * Random.nextFloat( 0.5f, 0.7f );
			float finalPitch = pitch * Random.nextFloat( 1.35f, 1.5f );
			this.level.playSound( null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundSource(), finalVolume, finalPitch );
		}
	}

	@Override
	protected float getStandingEyeHeight( Pose pose, EntityDimensions dimensions ) {
		return 0.2f;
	}

	@AutoInstance
	public static class WebAbility extends GameModifier {
		final DoubleConfig delay = new DoubleConfig( "delay", "Duration between creating a new web (in seconds).", false, 30.0, 5.0, 600.0 );

		public WebAbility() {
			super( Registries.Modifiers.DEFAULT, "BlackWidowWebAbility", "Black Widow spawns the web when in combat." );

			OnEntityTick.Context onTick = new OnEntityTick.Context( this::spawnWeb );
			onTick.addCondition( new Condition.IsServer() )
				.addCondition( new Condition.Excludable() )
				.addCondition( data->data.entity instanceof BlackWidowEntity )
				.addCondition( this::ticksHavePassed )
				.addConfig( this.delay );

			this.addContext( onTick );
		}

		private void spawnWeb( OnEntityTick.Data data ) {
			data.level.setBlock( data.entity.blockPosition(), Blocks.COBWEB.defaultBlockState(), 3 );
		}

		private boolean ticksHavePassed( OnEntityTick.Data data ) {
			return data.entity.tickCount % this.delay.asTicks() == 0;
		}
	}
}