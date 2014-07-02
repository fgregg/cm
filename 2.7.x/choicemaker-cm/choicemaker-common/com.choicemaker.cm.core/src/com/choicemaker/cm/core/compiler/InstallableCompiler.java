package com.choicemaker.cm.core.compiler;

import java.io.Writer;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ProbabilityModelSpecification;
import com.choicemaker.cm.core.PropertyNames;

/**
 * A singleton implementation that uses an installable delegate to implement
 * Compiler methods. In general, a delegate should be installed only once in an
 * application context, and this class enforces this restriction by using a
 * {@link PropertyNames#INSTALLABLE_COMPILER System property} to specify the
 * delegate type. If the property is not set, a
 * {@link #getDefaultInstance() default factory} is used.
 *
 * @author rphall
 *
 */
public final class InstallableCompiler implements ICompiler {

	private static final Logger logger = Logger
			.getLogger(InstallableCompiler.class.getName());

	/**
	 * The default instance is a {@link DoNothingCompiler stubbed
	 * implementation} that doesn't actually compile anything.
	 */
	static final ICompiler getDefaultInstance() {
		return DoNothingCompiler.instance;
	}

	/** The singleton instance of this factory */
	private static InstallableCompiler singleton = new InstallableCompiler();

	/** A method that returns the factory singleton */
	public static ICompiler getInstance() {
		assert singleton != null;
		return singleton;
	}

	/**
	 * The delegate used by the factory singleton to implement the Compiler
	 * interface.
	 */
	private ICompiler delegate;

	/**
	 * If a delegate hasn't been set, this method looks up a System property to
	 * determine which type of factory to set and then sets it. If the property
	 * exists but the specified factory type can not be set, throws an
	 * IllegalStateException. If the property doesn't exist, sets the
	 * {@link #getDefaultInstance() default type}. If the default type
	 * can not be set -- for example, if the default type is misconfigured --
	 * throws a IllegalStateException.
	 *
	 * @throws IllegalStateException
	 *             if a delegate does not exist and can not be set.
	 */
	ICompiler getDelegate() {
		if (delegate == null) {
			String msgPrefix = "Installing compiler factory: ";
			String fqcn = System
					.getProperty(PropertyNames.INSTALLABLE_COMPILER);
			try {
				if (fqcn != null) {
					logger.info(msgPrefix + fqcn);
					setDelegate(fqcn);
				} else {
					logger.info(msgPrefix
							+ getDefaultInstance().getClass().getName());
					setDelegate(getDefaultInstance());
				}
			} catch (Exception x) {
				String msg = msgPrefix + x.toString() + ": " + x.getCause();
				logger.error(msg, x);
				assert delegate == null;
				throw new IllegalStateException(msg);
			}
		}
		assert delegate != null;
		return delegate;
	}

	public String compile(CompilationArguments arguments, Writer statusOutput)
			throws CompilerException {
		return getDelegate().compile(arguments, statusOutput);
	}

	public ImmutableProbabilityModel compile(ProbabilityModelSpecification model,
			Writer statusOutput) throws CompilerException {
		return getDelegate().compile(model, statusOutput);
	}

	public Properties getFeatures() {
		return getDelegate().getFeatures();
	}

	public boolean compile(IProbabilityModel model, Writer statusOutput)
			throws CompilerException {
		return getDelegate().compile(model, statusOutput);
	}

	/** For testing only; otherwise treat as private */
	InstallableCompiler() {
	}

	/**
	 * Sets the factory delegate.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 * */
	private void setDelegate(ICompiler delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("null delegate");
		}
		this.delegate = delegate;
	}

	/**
	 * An alternative method for setting a factory delegate using a FQCN factory
	 * name.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 */
	private void setDelegate(String fqcn) {
		if (fqcn == null || fqcn.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank class name for compiler factory");
		}
		final String msgPrefix = "Installing compiler factory: ";
		try {
			Class c = Class.forName(fqcn);
			ICompiler instance = (ICompiler) c.newInstance();
			setDelegate(instance);
		} catch (Exception e) {
			String msg = msgPrefix + e.toString() + ": " + e.getCause();
			logger.error(msg, e);
			throw new IllegalArgumentException(msg);
		}
	}

}
