package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.utils.Utils;
import io.dampen59.mineboxadditions.utils.models.Skill;
import io.dampen59.mineboxadditions.utils.models.SkillData;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Debug(export = true)
@Mixin(BossBar.class)
public abstract class BossBarMixin {
    @Unique
    private static final Pattern ZONE_PATTERN = Pattern.compile("끫\\s*([^]+?)");
    @Unique
    private static final Pattern TIME_PATTERN = Pattern.compile("끪\\s*([^]+?)");
    @Unique
    private static final Pattern SKILL_PATTERN = Pattern.compile("^(.+?)\\s*\\|\\s*.*?(\\d+)\\s*\\((\\d+)\\s*/\\s*(\\d+)\\)$");

    @Inject(method = "setName", at = @At("HEAD"))
    private void mbx$setName(Text name, CallbackInfo ci) {
        Matcher timeMatch = TIME_PATTERN.matcher(name.getString());
        if (timeMatch.find()) {
            Utils.updateTime(timeMatch.group(1));
        }

        Matcher matcher = SKILL_PATTERN.matcher(name.getString());
        if (matcher.find()) {
            for (Skill skill : Skill.values()) {
                if (skill.getName().getString().equals(matcher.group(1))) {
                    SkillData data = Utils.getSkill(skill);
                    data.setLevel(Integer.parseInt(matcher.group(2)));
                    data.setCurrentXp(Integer.parseInt(matcher.group(3)));
                    data.setMaxXP(Integer.parseInt(matcher.group(4)));
                    break;
                }
            }
        }
    }
}
