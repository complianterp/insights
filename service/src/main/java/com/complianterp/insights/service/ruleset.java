package com.complianterp.insights.service;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSQuery;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSSelectQueryBuilder;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSSelectQueryResult;
import com.sap.cloud.sdk.hana.connectivity.cds.ConditionBuilder;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSException;
import com.sap.cloud.sdk.hana.connectivity.handler.CDSDataSourceHandler;
import com.sap.cloud.sdk.hana.connectivity.handler.DataSourceHandlerFactory;
import com.sap.cloud.sdk.service.prov.api.EntityData;
import com.sap.cloud.sdk.service.prov.api.operations.Create;
import com.sap.cloud.sdk.service.prov.api.operations.Delete;
import com.sap.cloud.sdk.service.prov.api.operations.Query;
import com.sap.cloud.sdk.service.prov.api.operations.Read;
import com.sap.cloud.sdk.service.prov.api.operations.Update;
import com.sap.cloud.sdk.service.prov.api.request.CreateRequest;
import com.sap.cloud.sdk.service.prov.api.request.DeleteRequest;
import com.sap.cloud.sdk.service.prov.api.request.QueryRequest;
import com.sap.cloud.sdk.service.prov.api.request.ReadRequest;
import com.sap.cloud.sdk.service.prov.api.request.UpdateRequest;
import com.sap.cloud.sdk.service.prov.api.response.CreateResponse;
import com.sap.cloud.sdk.service.prov.api.response.DeleteResponse;
import com.sap.cloud.sdk.service.prov.api.response.ErrorResponse;
import com.sap.cloud.sdk.service.prov.api.response.QueryResponse;
import com.sap.cloud.sdk.service.prov.api.response.ReadResponse;
import com.sap.cloud.sdk.service.prov.api.response.UpdateResponse;

/**
 *
 * @author S0016910852
 */
public class ruleset {

	@Query(entity = "modules", serviceName = "ruleset")
	public QueryResponse getAllModules(QueryRequest queryRequest) {
		QueryResponse queryResponse = QueryResponse.setSuccess().setEntityData(getEntitySet(queryRequest)).response();
		return queryResponse;
	}

	@Query(entity = "subModules", serviceName = "ruleset")
	public QueryResponse getAllSubModules(QueryRequest queryRequest) {
		QueryResponse queryResponse = QueryResponse.setSuccess().setEntityData(getEntitySet(queryRequest)).response();
		return queryResponse;
	}

	@Query(entity = "subModules", serviceName = "ruleset", sourceEntity = "modules")
	public QueryResponse getSubModulesForModule(QueryRequest queryRequest) {
		QueryResponse queryResponse = null;
		EntityData ModuleEntity;
		try {
			String sourceEntityName = queryRequest.getSourceEntityName();
			//Read modules to check if the passed moduleId exists
			if (sourceEntityName.equals("modules")) {
				ModuleEntity = readModule(queryRequest.getSourceKeys());
				if (ModuleEntity == null) {
					ErrorResponse errorResponse = ErrorResponse.getBuilder()
							.setMessage("Parent module does not exist").setStatusCode(401).response();							
					queryResponse = QueryResponse.setError(errorResponse);
				} else {
					queryResponse = QueryResponse.setSuccess()
							.setEntityData(getSubModDetailsForModule(queryRequest.getSourceKeys())).response();
				}
			}
		} catch (Exception e) {
			logger.error("==> Exception fetching sub-modules for a module from CDS: " + e.getMessage());
			queryResponse = QueryResponse
					.setError(ErrorResponse.getBuilder().setMessage(e.getMessage()).setCause(e).response());
		}
		return queryResponse;
	}

	@Create(entity = "modules", serviceName = "ruleset")
	public CreateResponse createModule(CreateRequest createRequest) {
		CreateResponse createResponse = CreateResponse.setSuccess().setData(createEntity(createRequest)).response();
		return createResponse;
	}

	@Create(entity = "subModules", serviceName = "ruleset", sourceEntity = "modules")
	public CreateResponse createSubModuleForModule(CreateRequest createRequest) {
		CreateResponse createResponse = null;
		EntityData ModuleEntity;
		try {
			String sourceEntityName = createRequest.getSourceEntityName();
			//Read modules to check if the passed moduleId exists
			if (sourceEntityName.equals("modules")) {
				ModuleEntity = readModule(createRequest.getSourceKeys());
				if (ModuleEntity == null) {
					ErrorResponse errorResponse = ErrorResponse.getBuilder()
							.setMessage("Parent module does not exist").setStatusCode(401).response();							
					createResponse = CreateResponse.setError(errorResponse);
				} else {
					// You can further validate that the payload data contains the moduleId same as that in the URL.
					// For that you can use the createRequest.getData() method and further find the specific property's value
					createResponse = CreateResponse.setSuccess().setData(createEntity(createRequest)).response();
				}
			}
		} catch (Exception e) {
			logger.error("==> Exception while creating a sub-module for a module in CDS: " + e.getMessage());
			createResponse = CreateResponse
					.setError(ErrorResponse.getBuilder().setMessage(e.getMessage()).setCause(e).response());
		}
		return createResponse;
	}

	@Read(entity = "modules", serviceName = "ruleset")
	public ReadResponse getModule(ReadRequest readRequest) {
		ReadResponse readResponse = ReadResponse.setSuccess().setData(readEntity(readRequest)).response();
		return readResponse;
	}

	@Read(entity = "subModules", serviceName = "ruleset")
	public ReadResponse getSubModule(ReadRequest readRequest) {
		ReadResponse readResponse = ReadResponse.setSuccess().setData(readEntity(readRequest)).response();
		return readResponse;
	}

