package brightspark.extraskills;

import codersafterdark.reskillable.api.skill.Skill;
import com.google.common.base.MoreObjects;
import net.minecraft.util.ResourceLocation;

import static brightspark.extraskills.ExtraSkills.MODID;

public class BasicSkill extends Skill
{
    private final String localName;

    public BasicSkill(String name, String localName, String background)
    {
        super(new ResourceLocation(MODID, name), new ResourceLocation("minecraft", "textures/blocks/" + background + ".png"));
        this.localName = localName;
    }

    @Override
    public String getName()
    {
        return localName;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("key", getKey())
                .add("localName", localName)
                .add("icon", getSpriteLocation())
                .add("background", getBackground())
                .toString();
    }
}
