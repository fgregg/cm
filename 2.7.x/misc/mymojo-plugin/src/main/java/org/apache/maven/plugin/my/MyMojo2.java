package org.apache.maven.plugin.my;

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
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.xmlconf.XmlParserFactory;

/**
 * Goal which generates Java source code from ClueMaker model files
 */
@Mojo( name = "generate",
	defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
    )
public class MyMojo2 extends AbstractMojo {

	private static final String EOL = System.getProperty("line.separator");

	/** File extension for model files */
	public static final String MODEL_FILE_PATTERN = ".model";

	@Component
	private MavenProject project;

	/**
	 * parameter expression="${plugin.artifacts}"
	 * required
	 */
	@Parameter( defaultValue = "${plugin.artifacts}",
			required = true
			)
	private List<Artifact> artifacts;

	/**
	 * An optional list of fully qualified class names for generator plugins.
	 * 
	 * parameter alias="generators"
	 */
	@Parameter( alias = "generators"
			)
	private String[] generatorFqcns;

	/**
	 * A required location for model files
	 * 
	 * parameter property="cluemaker.source.directory"
	 *            default-value="src/main/cluemaker"
	 * required
	 */
	@Parameter( property = "cluemaker.source.directory",
			defaultValue = "src/main/cluemaker",
			required = true)
	private File sourceDirectory;

	/**
	 * A required location for generated Java files
	 * 
	 * @parameter defaultValue="${project.build.directory}/generated-sources"
	 * @required
	 */
	@Parameter( defaultValue = "${project.build.directory}/generated-sources",
			required = true)
	private File targetDirectory;

	public void setGenerators(String[] fqcns) {
		generatorFqcns = fqcns;
	}

	@SuppressWarnings("unchecked")
	protected void installChoiceMakerComponents() throws XmlConfException {
		// Install the generator plugins used by the compiler
		List<GeneratorPlugin> generators =
			ListBackedGeneratorPluginFactory.load();
		if (generatorFqcns != null && generatorFqcns.length > 0) {
			generators = ListBackedGeneratorPluginFactory.load(generatorFqcns);
		}
		IGeneratorPluginFactory factory =
			new ListBackedGeneratorPluginFactory(generators);
		InstallableGeneratorPluginFactory.getInstance().install(factory);

		// Install the compiler
		InstallableCompiler.getInstance().install(
				WellKnownPropertyValues.BASIC_COMPILER);

		// Configure ChoiceMaker
		MojoConfigurator configurator =
			new MojoConfigurator(project, sourceDirectory, targetDirectory,
					artifacts);
		ConfigurationManager.install(configurator);
		ConfigurationManager.getInstance().init();
	}

	private static final String SP2 = "  ";

	public void execute() throws MojoExecutionException {

		if (artifacts != null) {
			for (Artifact a : artifacts) {
				System.out.println(a.getClass().getName());
				System.out.println(SP2 + "groupId: " + a.getGroupId());
				System.out.println(SP2 + "artifactId: " + a.getArtifactId());
				System.out.println(SP2 + "version: " + a.getVersion());
				System.out.println(SP2 + "classifier: " + a.getClassifier());
				System.out.println(SP2 + "isResolved: " + a.isResolved());
				System.out.println(SP2 + "file: " + a.getFile().getName());
			}
		}

		// Set up the source and target directories
		File f = targetDirectory;
		if (!f.exists()) {
			f.mkdirs();
		}
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
							+ sourceDirectory.getPath() + "'");
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
				String msg = "Error(s) compiling " + model.getName() + ": ";
				sb.append(msg).append(EOL);
			}
		}
		if (exceptionCount != 0) {
			String msg =
				"Compilation failures: " + exceptionCount + EOL + sb.toString();
			throw new MojoExecutionException(msg);
		}
	}

	void generateJavaCode(File model) throws CompilerException {

		// Log the current file to the console
		System.out.println(model.getName());

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
	}

}
