package brightspark.extraskills;

import codersafterdark.reskillable.api.skill.Skill;
import net.minecraft.util.ResourceLocation;

import static brightspark.extraskills.ExtraSkills.MODID;

public class BasicSkill extends Skill
{
    public BasicSkill(String name, String background)
    {
        super(new ResourceLocation(MODID, name), new ResourceLocation("minecraft", "textures/blocks/" + background + ".png"));
    }
}
