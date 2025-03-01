package org.luke.discordLinking.DiscordSide;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import static org.luke.discordLinking.DiscordLinking.getInstance;
import static org.luke.discordLinking.DiscordSide.DiscordBot.getMemberById;

public class RoleAssigner {

    public enum RoleMode{
        Add,
        Remove
    }

    public static void assignRole(Long userId, Long roleId, RoleMode mode) {
        Guild guild = DiscordBot.getGuild();
        getMemberById(guild, userId, member -> {
            if (member == null) {
                getInstance().getLogger().warn("メンバーが見つかりませんでした。 ユーザーid: {}", userId);
                return;
            }

            Role role = guild.getRoleById(roleId);
            if (role == null) {
                getInstance().getLogger().warn("ロールが見つかりませんでした。 ロールid: {}", roleId);
                return;
            }

            if(mode == RoleMode.Add) {
                guild.addRoleToMember(member, role).queue(
                        success -> getInstance().getLogger().info("{} に {} ロールを適用しました。", member.getUser().getName(), role.getName()),
                        error -> getInstance().getLogger().error("ロールを適用するのに失敗しました。 {} ", error.getMessage())
                );
            } else {
                guild.removeRoleFromMember(member, role).queue(
                        success -> getInstance().getLogger().info("{} から {} ロールを削除しました。", member.getUser().getName(), role.getName()),
                        error -> getInstance().getLogger().error("ロールを削除するのに失敗しました。 {}", error.getMessage())
                );
            }
        });

    }
}
