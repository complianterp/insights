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
public class RuleSetService {

	@Query(entity = "modules", serviceName = "RuleSetService")
	public QueryResponse getAllmodules(QueryRequest queryRequest) {
		QueryResponse queryResponse = QueryResponse.setSuccess().setEntityData(getEntitySet(queryRequest)).response();
		return queryResponse;
	}

	@Query(entity = "subModules", serviceName = "RuleSetService")
	public QueryResponse getAllSOLineItems(QueryRequest queryRequest) {
		QueryResponse queryResponse = QueryResponse.setSuccess().setEntityData(getEntitySet(queryRequest)).response();
		return queryResponse;
	}

	@Query(entity = "subModules", serviceName = "RuleSetService", sourceEntity = "modules")
	public QueryResponse getSOLineItemsForSO(QueryRequest queryRequest) {
		QueryResponse queryResponse = null;
		EntityData SOEntity;
		try {
			String sourceEntityName = queryRequest.getSourceEntityName();
			//Read modules to check if the passed moduleId exists
			if (sourceEntityName.equals("modules")) {
				SOEntity = readmodule(queryRequest.getSourceKeys());
				if (SOEntity == null) {
					ErrorResponse errorResponse = ErrorResponse.getBuilder()
							.setMessage("Parent module does not exist").setStatusCode(401).response();
					queryResponse = QueryResponse.setError(errorResponse);
				} else {
					queryResponse = QueryResponse.setSuccess()
							.setEntityData(getSOItemsForSO(queryRequest.getSourceKeys())).response();
				}
			}
		} catch (Exception e) {
			logger.error("==> Exception fetching SOItems for a SO from CDS: " + e.getMessage());
			queryResponse = QueryResponse
					.setError(ErrorResponse.getBuilder().setMessage(e.getMessage()).setCause(e).response());
		}
		return queryResponse;
	}

	@Create(entity = "modules", serviceName = "RuleSetService")
	public CreateResponse createmodule(CreateRequest createRequest) {
		CreateResponse createResponse = CreateResponse.setSuccess().setData(createEntity(createRequest)).response();
		return createResponse;
	}

	@Create(entity = "subModules", serviceName = "RuleSetService", sourceEntity = "modules")
	public CreateResponse createmoduleLineItemFormodule(CreateRequest createRequest) {
		CreateResponse createResponse = null;
		EntityData SOEntity;
		try {
			String sourceEntityName = createRequest.getSourceEntityName();
			//Read modules to check if the passed moduleId exists
			if (sourceEntityName.equals("modules")) {
				SOEntity = readmodule(createRequest.getSourceKeys());
				if (SOEntity == null) {
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
			logger.error("==> Exception while creating a SOItem for a SO in CDS: " + e.getMessage());
			createResponse = CreateResponse
					.setError(ErrorResponse.getBuilder().setMessage(e.getMessage()).setCause(e).response());
		}
		return createResponse;
	}

	@Read(entity = "modules", serviceName = "RuleSetService")
	public ReadResponse getmodule(ReadRequest readRequest) {
		ReadResponse readResponse = ReadResponse.setSuccess().setData(readEntity(readRequest)).response();
		return readResponse;
	}

	@Read(entity = "subModules", serviceName = "RuleSetService")
	public ReadResponse getSOItem(ReadRequest readRequest) {
		ReadResponse readResponse = ReadResponse.setSuccess().setData(readEntity(readRequest)).response();
		return readResponse;
	}

	@Update(entity = "modules", serviceName = "RuleSetService")
	public UpdateResponse updatemodule(UpdateRequest updateRequest) {
		updateEntity(updateRequest);
		UpdateResponse updateResponse = UpdateResponse.setSuccess().response();
		return updateResponse;
	}

	@Delete(entity = "modules", serviceName = "RuleSetService")
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

	private static Logger logger = LoggerFactory.getLogger(RuleSetService.class);

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

	private List<EntityData> getSOItemsForSO(Map<String, Object> moduleId) {
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

	private EntityData readmodule(Map<String, Object> moduleId) {
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
