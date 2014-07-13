// Generated by ChoiceMaker. Do not edit.
package com.choicemaker.demo.simple_person_matching.gendata.gend.internal.Person2.db;
import org.apache.log4j.*;
import java.util.*;
import java.sql.*;
import com.choicemaker.cm.core.*;
import com.choicemaker.cm.core.base.*;
import com.choicemaker.cm.io.db.base.*;
import com.choicemaker.demo.simple_person_matching.gendata.gend.internal.Person2.*;
import java.util.Date;
import com.choicemaker.util.StringUtils;
import com.choicemaker.cm.validation.eclipse.impl.Validators;
public final class Person2__default__DbReaderParallel implements DbReaderParallel {
private static Logger logger = Logger.getLogger(com.choicemaker.demo.simple_person_matching.gendata.gend.internal.Person2.db.Person2__default__DbReaderParallel.class);
private ResultSet[] rs;
private static DerivedSource src = DerivedSource.valueOf("db");
public String getName() {
return "Person2:r:default";
}
private PersonImpl o__PersonImpl;
public void open(ResultSet[] rs) throws java.sql.SQLException {
this.rs = rs;
getRecordPersonImpl();
}
public Record getNext() throws java.sql.SQLException {
Record __res = o__PersonImpl;
__res.computeValidityAndDerived(src);
getRecordPersonImpl();
return __res;
}
public boolean hasNext() {
return o__PersonImpl != null;
}
public int getNoCursors() {
return NO_CURSORS;
}
static final int NO_CURSORS = 1;
private void getRecordPersonImpl() throws java.sql.SQLException {
String __tmpStr;
if(rs[0].next()) {
o__PersonImpl = new PersonImpl();
o__PersonImpl.linkage_role = rs[0].getString(1);
o__PersonImpl.entityId = rs[0].getInt(2);
o__PersonImpl.recordId = rs[0].getInt(3);
o__PersonImpl.ssn = rs[0].getString(4);
o__PersonImpl.firstName = rs[0].getString(5);
o__PersonImpl.middleName = rs[0].getString(6);
o__PersonImpl.lastName = rs[0].getString(7);
o__PersonImpl.streetNumber = rs[0].getString(8);
o__PersonImpl.streetName = rs[0].getString(9);
o__PersonImpl.apartmentNumber = rs[0].getString(10);
o__PersonImpl.city = rs[0].getString(11);
o__PersonImpl.state = rs[0].getString(12);
o__PersonImpl.zip = rs[0].getString(13);
} else {
o__PersonImpl = null;
}
}
public String getMasterId() {
return masterId;
}
static final String masterId = "record_id";
static final String masterIdType = "int";
public DbView[] getViews() {
return views;
}
static DbView[] views = {
new DbView(0,new DbField[]{
new DbField("person", "linkage_role"),
new DbField("person", "entity_id"),
new DbField("person", "record_id"),
new DbField("person", "ssn"),
new DbField("person", "first_name"),
new DbField("person", "middle_name"),
new DbField("person", "last_name"),
new DbField("person", "street_number"),
new DbField("person", "street_name"),
new DbField("person", "apartment_number"),
new DbField("person", "city"),
new DbField("person", "state_code"),
new DbField("person", "zip_code")},
"person", null,new DbField[]{
})};
public Map getIndices() {
return indices;
}
private static Map indices = new HashMap();
static {
Map tableIndices;
}
}