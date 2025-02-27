package org.luke.discordLinking.DiscordSide;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.luke.discordLinking.Data;

import static org.luke.discordLinking.DiscordSide.DiscordBot.getMemberById;

public class RoleAssigner {

    public enum RoleMode{
        Add,
        Remove
    }

    public static void assignRole(Long userId, Long roleId, RoleMode mode) {
        Guild guild = DiscordBot.getJda().getGuildById(Data.discordServerID);
        if (guild == null) {
            System.out.println("Guild not found!");
            return;
        }

        getMemberById(guild, userId, member -> {
            if (member == null) {
                System.out.println("Member not found!");
                return;
            }

            Role role = guild.getRoleById(roleId);
            if (role == null) {
                System.out.println("Role not found!");
                return;
            }

            if(mode == RoleMode.Add) {
                guild.addRoleToMember(member, role).queue(
                        success -> System.out.println(member.getUser().getName() + " に " + role.getName() + " ロールを適用しました。"),
                        error -> System.out.println("Failed to add role: " + error.getMessage())
                );
            } else {
                guild.removeRoleFromMember(member, role).queue(
                        success -> System.out.println(member.getUser().getName() + " から " + role.getName() + " ロールを削除しました。"),
                        error -> System.out.println("Failed to remove role: " + error.getMessage())
                );
            }
        });

    }
}
