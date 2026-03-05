package net.andrecarbajal.naviMusic.commands

import net.andrecarbajal.naviMusic.dto.response.Response
import net.andrecarbajal.naviMusic.dto.response.RichResponse
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData

abstract class SlashCommand {
    val name: String
    val description: String
    val category: Category
    var permission: Permission? = null
    var optionDataList: MutableList<OptionData> = mutableListOf()

    constructor(name: String, description: String, category: Category) {
        this.name = name
        this.description = description
        this.category = category
    }

    constructor(
        name: String,
        description: String,
        category: Category,
        permission: Permission?,
        optionDataList: MutableList<OptionData>
    ) {
        this.name = name
        this.description = description
        this.category = category
        this.permission = permission
        this.optionDataList = optionDataList
    }

    enum class Category {
        GENERAL, MUSIC
    }

    protected fun addOption(optionData: OptionData) {
        optionDataList.add(optionData)
    }

    abstract fun onCommand(event: SlashCommandInteractionEvent)

    protected fun noVoiceChannelCheck(e: SlashCommandInteractionEvent): Boolean {
        val member = e.member ?: return true
        val inVoiceChannel = e.guild?.voiceChannels?.any { it.members.contains(member) } ?: false

        if (!inVoiceChannel) {
            val response = RichResponse(
                ephimeral = true,
                type = Response.Type.USER_ERROR,
                title = "You must be in a voice channel to use this command"
            )
            response.sendReply(e)
            return true
        }

        return false
    }
}
