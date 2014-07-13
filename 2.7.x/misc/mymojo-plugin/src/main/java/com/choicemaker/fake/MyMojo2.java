package com.choicemaker.fake;

import java.io.File;
import java.io.FileFilter;
import java.io.StringWriter;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.choicemaker.cm.core.WellKnownPropertyValues;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.compiler.CompilationArguments;
import com.choicemaker.cm.core.compiler.CompilerException;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.compiler.InstallableCompiler;
import com.choicemaker.cm.core.configure.ConfigurationManager;
import com.choicemaker.cm.core.gen.GeneratorPlugin;
import com.choicemaker.cm.core.gen.IGeneratorPluginFactory;
import com.choicemaker.cm.core.gen.InstallableGeneratorPluginFactory;
import com.choicemaker.cm.core.gen.ListBackedGeneratorPluginFactory;
import com.choicemaker.cm.core.xmlconf.XmlParserFactory;
import com.choicemaker.util.FileUtilities;
import com.choicemaker.util.SystemPropertyUtils;

/**
 * Goal which generates Java source code from ClueMaker model files
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES,
		requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MyMojo2 extends AbstractMojo {

	/**
	 * Default location, relative to the project build directory, for the root
	 * of the source code tree generated by this Maven plugin.
	 */
	public static final String GENERATED_SOURCE_PATH = "generated-sources/cluemaker";

	/**
	 * Default location, relative to the project POM file, for a directory
	 * containing ChoiceMaker model files.
	 */
	public static final String CLUEMAKER_PATH = "src/main/cluemaker";

	private static final String EOL = System.getProperty(SystemPropertyUtils.LINE_SEPARATOR);

	/** File extension for model files */
	public static final String MODEL_FILE_PATTERN = ".model";

	@Component
	private MavenProject project;

	/**
	 * parameter expression="${plugin.artifacts}" required
	 */
	@Parameter(defaultValue = "${plugin.artifacts}", required = true)
	private List<Artifact> artifacts;

	/**
	 * An optional list of fully qualified class names for generator plugins.
	 * 
	 * parameter alias="generators"
	 */
	@Parameter(alias = "generators")
	private String[] generatorFqcns;

	/**
	 * A required location for model files
	 */
	@Parameter(property = "cluemaker.source.directory",
			defaultValue = "src/main/cluemaker", required = true)
	private File cluemakerDirectory;

	/**
	 * A required location for generated Java files
	 */
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/cluemaker",
			required = true)
	private File generatedSourceDirectory;

	/**
	 * A required location for compiled Java files
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}",
			required = true)
	private File compiledCodeDirectory;

	public void setGenerators(String[] fqcns) {
		generatorFqcns = fqcns;
	}

	@SuppressWarnings("unchecked")
	protected void installChoiceMakerComponents() throws XmlConfException {

		// Install the generator plugins used by the compiler
		List<GeneratorPlugin> generators;
		if (generatorFqcns != null && generatorFqcns.length > 0) {
			generators = ListBackedGeneratorPluginFactory.load(generatorFqcns);
		} else {
			generators = ListBackedGeneratorPluginFactory.load();
		}
		if (getLog().isDebugEnabled()) {
			for (GeneratorPlugin generator : generators) {
				getLog().debug(
						"generator plugin: " + generator.getClass().getName());
			}
		}
		if (generators.isEmpty()) {
			getLog().info("NOTE: No generator plugins -- was this intended?");
		}

		IGeneratorPluginFactory factory =
			new ListBackedGeneratorPluginFactory(generators);
		InstallableGeneratorPluginFactory.getInstance().install(factory);

		// Install the compiler
		InstallableCompiler.getInstance().install(
				WellKnownPropertyValues.BASIC_COMPILER);

		// Configure ChoiceMaker
		MojoConfigurator configurator =
			new MojoConfigurator(project, cluemakerDirectory,
					generatedSourceDirectory, compiledCodeDirectory, artifacts);
		ConfigurationManager.install(configurator);
		ConfigurationManager.getInstance().init();
	}

	static final String SP2 = "  ";

	public void execute() throws MojoExecutionException {

		if (getLog().isDebugEnabled()) {
			if (artifacts != null) {
				for (Artifact a : artifacts) {
					getLog().debug(a.getClass().getName());
					getLog().debug(SP2 + "groupId: " + a.getGroupId());
					getLog().debug(SP2 + "artifactId: " + a.getArtifactId());
					getLog().debug(SP2 + "version: " + a.getVersion());
					getLog().debug(SP2 + "classifier: " + a.getClassifier());
					getLog().debug(SP2 + "isResolved: " + a.isResolved());
					getLog().debug(SP2 + "file: " + a.getFile().getName());
				}
			}
		}

		// Set up the source and target directories
		File f = generatedSourceDirectory;
		if (!f.exists()) {
			f.mkdirs();
		}
		getLog().debug("generatedSourceDirectory: " + generatedSourceDirectory);

		f = cluemakerDirectory;
		if (!f.exists()) {
			throw new MojoExecutionException(
					"source directory does not exist: "
							+ cluemakerDirectory.getPath());
		}
		if (!f.isDirectory()) {
			throw new MojoExecutionException("not a directory: "
					+ cluemakerDirectory.getPath());
		}
		getLog().debug("cluemakerDirectory: " + cluemakerDirectory);

		// Configure ChoiceMaker
		try {
			installChoiceMakerComponents();
		} catch (XmlConfException e1) {
			e1.fillInStackTrace();
			throw new MojoExecutionException("Configuration error.", e1);
		}

		// Read the ClueMaker model files
		FileFilter ff = new FileFilter() {
			public boolean accept(File pathname) {
				boolean retVal = pathname.isFile();
				retVal =
					retVal && pathname.getName().endsWith(MODEL_FILE_PATTERN);
				return retVal;
			}
		};
		File[] models = f.listFiles(ff);
		if (models == null || models.length == 0) {
			throw new MojoExecutionException(
					"no models found in source directory '"
							+ cluemakerDirectory.getPath() + "'");
		} else {
			for (File model : models) {
				getLog().info("Found model: " + model.getName());
			}
		}

		// Generate Java code from the ClueMaker models
		int exceptionCount = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < models.length; i++) {
			File model = models[i];
			try {
				generateJavaCode(model);
			} catch (CompilerException e) {
				++exceptionCount;
				String msg =
					"Code generation failed for " + model.getName() + ": ";
				// 'info' since failures are expected in unit tests
				getLog().info(msg);
				sb.append(msg).append(EOL);
			}
		}
		if (exceptionCount != 0) {
			String msg =
				"Compilation failures: " + exceptionCount + EOL + sb.toString();
			throw new MojoExecutionException(msg);
		}

		// Add the generated-sources directory to the build path
		String gsPath = generatedSourceDirectory.getPath();
		getLog().info("Adding '" + gsPath + "' to compile-source root");
		project.addCompileSourceRoot(gsPath);
		if (getLog().isInfoEnabled()) {
			getLog().info("Compile source roots:");
			for (String s : project.getCompileSourceRoots()) {
				getLog().info(SP2 + s);
			}
		}
		
	}

	void generateJavaCode(File model) throws CompilerException {

		// Log the current file to the console
		getLog().info("Generating code for " + model.getName());

		// Parse the model file for the name of the clues file
		String fileName = model.getAbsolutePath();
		Document document = null;
		SAXBuilder builder = XmlParserFactory.createSAXBuilder(false);
		try {
			document = builder.build(model);
		} catch (Exception ex) {
			throw new CompilerException("Internal error.", ex);
		}
		Element m = document.getRootElement();
		String clueFileName = m.getAttributeValue("clueFileName");
		getLog().debug("Clues: " + clueFileName);

		// Set up the compilation arguments
		CompilationArguments arguments = new CompilationArguments();
		// String[] args = { clueFileName };
		String[] args =
			{ FileUtilities.getAbsoluteFile(new File(fileName).getParentFile(),
					clueFileName).toString() };
		arguments.enter(args);
		StringWriter statusOutput = new StringWriter();

		// Translate the ClueMaker clues file (and the record layout schema
		// that backs it) into Java code
		ICompiler compiler = InstallableCompiler.getInstance();
		int errorCount = compiler.generateJavaCode(arguments, statusOutput);
		if (errorCount != 0) {
			String msg =
				"Errors: " + errorCount + EOL + statusOutput.toString();
			throw new CompilerException(msg);
		}
		getLog().info("Code generation completed for " + model.getName());
	}

}
