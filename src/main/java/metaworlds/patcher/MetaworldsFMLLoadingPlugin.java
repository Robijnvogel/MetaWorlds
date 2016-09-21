package metaworlds.patcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.jblas.DoubleMatrix;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion(value = "1.7.2")
public class MetaworldsFMLLoadingPlugin implements IFMLLoadingPlugin 
{
    //private static Logger logger = Logger.getLogger(ClassPatcher.class.getName());

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{ClassPatcher.class.getName()/*, 
                "cpw.mods.fml.common.asm.transformers.MarkerTransformer"*/};
    }

    @Override
    public String getModContainerClass() {
        return MetaworldsDummyContainer.class.getName();
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        FMLLog.log("Metaworlds", Level.INFO, "Checking libraries...");
        
        if (!checkLibraries())
        {
            if (!findInstaller((File)data.get("coremodLocation")))
            {
                FMLLog.log("Metaworlds", Level.FATAL, "Libraries for metaworlds missing!");
                System.exit(1);
            }
            
            int result = JOptionPane.showConfirmDialog(null, "MetaWorlds is missing components! Install now?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION)
            {
                runInstaller();
                System.exit(0);
            }
            else
            {
                FMLLog.log("Metaworlds", Level.FATAL, "Libraries for metaworlds missing! (+Installer cancelled)");
                System.exit(1);
            }
        }
        
        FMLLog.log("Metaworlds", Level.INFO, "Setting up patches");
        moveTransformerUpInHierarchy();
        ClassPatcher.setupBinPatch(FMLLaunchHandler.side(), (File)data.get("coremodLocation"));
    }
    
    protected boolean checkLibraries()
    {
        try
        {
            DoubleMatrix testMatrix = DoubleMatrix.eye(4);
            testMatrix = testMatrix.mmul(testMatrix);
        }
        catch(java.lang.NoClassDefFoundError e)
        {
            return false;
        }
        
        return true;
    }
    
    protected File thisFile = null;
    
    protected boolean findInstaller(File thisJar)
    {
        thisFile = thisJar;
        try {
            boolean result = false;
            ZipFile zip = new ZipFile(thisJar);
            
            if (zip.getEntry("MetaWorldsInst.jar") != null)
                result = true;
            
            zip.close();
            
            return result;
        } catch(Exception e) { }
        
        return false;
    }
    
    protected void runInstaller()
    {
        try
        {
            ZipFile installerZip = new ZipFile(thisFile);
            ZipEntry installerEntry = installerZip.getEntry("MetaWorldsInst.jar");
            
            File installerFile = File.createTempFile("MetaWorldsInst", ".jar");
            
            InputStream installerInputStream = installerZip.getInputStream(installerEntry);
            OutputStream installerOutputStream = new FileOutputStream(installerFile);
            IOUtils.copy(installerInputStream, installerOutputStream);
            
            installerOutputStream.close();
            installerInputStream.close();
            installerZip.close();
            
            Process process = Runtime.getRuntime().exec("java -jar " + installerFile.getCanonicalPath());
            process.getErrorStream().close();
            process.getInputStream().close();
            process.getOutputStream().close();
            
            process.waitFor();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    protected void moveTransformerUpInHierarchy()
    {
        LaunchClassLoader loader = (LaunchClassLoader)getClass().getClassLoader();
        try
        {
            Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
            transformersField.setAccessible(true);
            
            List<IClassTransformer> transformers = (List<IClassTransformer>)transformersField.get(loader);
            
            //int transformerIndex = 0;
            ClassPatcher patcherInstance = null;
            
            for (IClassTransformer curTransformer : transformers)
            {
                if (curTransformer.getClass() == ClassPatcher.class)
                {
                    patcherInstance = (ClassPatcher)curTransformer;
                    break;
                }
                
                //transformerIndex++;
            }
            
            transformers.remove(patcherInstance);
            transformers.add(1, patcherInstance);
            FMLLog.log("Metaworlds", Level.INFO, "Metaworlds Patcher priority set");
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }

}
