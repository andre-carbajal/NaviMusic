package net.andrecarbajal.naviMusic.dto.response

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

class RichResponse(
    var text: String? = null,
    var type: Type = Type.OK,
    var ephimeral: Boolean = false,
    var title: String? = null,
    var color: Color? = null,
    var fields: List<MessageEmbed.Field>? = null,
    var image: String? = null,
    var thumbnail: String? = null,
    var author: Author? = null,
    var footer: Footer? = null,
    var member: Member? = null
) {
    enum class Type {
        OK, ERROR, USER_ERROR
    }

    fun sendReply(event: SlashCommandInteractionEvent) {
        if (member != null) {
            event.reply(member!!.asMention).addEmbeds(buildEmbed()).queue()
            return
        }
        event.replyEmbeds(buildEmbed()).setEphemeral(ephimeral).queue()
    }

    fun editReply(event: SlashCommandInteractionEvent) {
        event.interaction.hook.editOriginalEmbeds(buildEmbed()).queue()
    }

    private fun buildEmbed(): MessageEmbed {
        val eb = EmbedBuilder()

        title?.let { eb.setTitle(it) }

        if (color != null) {
            eb.setColor(color)
        } else {
            when (type) {
                Type.ERROR -> eb.setColor(Color.RED)
                Type.USER_ERROR -> eb.setColor(Color.YELLOW)
                else -> {}
            }
        }

        fields?.forEach { eb.addField(it) }
        image?.let { eb.setImage(it) }
        eb.setThumbnail(thumbnail ?: "https://i.imgur.com/xiiGqIO.png")

        author?.let { eb.setAuthor(it.name, it.avatarUrl, it.url) }
        footer?.let { eb.setFooter(it.text, it.imageUrl) }

        eb.setDescription(text)

        return eb.build()
    }

    data class Author(
        val name: String,
        var url: String? = null,
        var avatarUrl: String? = null
    )

    data class Footer(
        val text: String,
        var imageUrl: String? = null
    )

    class RichResponseBuilder {
        private var text: String? = null
        private var type: Type = Type.OK
        private var ephimeral: Boolean = false
        private var title: String? = null
        private var color: Color? = null
        private var fields: List<MessageEmbed.Field>? = null
        private var image: String? = null
        private var thumbnail: String? = null
        private var author: Author? = null
        private var footer: Footer? = null
        private var member: Member? = null

        fun text(text: String?) = apply { this.text = text }
        fun type(type: Type) = apply { this.type = type }
        fun ephimeral(ephimeral: Boolean) = apply { this.ephimeral = ephimeral }
        fun title(title: String?) = apply { this.title = title }
        fun color(color: Color?) = apply { this.color = color }
        fun fields(fields: List<MessageEmbed.Field>?) = apply { this.fields = fields }
        fun image(image: String?) = apply { this.image = image }
        fun thumbnail(thumbnail: String?) = apply { this.thumbnail = thumbnail }
        fun author(author: Author?) = apply { this.author = author }
        fun footer(footer: Footer?) = apply { this.footer = footer }
        fun member(member: Member?) = apply { this.member = member }

        fun build() = RichResponse(
            text, type, ephimeral, title, color, fields, image, thumbnail, author, footer, member
        )
    }

    companion object {
        @JvmStatic
        fun builder() = RichResponseBuilder()
    }
}