	//@Read(entity = "subModules", serviceName = "ruleset", sourceEntity = "modules")
	//public ReadResponse readSalesOrderLineItem(ReadRequest readRequest) {
		//try {
		//	String entitySetName = readRequest.getEntityName();						// Retrieve the name of the entity set from the ReadRequest object
		//	Map<String, Object> keys = readRequest.getKeys();						// Retrieve the keys of the entity from the ReadRequest object
		//	String sourceEntitySetName = readRequest.getSourceEntityName();			// Retrieve the name of the parent entity specified in the navigation
		//	Map<String, Object> sourceEntityKeys = readRequest.getSourceKeys();		// Retrieve the keys of the parent entity specified in the navigation
			// Add your implementation of the read operation here
		//	EntityData entityData = getSubModuleForModule(entitySetName, sourceEntitySetName, sourceEntityKeys);
			// Return an instance of ReadResponse in case of success
			//return ReadResponse.setSuccess().setData(entityData).response();
		//} catch (Exception e) {
		// Return an instance of ReadResponse containing the error in case of failure
		//	ErrorResponse errorResponse = ErrorResponse.getBuilder()
		//	.setMessage(e.getMessage())
		//	.setStatusCode(INTERNAL_SERVER_ERROR)
		//	.setCause(e)
		//	.response();
		//return ReadResponse.setError(errorResponse);
		//return "";
		//}
	//}


	@Update(entity = "modules", serviceName = "ruleset")
	public UpdateResponse updateModule(UpdateRequest updateRequest) {
		updateEntity(updateRequest);
		UpdateResponse updateResponse = UpdateResponse.setSuccess().response();
		return updateResponse;
	}

	@Delete(entity = "modules", serviceName = "ruleset")
	public DeleteResponse deletemodule(DeleteRequest deleteRequest) {
		deleteEntity(deleteRequest);
		DeleteResponse deleteResponse = DeleteResponse.setSuccess().response();
		return deleteResponse;
	}

	private static Connection getConnection() {
		Connection conn = null;
		Context ctx;
		try {
			ctx = new InitialContext();
			conn = ((DataSource) ctx.lookup("java:comp/env/jdbc/java-hdi-container")).getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	private static Logger logger = LoggerFactory.getLogger(ruleset.class);

	private List<EntityData> getEntitySet(QueryRequest queryRequest) {
		String fullQualifiedName = queryRequest.getEntityMetadata().getNamespace() + "."
				+ queryRequest.getEntityMetadata().getName();
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(),
				queryRequest.getEntityMetadata().getNamespace());
		try {
			CDSQuery cdsQuery = new CDSSelectQueryBuilder(fullQualifiedName).orderBy("moduleId", false).build();
			CDSSelectQueryResult cdsSelectQueryResult = dsHandler.executeQuery(cdsQuery);
			return cdsSelectQueryResult.getResult();
		} catch (CDSException e) {
			logger.error("==> Exception while fetching query data from CDS: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private EntityData createEntity(CreateRequest createRequest) {
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(),
				createRequest.getEntityMetadata().getNamespace());
		EntityData ed = null;
		try {
			ed = dsHandler.executeInsert(createRequest.getData(), true);
		} catch (CDSException e) {
			//Handle exception here
			e.printStackTrace();
		}
		return ed;
	}

	private EntityData readEntity(ReadRequest readRequest) {
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(),
				readRequest.getEntityMetadata().getNamespace());
		EntityData ed = null;
		try {
			ed = dsHandler.executeRead(readRequest.getEntityMetadata().getName(), readRequest.getKeys(),
					readRequest.getEntityMetadata().getElementNames());
		} catch (CDSException e) {
			//Handle exception here
			e.printStackTrace();
		}
		return ed;
	}

	private void updateEntity(UpdateRequest updateRequest) {
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(),
				updateRequest.getEntityMetadata().getNamespace());
		try {
			dsHandler.executeUpdate(updateRequest.getData(), updateRequest.getKeys(), false);
		} catch (CDSException e) {
			//Handle exception here
			e.printStackTrace();
		}
	}

	private void deleteEntity(DeleteRequest deleteRequest) {
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(),
				deleteRequest.getEntityMetadata().getNamespace());
		try {
			dsHandler.executeDelete(deleteRequest.getEntityMetadata().getName(), deleteRequest.getKeys());
		} catch (CDSException e) {
			//Handle exception here
			e.printStackTrace();
		}
	}			
				
	private List<EntityData> getSubModDetailsForModule(Map<String, Object> moduleId) {
		String fullQualifiedName = "ruleset.subModules";
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(),"ruleset");
		try {
			CDSQuery cdsQuery = new CDSSelectQueryBuilder(fullQualifiedName)
					.where(new ConditionBuilder().columnName("moduleId")
							.EQ(moduleId.get("moduleId").toString()).build())
					.orderBy("subModuleId", false).build();
			CDSSelectQueryResult cdsSelectQueryResult = dsHandler.executeQuery(cdsQuery);
			return cdsSelectQueryResult.getResult();
		} catch (CDSException e) {
			e.printStackTrace();
		}
		return null;
	}



	private EntityData readModule(Map<String, Object> moduleId) {
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(),"ruleset");
		List<String> properties = Arrays.asList("moduleId");
		EntityData ed = null;
		try {
			ed = dsHandler.executeRead("modules", moduleId, properties);
		} catch (CDSException e) {
			//Handle exception here
			e.printStackTrace();
		}
		return ed;
	}

}
