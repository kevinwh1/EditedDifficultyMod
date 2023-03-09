package com.majruszsdifficulty.gamemodifiers.list.gamestages;

import com.majruszsdifficulty.GameStage;
import com.majruszsdifficulty.Registries;
import com.majruszsdifficulty.gamemodifiers.contexts.OnGameStageChange;
import com.mlib.annotations.AutoInstance;
import com.mlib.data.JsonListener;
import com.mlib.data.SerializableStructure;
import com.mlib.gamemodifiers.GameModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@AutoInstance
public class ChatMessageSender extends GameModifier {
	final Supplier< Messages > messages;

	public ChatMessageSender() {
		super( Registries.Modifiers.DEFAULT );

		this.messages = JsonListener.add( "game_stages", Registries.getLocation( "messages" ), Messages.class, Messages::new );

		new OnGameStageChange.Context( this::sendMessage )
			.addCondition( data->!data.isLoadedFromDisk() )
			.addCondition( data->data.previous.ordinal() < data.current.ordinal() )
			.insertTo( this );
	}

	private void sendMessage( OnGameStageChange.Data data ) {
		this.messages.get().stream()
			.filter( message->data.current == message.gameStage )
			.forEach( message->{
				MutableComponent component = new TranslatableComponent( message.id )
					.withStyle( message.chatFormatting != null ? new ChatFormatting[]{ message.chatFormatting } : data.current.getChatFormatting() );

				data.server.getPlayerList()
					.getPlayers()
					.forEach( player->player.displayClientMessage( component, false ) );
			} );
	}

	static class Messages extends SerializableStructure {
		List< Message > messages = new ArrayList<>();

		public Messages() {
			this.define( null, ()->this.messages, x->this.messages = x, Message::new );
		}

		public Stream< Message > stream() {
			return this.messages.stream();
		}
	}

	static class Message extends SerializableStructure {
		String id = "";
		GameStage gameStage = GameStage.NORMAL;
		ChatFormatting chatFormatting = null;

		public Message() {
			this.define( "id", ()->this.id, x->this.id = x );
			this.define( "game_stage", ()->this.gameStage, x->this.gameStage = x, GameStage::values );
			this.define( "style", ()->this.chatFormatting, x->this.chatFormatting = x, ChatFormatting::values );
		}
	}
}
