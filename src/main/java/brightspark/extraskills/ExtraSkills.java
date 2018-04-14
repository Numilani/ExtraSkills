package brightspark.extraskills;

import codersafterdark.reskillable.api.skill.Skill;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

@Mod(modid = ExtraSkills.MODID, name = ExtraSkills.NAME, version = ExtraSkills.VERSION, dependencies = ExtraSkills.DEPENDENCIES)
public class ExtraSkills
{
    static final String MODID = "extraskills";
    static final String NAME = "ExtraSkills";
    static final String VERSION = "@VERSION@";
    static final String DEPENDENCIES = "required-after:reskillable";

    private static Logger LOGGER;
    private static File MOD_CONFIG_DIR;
    private static Set<SkillDto> SKILLS_DTO = null;
    private static Set<Skill> SKILLS = new HashSet<>();
    private static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();

        MOD_CONFIG_DIR = new File(event.getModConfigurationDirectory(), MODID);
        File jsonFile = new File(MOD_CONFIG_DIR, "skills.json");

        if(MOD_CONFIG_DIR.mkdirs()) LOGGER.info("Created mod config directory");
        if(!jsonFile.exists())
        {
            //Generate default file
            LOGGER.warn("No skills.json found! Generating default file...");
            SkillDto skill = new SkillDto("example", "Example", "stone");
            try(JsonWriter writer = GSON.newJsonWriter(new FileWriter(jsonFile)))
            {
                writer.jsonValue(GSON.toJson(ImmutableSet.of(skill)));
            }
            catch(IOException e)
            {
                LOGGER.error("Error creating default skills JSON file!");
                e.printStackTrace();
            }
        }
        else
        {
            //Read file
            try
            {
                Type type = new TypeToken<Set<SkillDto>>(){}.getType();
                SKILLS_DTO = GSON.fromJson(new JsonReader(new FileReader(jsonFile)), type);
            }
            catch(JsonParseException | FileNotFoundException e)
            {
                LOGGER.error("Error reading skills JSON file!");
                e.printStackTrace();
            }

            if(SKILLS_DTO == null || SKILLS_DTO.size() == 0)
                LOGGER.warn("No skill read from JSON file!");
            else
            {
                //Remove example skill
                SKILLS_DTO.removeIf(skill -> skill.name.equalsIgnoreCase("example"));

                LOGGER.info("Read " + SKILLS_DTO.size() + " extra skills from JSON file");

                //Convert to Skills
                SKILLS_DTO.forEach(skillDto -> SKILLS.add(new BasicSkill(skillDto.name, skillDto.background)));
            }
        }
    }

    @EventHandler
    public static void init(FMLInitializationEvent event)
    {
        //Read icon textures
        String[] iconsNames = MOD_CONFIG_DIR.list((dir, name) -> name.endsWith(".png"));
        if(iconsNames != null && iconsNames.length > 0)
        {
            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
            Set<String> icons = Sets.newHashSet(iconsNames);
            SKILLS.forEach(skill -> {
                ResourceLocation icon = skill.getSpriteLocation();
                String[] path = icon.getResourcePath().split("/");
                String fileName = path[path.length - 1];
                if(icons.contains(fileName))
                {
                    //Load icon texture
                    try
                    {
                        textureManager.loadTexture(icon, new DynamicTexture(ImageIO.read(new File(MOD_CONFIG_DIR, fileName))));
                    }
                    catch(IOException e)
                    {
                        LOGGER.warn("Couldn't load icon " + icon + "!");
                        e.printStackTrace();
                    }
                }
            });
        }

        //Add localisation
        String langPrefix = "skillable.skill." + MODID + ".";
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(resourceManager -> {
            //We're going to create a "fake" input stream from our Json file for localisations and inject them
            StringBuilder sb = new StringBuilder();
            SKILLS_DTO.forEach(skill -> sb.append(langPrefix).append(skill.name).append("=").append(skill.localName).append("\n"));
            InputStream inputStream = new ByteArrayInputStream(sb.toString().getBytes());
            LanguageMap.inject(inputStream);
        });
    }

    @Mod.EventBusSubscriber
    public static class RegistryHandler
    {
        @SubscribeEvent
        public static void regSkills(RegistryEvent.Register<Skill> event)
        {
            IForgeRegistry<Skill> registry = event.getRegistry();
            if(SKILLS != null) SKILLS.forEach(registry::register);
        }
    }
}
