package es.nortia_in.orm.enhance

import com.avaje.ebean.Ebean
import com.avaje.ebean.EbeanServer

import es.nortia_in.orm.directory.DomainDirectory;
import es.nortia_in.orm.gorm.EBeanGormException;
import es.nortia_in.orm.gorm.GormLikeMethods
import es.nortia_in.orm.gorm.query.GormLikeCriteria;
import groovy.lang.MetaClass;

/**
 * Enhance a given domain class to add GORM-like methods such as find, delete, findAllBy, list, etc.
 * @author angel
 *
 */
class GormLikeMethodsEnhancer implements DomainClassEnhancer{

	/**
	 * The domain directory for domain class quering
	 */
	DomainDirectory domainDirectory

	/**
	 * Template method to execute domain entity save method
	 * @param clazz the domain entity class
	 * @param entity the entity to be saved
	 * @return the save result
	 */
	protected def doSave(Class clazz, Object entity) {
		GormLikeMethods.save(getEbeanServer(clazz), entity)
	}

	/**
	 * Template method to execute domain entity refresh method
	 * @param clazz the domain entity class
	 * @param entity the entity to be refreshed
	 * @return the refresh method result
	 */
	protected def doRefresh(Class clazz, Object entity) {
		GormLikeMethods.refresh(getEbeanServer(clazz), entity)
	}

	/**
	 * Template method to execute domain entity delete method
	 * @param clazz the domain class
	 * @param entity the entity to be deleted
	 * @return the delete result
	 */
	protected def doDelete(Class clazz, Object entity) {
		GormLikeMethods.delete(getEbeanServer(clazz), entity, clazz)
	}

	/**
	 * Template method to execute domain entity list method
	 * @param clazz the domain class to be listed
	 * @param args the list arguments
	 * @return the list result
	 */
	protected def doList(Class clazz, Object args) {
		GormLikeMethods.list(getEbeanServer(clazz), clazz, args)
	}

	/**
	 * Template method to execute domain entity get method
	 * @param clazz the domain class to be retrieved
	 * @param id the entity id
	 * @return the get result
	 */
	protected def doGet(Class clazz, Object id) {
		GormLikeMethods.get(getEbeanServer(clazz), clazz, id);
	}

	/**
	 * Template method to execute domain entity count method
	 * @param clazz the entity class
	 * @return the count result
	 */
	protected def doCount(Class clazz) {
		GormLikeMethods.count(getEbeanServer(clazz), clazz);
	}

	/**
	 * Template method to create a query criteria based on given domain class
	 * @param clazz the domain class
	 * @return the created criteria
	 */
	protected def doCreateQuery(Class clazz) {
		GormLikeMethods.createQuery(getEbeanServer(clazz), clazz);
	}

	/**
	 * Template method to create a {@link GormLikeCriteria} over given domain class
	 * @param clazz the domain class for criteria creation
	 * @return the created criteria
	 */
	protected def doCreateCriteria(Class clazz) {
		return new GormLikeCriteria(clazz, getEbeanServer(clazz))
	}

	/**
	 * Template method to execute a GORM-Like withCriteria method.
	 * @param clazz the domain class for criteria creation
	 * @param c the criteria definition
	 * @return the criteria result
	 */
	protected def doWithCriteria(Class clazz, Closure c) {
		def criteria = doCreateCriteria(clazz)
		assert criteria

		//Execute the criteria
		return criteria.list(c)
	}

	/**
	 * Template method to execute any given dynamic query method such as findAllByXXX or findByXX
	 * @param clazz the domain class to be queried
	 * @param methodName the dynamic method name
	 * @param args the method arguments
	 * @return the dynamic method result
	 */
	protected def doDynamicMethod(Class clazz, String methodName, Object args) {
		GormLikeMethods.dynamicMethod(getEbeanServer(clazz), clazz, methodName, args)
	}


	/**
	 * Template method to execute isDirty() Gorm-like method over given entity
	 * @param object the entity to be checked
	 * @return true if entity is dirty, false otherwise
	 */
	protected boolean doIsDirty(Object object, String propertyName) {

		if (!propertyName) {
			return GormLikeMethods.isDirty(object)
		}

		return GormLikeMethods.isDirtyProperty(object, propertyName)
	}

