package com.majruszs_difficulty.commands;

import com.majruszs_difficulty.GameState;
import com.mlib.commands.BaseCommand;
import com.mlib.commands.CommandManager;
import com.mlib.commands.IRegistrableCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

/** Command that displays current game stage. */
public class GetGameStageCommand extends BaseCommand implements IRegistrableCommand {
	/** Registers this command. */
	@Override
	public void register( CommandDispatcher< CommandSourceStack > commandDispatcher ) {
		CommandManager commandManager = new CommandManager( commandDispatcher );
		commandManager.register( literal( "gamestage" ), this::handleCommand );
		commandManager.register( literal( "gamestate" ), this::handleCommand );
	}

	/** Changes current game stage and sends information to all players. */
	protected int handleCommand( CommandContext< CommandSourceStack > context, CommandSourceStack source ) {
		source.sendSuccess( CommandsHelper.createGameStageMessage( GameState.getCurrentMode(), "current" ), true );

		return GameState.convertStateToInteger( GameState.getCurrentMode() );
	}
}
