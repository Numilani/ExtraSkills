package brightspark.extraskills;

public class SkillDto
{
    public String name, localName, background;

    private SkillDto() {}

    public SkillDto(String name, String localName, String background)
    {
        this.name = name;
        this.localName = localName;
        this.background = background;
    }
}