	/**
	 * Template method to execute isSynch() Gorm-like method over given entity
	 * @param object the entity to be checked
	 * @return true if entity is synch with database, false otherwise
	 */
	protected boolean doIsSynch(Object object) {
		return GormLikeMethods.isSynch(object)
	}
	
	/**
	* Template method to execute isNew() Gorm-like method over given entity
	* @param object the entity to be checked
	* @return true if entity is new and not persisted yet, false otherwise
	*/
   protected boolean doIsNew(Object object) {
	   return GormLikeMethods.isNew(object)
   }

	/**
	 * Template method to execute getDirtyPropertyNames() GORM-like method over given entity
	 * @param object the entity to be checked
	 * @return the dirty properties collection
	 */
	protected def doGetDirtyPropertyNames(Object object) {
		GormLikeMethods.getDirtyPropertyNames(object)
	}


	/**
	 * Template method to execute getPersistentValue() GORM-like method over given entity.
	 * Retrieves old property value if  was changed
	 * @param object the entity whose old property value is wanted
	 * @param propertyName the property name to be retrieved
	 * @return the persisted value in database or null if is not dirty
	 */
	protected def doGetPersistentValue(Object object, String propertyName) {
		GormLikeMethods.getPersistentValue(object, propertyName)
	}
	/**
	 * Returns the EbeanServer corresponding to persistence unit of class. If
	 * the class has <code>null</code> as persistence unit property, returns
	 * the default EbeanServer if exists.
	 * @param clazz the domain class
	 * @return the EbeanServer for persistence unit of class
	 * @throws EBeanGormException if the EbeanServer can't be found
	 */
	def getEbeanServer(Class clazz) {

		assert clazz

		//Retrieve class persistence unit
		String persistenceUnit = domainDirectory?.getPersistenceUnit(clazz)

		EbeanServer ebeanServer
		try {
			ebeanServer = Ebean.getServer(persistenceUnit)
		} catch (RuntimeException e) {
			throw new EBeanGormException("Error accessing ${persistenceUnit} EbeanServer: ${e.message}", e)
		}
		if (!ebeanServer) {
			throw new EBeanGormException("Not ${persistenceUnit} EbeanServer found")
		}
		return ebeanServer
	}

	@Override
	public void enhance(MetaClass metaClass, Class clazz) {

		assert clazz
		assert metaClass != null


		//Save method
		metaClass.save = {
			def entity = delegate
			doSave(clazz, entity)
		}

		//Refresh method
		metaClass.refresh =  {
			def entity = delegate
			doRefresh(clazz, entity)
		}

		//Delete method
		metaClass.delete = {
			def entity = delegate
			doDelete(clazz, entity)
		}

		//List
		metaClass.static.list = {args ->
			doList(clazz, args)
		}

		//Get
		metaClass.static.get = {id ->
			doGet(clazz, id)
		}

		//Count
		metaClass.static.count = { doCount(clazz) }

		// Create ebean query
		metaClass.static.createQuery = { doCreateQuery(clazz) }

		//Create criteria
		metaClass.static.createCriteria = { doCreateCriteria(clazz) }

		//With criteria
		metaClass.static.withCriteria = {closure ->
			doWithCriteria(clazz, closure)
		}

		//isDirty
		metaClass.isDirty = {prop->
			doIsDirty(delegate, prop)
		}
		metaClass.isSynch = { doIsSynch(delegate) }
		metaClass.isNew = { doIsNew(delegate) }

		//getDirtyPropertyNames
		metaClass.getDirtyPropertyNames = { doGetDirtyPropertyNames(delegate) }

		//getPersistentValue
		metaClass.getPersistentValue = {prop ->
			doGetPersistentValue(delegate, prop)
		}

		//Any other dynamic method
		metaClass.static.methodMissing = {methodName, args ->
			doDynamicMethod(clazz, methodName, args)
		}

	}


}
