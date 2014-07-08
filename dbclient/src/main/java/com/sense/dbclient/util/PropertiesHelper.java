package com.sense.dbclient.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesHelper
{
	public static String PROPERTIES_FILE = "baikuservice.properties.file";

	public static Properties properties;

	static
	{
		properties = new Properties();

		String requestedFile = System.getProperty(PROPERTIES_FILE);
		String propFileName = requestedFile != null ? requestedFile
				: "dbproxy.properties";
		File propFile = new File(propFileName);

		InputStream in = null;

		try
		{
			if (propFile.exists())
			{
				try
				{
					in = new FileInputStream(propFileName);
					properties.load(new BufferedInputStream(in));
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
			else if (requestedFile != null)
			{
				in = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(requestedFile);

				if (in == null)
				{
					throw new FileNotFoundException("找不到" + requestedFile
							+ "文件");
				}
				try
				{
					properties.load(new BufferedInputStream(in));
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
			else
			{
				/**
				 * 自定义的路径查找方案
				 */
				in = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(propFileName);

				if (in == null)
				{
					System.out.println("in is null");
					String homePath = getHomePath();
					if (homePath == null)
					{
						throw new FileNotFoundException("找不到" + propFileName
								+ "文件");
					}
					else
					{
						propFile = new File(homePath + File.separator
								+ propFileName);
						if (!propFile.exists())
						{
							throw new FileNotFoundException("找不到"
									+ propFileName + "文件");
						}
						in = new FileInputStream(propFile);
					}
				}
				try
				{
					properties.load(new BufferedInputStream(in));
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException ignore)
				{ /* ignore */
				}
			}
		}
	}

	public static String getHomePath()
	{
		String thisClassPath = null;
		try
		{
			thisClassPath = PathUtils.getPathFromClass(PropertiesHelper.class);
		}
		catch (IOException e)
		{
		}
		if (thisClassPath == null)
		{
			return null;
		}
		String thisHomePath = null;
		if (thisClassPath.endsWith(".class"))
		{
			File clsFile = new File(thisClassPath);
			int cs = PropertiesHelper.class.getPackage().getName().split("\\.").length;
			String relatePath = ".";
			for (int i = 0; i < cs; i++)
			{
				relatePath += File.separator + "..";
			}
			String tempPath = clsFile.getParent() + File.separator + relatePath;
			File file = new File(tempPath);
			try
			{
				thisHomePath = file.getCanonicalPath();
			}
			catch (IOException e)
			{
			}
		}
		else if (thisClassPath.endsWith(".jar"))
		{
			File clsFile = new File(thisClassPath);
			String tempPath = clsFile.getParent() + File.separator + ".";
			File file = new File(tempPath);
			try
			{
				thisHomePath = file.getCanonicalPath();
			}
			catch (IOException e)
			{
			}
		}
		else
		{
			thisHomePath = thisClassPath;
		}
		if (thisHomePath == null)
		{
			return null;
		}
		return thisHomePath;
	}

	// public PropertiesHelper(String propertiesFileName) {
	// try {
	// // InputStream inStream = new
	// FileInputStream(SystemEnvUtil.getConfigPath(propertiesFileName));
	// InputStream inStream = new FileInputStream(propertiesFileName);
	// properties.load(inStream);
	// } catch (FileNotFoundException e) {
	// try {
	// ClassLoader classLoader = PropertiesHelper.class.getClassLoader();
	// InputStream is = classLoader.getResourceAsStream(propertiesFileName);
	// properties.load(is);
	// } catch (IOException e1) {
	//
	// }
	// } catch (IOException e) {
	//
	// }
	// }

	public static String getValue(String key)
	{
		String property = properties.getProperty(key);
		if (property != null)
		{
			if (property.indexOf("$BASE_DIR$") != -1)
			{
				String homePath = PropertiesHelper.getHomePath();
				property = property.replace("$BASE_DIR$", homePath == null ? ""
						: homePath);
			}
		}
		return property;
	}

	public static String getValue(String key, String defaultValue)
	{
		String property = properties.getProperty(key, defaultValue);
		if (property != null)
		{
			if (property.indexOf("$BASE_DIR$") != -1)
			{
				String homePath = PropertiesHelper.getHomePath();
				property = property.replace("$BASE_DIR$", homePath == null ? ""
						: homePath);
			}
		}
		return property;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("homePath:" + PropertiesHelper.getHomePath());
		System.out.println(PropertiesHelper.getValue("emas.home.kkk",
				"$BASE_DIR$/lib/emas.jar"));
		System.out.println(PropertiesHelper.getValue("emas.ws.ca.url"));
	}

}
