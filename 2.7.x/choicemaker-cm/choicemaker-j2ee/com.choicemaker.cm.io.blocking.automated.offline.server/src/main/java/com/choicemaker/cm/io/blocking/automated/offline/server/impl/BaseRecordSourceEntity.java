package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.BaseRecordSourceJPA.CN_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.BaseRecordSourceJPA.CN_TYPE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.BaseRecordSourceJPA.DISCRIMINATOR_COLUMN;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.BaseRecordSourceJPA.ID_GENERATOR_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.BaseRecordSourceJPA.ID_GENERATOR_PK_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.BaseRecordSourceJPA.ID_GENERATOR_PK_COLUMN_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.BaseRecordSourceJPA.ID_GENERATOR_TABLE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.BaseRecordSourceJPA.ID_GENERATOR_VALUE_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.BaseRecordSourceJPA.TABLE_NAME;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.choicemaker.cm.args.PersistableRecordSource;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN,
		discriminatorType = DiscriminatorType.STRING)
@Table(name = TABLE_NAME)
public class BaseRecordSourceEntity implements PersistableRecordSource {

	private static final long serialVersionUID = 271L;

	@Id
	@Column(name = CN_ID)
	@TableGenerator(name = ID_GENERATOR_NAME, table = ID_GENERATOR_TABLE,
			pkColumnName = ID_GENERATOR_PK_COLUMN_NAME,
			valueColumnName = ID_GENERATOR_VALUE_COLUMN_NAME,
			pkColumnValue = ID_GENERATOR_PK_COLUMN_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = ID_GENERATOR_NAME)
	protected long id;

	@Column(name = CN_TYPE)
	protected final String type;

	protected BaseRecordSourceEntity(String type) {
		if (type == null || !type.equals(type.trim()) || type.isEmpty()) {
			throw new IllegalArgumentException("invalid type: '" + type + "'");
		}
		this.type = type;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BaseRecordSourceEntity other = (BaseRecordSourceEntity) obj;
		if (id != other.id) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RECORD_SOURCE_ROLE [id=" + id + ", type=" + type + "]";
	}

}
