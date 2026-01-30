package io.dampen59.mineboxadditions.utils.models;

public class SkillData {
    private final Skill skill;
    private Integer level;
    private Long current;
    private Long max;

    public SkillData(Skill skill) {
        this.skill = skill;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Long getCurrentXp() {
        return current;
    }

    public void setCurrentXp(long xp) {
        this.current = xp;
    }

    public Long getMaxXp() {
        return max;
    }

    public void setMaxXP(long xp) {
        this.max = xp;
    }

    @Override
    public String toString() {
        return String.format("skill=%s, lvl=%d, xp=%d/%d", skill.getName().getString(), level, current, max);
    }
}
