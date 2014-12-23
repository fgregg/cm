package com.choicemaker.cmit.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.choicemaker.cm.args.AbaSettings;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultServerConfigurationEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultSettingsEntity;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.impl.TransitivityParametersEntity;

/**
 * Lists of objects created during a test. Provides a convenient way of cleaning
 * up persistent objects after a test is finished. This class is not
 * thread-safe, so it should be used only within the scope of a method.
 * 
 * @author rphall
 */
public class TestEntities {

	private static final Logger logger = Logger.getLogger(TestEntities.class
			.getName());

	private Set<Object> objects = new LinkedHashSet<>();

	protected void add(Object o) {
		if (o != null) {
			objects.add(o);
		}
	}

	public void add(OabaJob job) {
		add((Object) job);
	}

	public void add(TransitivityJob job) {
		add((Object) job);
	}

	public void add(OabaParameters params) {
		add((Object) params);
	}

	public void add(TransitivityParametersEntity tp) {
		add((Object) tp);
	}

	public void add(OabaJobProcessing p) {
		add((Object) p);
	}

	public void add(ServerConfiguration sc) {
		add((Object) sc);
	}

	public void add(DefaultServerConfigurationEntity dscb) {
		add((Object) dscb);
	}

	public void add(AbaSettings aba) {
		add((Object) aba);
	}

	public void add(OabaSettings oaba) {
		add((Object) oaba);
	}

	public void add(DefaultSettingsEntity dse) {
		add((Object) dse);
	}

	public boolean contains(Object o) {
		boolean retVal = false;
		if (o != null) {
			retVal = objects.contains(o);
		}
		return retVal;
	}

	public void removePersistentObjects(EntityManager em) {
		try {
			removePersistentObjects(em, null);
		} catch (Exception e) {
			logger.severe(e.toString());
			throw new RuntimeException(e.toString());
		}
	}

	public void removePersistentObjects(EntityManager em, UserTransaction utx)
			throws Exception {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		for (Object o : objects) {
			final Long oId = getId(o);
			if (oId != null) {
				final Class<?> c = o.getClass();
				if (oId != 0) {
					boolean usingUtx = false;
					if (utx != null) {
						utx.begin();
						usingUtx = true;
					}
					Object refresh = em.find(c, oId);
					if (refresh != null) {
						em.merge(refresh);
						boolean isManaged = em.contains(refresh);
						if (!isManaged) {
							String typeName = o.getClass().getSimpleName();
							logger.warning(typeName + " " + oId
									+ " is not managed");
						} else {
							em.remove(refresh);
						}
					}
					if (usingUtx) {
						utx.commit();
					}
				}
			} else {
				try {
					// utx.begin();
					em.merge(o);
					em.remove(o);
					// utx.commit();
				} catch (Exception x) {
					logger.warning(x.toString());
				}
			}
		}
	}

	// This method works because the various 'add' methods ensure only objects
	// with a 'getId()' method are added to the set of managed objects
	private Long getId(Object o) {
		Long retVal = null;
		try {
			Class<?> c = o.getClass();
			Method getId = c.getMethod("getId", (Class[]) null);
			retVal = (long) getId.invoke(o, (Object[]) null);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			logger.warning("unable to find method 'getId': " + e.toString());
		}
		return retVal;
	}

}
