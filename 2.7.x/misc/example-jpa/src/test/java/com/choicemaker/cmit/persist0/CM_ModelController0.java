package com.choicemaker.cmit.persist0;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.persist0.CMP_ModelBean;
import com.choicemaker.cm.persist0.CMP_ModelBean.NamedQuery;

@Stateless
public class CM_ModelController0 {
	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public CMP_ModelBean save(CMP_ModelBean model) {
		if (model.getId() == 0) {
			em.persist(model);
		} else {
			model = em.merge(model);
		}
		return model;
	}

	public CMP_ModelBean find(long id) {
		CMP_ModelBean model = em.find(CMP_ModelBean.class, id);
		return model;
	}

	public List<CMP_ModelBean> findAll() {
		Query query = em.createNamedQuery(NamedQuery.FIND_ALL.name);
		@SuppressWarnings("unchecked")
		List<CMP_ModelBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<CMP_ModelBean>();
		}
		return entries;
	}

	public void delete(CMP_ModelBean model) {
		model = em.merge(model);
		em.remove(model);
		em.flush();
	}

	public void detach(CMP_ModelBean model) {
		em.detach(model);
	}

}
