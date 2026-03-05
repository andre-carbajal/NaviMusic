package net.andrecarbajal.naviMusic.dto.response

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

open class Response(
    var text: String? = null,
    var type: Type = Type.OK,
    var ephimeral: Boolean = false
) {
    enum class Type {
        OK, ERROR, USER_ERROR
    }

    open fun editReply(event: SlashCommandInteractionEvent) {
        event.interaction.hook.editOriginal(text ?: "").queue()
    }

    open fun sendReply(event: SlashCommandInteractionEvent) {
        event.reply(text ?: "").setEphemeral(ephimeral).queue()
    }

    class ResponseBuilder {
        private var text: String? = null
        private var type: Type = Type.OK
        private var ephimeral: Boolean = false

        fun text(text: String?) = apply { this.text = text }
        fun type(type: Type) = apply { this.type = type }
        fun ephimeral(ephimeral: Boolean) = apply { this.ephimeral = ephimeral }
        fun build() = Response(text, type, ephimeral)
    }

    companion object {
        @JvmStatic
        fun builder() = ResponseBuilder()
    }
}
