package com.choicemaker.cmit.persist0;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.persist0.CM_ModelBean;
import com.choicemaker.cm.persist0.CM_ModelBean.NamedQuery;

@Stateless
public class CM_ModelController0 {
	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public CM_ModelBean save(CM_ModelBean model) {
		if (model.getId() == 0) {
			em.persist(model);
		} else {
			model = em.merge(model);
		}
		return model;
	}

	public CM_ModelBean find(long id) {
		CM_ModelBean model = em.find(CM_ModelBean.class, id);
		return model;
	}

	public List<CM_ModelBean> findAll() {
		Query query = em.createNamedQuery(NamedQuery.FIND_ALL.name);
		@SuppressWarnings("unchecked")
		List<CM_ModelBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<CM_ModelBean>();
		}
		return entries;
	}

	public void delete(CM_ModelBean model) {
		model = em.merge(model);
		em.remove(model);
		em.flush();
	}

	public void detach(CM_ModelBean model) {
		em.detach(model);
	}

}
