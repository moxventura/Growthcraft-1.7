package growthcraft.core;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import javax.annotation.Nonnull;

import growthcraft.api.core.log.ILoggable;
import growthcraft.api.core.log.ILogger;
import growthcraft.api.core.log.NullLogger;
import growthcraft.api.core.util.TagParser;

import net.minecraftforge.common.config.Configuration;

/**
 * Extend this class when you need config for another module, see the other
 * modules for usage.
 */
public abstract class ConfigBase implements ILoggable
{
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	protected static @interface ConfigOption
	{
		// the config option's category
		String catergory() default Configuration.CATEGORY_GENERAL;
		// the config option's name
		String name();
		// the config option's description
		String desc() default "";
		// optional value - usually used for configuring a type checker or parser
		String opt() default "";
		// default value for special types
		String def() default "";
	}

	private static final String DEFAULT_STR = "; Default : ";

	// All configs will include a Debug option.
	@ConfigOption(catergory="Debug", name="Enable Debugging", desc="Should Growthcraft log all its activity for debugging purposes?")
	public boolean debugEnabled;

	protected ILogger logger = NullLogger.INSTANCE;
	protected Configuration config;

	@Override
	public void setLogger(@Nonnull ILogger l)
	{
		this.logger = l;
	}

	@SuppressWarnings("rawtypes")
	private void loadConfigForClass(Class klass)
	{
		for (Field field : klass.getDeclaredFields())
		{
			final ConfigOption opt = field.getAnnotation(ConfigOption.class);
			if (opt != null)
			{
				final Class typeClass = field.getType();
				try
				{
					if (Byte.TYPE.equals(typeClass))
					{
						final byte val = field.getByte(this);
						field.setShort(this, (byte)config.get(opt.catergory(), opt.name(), val, opt.desc() + DEFAULT_STR + val).getInt());
					}
					else if (Short.TYPE.equals(typeClass))
					{
						final short val = field.getShort(this);
						field.setShort(this, (short)config.get(opt.catergory(), opt.name(), val, opt.desc() + DEFAULT_STR + val).getInt());
					}
					else if (Integer.TYPE.equals(typeClass))
					{
						final int val = field.getInt(this);
						field.setInt(this, config.get(opt.catergory(), opt.name(), val, opt.desc() + DEFAULT_STR + val).getInt());
					}
					else if (Float.TYPE.equals(typeClass))
					{
						final float val = field.getFloat(this);
						field.setFloat(this, (float)config.get(opt.catergory(), opt.name(), (double)val, opt.desc() + DEFAULT_STR + val).getDouble());
					}
					else if (Boolean.TYPE.equals(typeClass))
					{
						final boolean val = field.getBoolean(this);
						field.setBoolean(this, config.get(opt.catergory(), opt.name(), val, opt.desc() + DEFAULT_STR + val).getBoolean());
					}
					else if (Double.TYPE.equals(typeClass))
					{
						final double val = field.getDouble(this);
						field.setDouble(this, config.get(opt.catergory(), opt.name(), val, opt.desc() + DEFAULT_STR + val).getDouble());
					}
					else if (String.class.equals(typeClass))
					{
						final String val = (String)field.get(this);
						field.set(this, config.get(opt.catergory(), opt.name(), val, opt.desc() + DEFAULT_STR + val).getString());
					}
					else if (typeClass.isArray())
					{
						if (TagParser.Tag.class.equals(typeClass))
						{
							final String val = opt.def();
							final TagParser parser = opt.opt().equals("scsv") ?
								TagParser.scsv :
								(opt.opt().equals("cosv") ? TagParser.cosv : TagParser.csv);

							field.set(this,
								parser.parse(
									config.get(
										opt.catergory(),
										opt.name(),
										val,
										opt.desc() + DEFAULT_STR + val
									).getString()
								)
							);
						}
						else
						{
							logger.error("Unhandled `Array` type config option: type=%s option=%s", typeClass, opt.name());
						}
					}
					else
					{
						logger.error("Unhandled config option: type=%s option=%s", typeClass, opt.name());
					}

					// Only use this when you need to debug config options
					logger.debug("ConfigBase<%s> catergory='%s' name='%s' key='%s' value=%s", opt.catergory(), this.toString(), opt.name(), field.getName(), field.get(this).toString());
				}
				catch (IllegalAccessException ex)
				{
					logger.error(ex.toString());
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected void autoloadConfig()
	{
		Class currentClass = getClass();
		while (currentClass != null)
		{
			loadConfigForClass(currentClass);
			currentClass = currentClass.getSuperclass();
		}
	}

	protected void loadConfig()
	{
		autoloadConfig();
	}

	protected void postLoadConfig()
	{

	}

	/**
	 * Creates a Configuration instance and loads it, this will then pass
	 * control to loadConfig; where the config reading should take place.
	 *
	 * @param configDir - root config directory
	 * @param filename - config filename
	 */
	public void load(File configDir, String filename)
	{
		this.config = new Configuration(new File(configDir, filename));
		try
		{
			config.load();
			loadConfig();
			postLoadConfig();
		}
		finally
		{
			if (config.hasChanged())
			{
				config.save();
				logger.warn("Config file %s has changed, be sure for check for updates.", filename);
			}
		}
	}
}
