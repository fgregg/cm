package org.apache.maven.plugin.my;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
//import org.codehaus.plexus.component.annotations.Component;
import org.apache.maven.plugins.annotations.Component;

import com.choicemaker.cm.core.gen.GeneratorPlugin;
import com.choicemaker.cm.core.gen.IGeneratorPluginFactory;
import com.choicemaker.cm.core.gen.InstallableGeneratorPluginFactory;
import com.choicemaker.cm.core.gen.ListBackedGeneratorPluginFactory;

/**
 * Goal which generates Java source code from ClueMaker model files
 *
 * @goal generate
 * @phase process-sources // * @configurator include-project-dependencies // * @requiresDependencyResolution
 *        compile+runtime
 */
public class MyMojo2 extends AbstractMojo {

	@Component
	private MavenProject project;

	/** File extension for model files */
	public static final String MODEL_FILE_PATTERN = ".model";

	/**
	 * An optional list of fully qualified class names for generator plugins.
	 *
	 * @parameter alias="generators"
	 */
	private String[] generatorFqcns;

	/**
	 * A required location for model files
	 *
	 * @parameter expression="${cluemaker.source.directory}"
	 *            default-value="src/main/cluemaker"
	 * @required
	 */
	private File sourceDirectory;

	/**
	 * A required location for generated Java files
	 *
	 * @parameter expression="${project.build.directory}/generated-sources"
	 * @required
	 */
	private File targetDirectory;

	public void setGenerators(String[] fqcns) {
		generatorFqcns = fqcns;
	}

	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException {

		// Set the classloader
//				ClassWorld world = new ClassWorld();
//				ClassRealm realm;
//				try {
//					realm = world.newRealm("gwt", null);
//					for (String elt : project.getCompileSourceRoots()) {
//						URL url = new File(elt).toURI().toURL();
//						realm.addURL(url);
//						if (getLog().isDebugEnabled()) {
//							getLog().debug("Source root: " + url);
//						}
//					}
//					for (String elt : project.getCompileClasspathElements()) {
//						URL url = new File(elt).toURI().toURL();
//						realm.addURL(url);
//						if (getLog().isDebugEnabled()) {
//							getLog().debug("Compile classpath: " + url);
//						}
//					}
//				} catch (DuplicateRealmException e) {
//					throw new MojoExecutionException(e.getMessage(), e);
//				} catch (MalformedURLException e) {
//					throw new MojoExecutionException(e.getMessage(), e);
//				} catch (DependencyResolutionRequiredException e) {
//					throw new MojoExecutionException(e.getMessage(), e);
//				}
//
////				// Argument to Compiler ctor
////				importFromCurrentClassLoader(realm, CompilerOptions.class);
////				// Argument to Compiler#run
////				importFromCurrentClassLoader(realm, TreeLogger.class);
////				// Referenced by CompilerOptions; TreeLogger.Type is already imported
////				// via TreeLogger above
////				importFromCurrentClassLoader(realm, JsOutputOption.class);
////				// Makes error check easier
////				importFromCurrentClassLoader(realm, UnableToCompleteException.class);
//
//				Thread.currentThread().setContextClassLoader(realm);
		// END Set the classloader

		List<GeneratorPlugin> generators = ListBackedGeneratorPluginFactory
				.load();

		File f = targetDirectory;
		if (!f.exists()) {
			f.mkdirs();
		}

		if (generatorFqcns != null && generatorFqcns.length > 0) {
			generators = ListBackedGeneratorPluginFactory.load(generatorFqcns);
		}

		IGeneratorPluginFactory factory = new ListBackedGeneratorPluginFactory(
				generators);
		InstallableGeneratorPluginFactory.getInstance().install(factory);

		f = sourceDirectory;
		if (!f.exists()) {
			throw new MojoExecutionException(
					"source directory does not exist: "
							+ sourceDirectory.getPath());
		}
		if (!f.isDirectory()) {
			throw new MojoExecutionException("not a directory: "
					+ sourceDirectory.getPath());
		}

		FileFilter ff = new FileFilter() {
			public boolean accept(File pathname) {
				boolean retVal = pathname.isFile();
				retVal = retVal
						&& pathname.getName().endsWith(MODEL_FILE_PATTERN);
				return retVal;
			}
		};
		File[] models = f.listFiles(ff);
		if (models == null || models.length == 0) {
			throw new MojoExecutionException(
					"no models found in source directory '"
							+ sourceDirectory.getPath() + "'");
		}
		for (int i = 0; i < models.length; i++) {
			generateJavaCode(models[i]);
		}

	}

	void generateJavaCode(File model) {
		System.out.println(model.getAbsolutePath());
	}

}
