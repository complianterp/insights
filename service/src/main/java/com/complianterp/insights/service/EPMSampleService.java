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
public class EPMSampleService {

	@Query(entity = "SalesOrders", serviceName = "EPMSampleService")
	public QueryResponse getAllSalesOrders(QueryRequest queryRequest) {
		QueryResponse queryResponse = QueryResponse.setSuccess().setEntityData(getEntitySet(queryRequest)).response();
		return queryResponse;
	}

	@Query(entity = "SalesOrderLineItems", serviceName = "EPMSampleService")
	public QueryResponse getAllSOLineItems(QueryRequest queryRequest) {
		QueryResponse queryResponse = QueryResponse.setSuccess().setEntityData(getEntitySet(queryRequest)).response();
		return queryResponse;
	}

	@Query(entity = "SalesOrderLineItems", serviceName = "EPMSampleService", sourceEntity = "SalesOrders")
	public QueryResponse getSOLineItemsForSO(QueryRequest queryRequest) {
		QueryResponse queryResponse = null;
		EntityData SOEntity;
		try {
			String sourceEntityName = queryRequest.getSourceEntityName();
			//Read SalesOrders to check if the passed SalesOrderID exists
			if (sourceEntityName.equals("SalesOrders")) {
				SOEntity = readSalesOrder(queryRequest.getSourceKeys());
				if (SOEntity == null) {
					ErrorResponse errorResponse = ErrorResponse.getBuilder()
							.setMessage("Parent SalesOrder does not exist").setStatusCode(401).response();
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

	@Create(entity = "SalesOrders", serviceName = "EPMSampleService")
	public CreateResponse createSalesOrder(CreateRequest createRequest) {
		CreateResponse createResponse = CreateResponse.setSuccess().setData(createEntity(createRequest)).response();
		return createResponse;
	}

	@Create(entity = "SalesOrderLineItems", serviceName = "EPMSampleService", sourceEntity = "SalesOrders")
	public CreateResponse createSalesOrderLineItemForSalesOrder(CreateRequest createRequest) {
		CreateResponse createResponse = null;
		EntityData SOEntity;
		try {
			String sourceEntityName = createRequest.getSourceEntityName();
			//Read SalesOrders to check if the passed SalesOrderID exists
			if (sourceEntityName.equals("SalesOrders")) {
				SOEntity = readSalesOrder(createRequest.getSourceKeys());
				if (SOEntity == null) {
					ErrorResponse errorResponse = ErrorResponse.getBuilder()
							.setMessage("Parent SalesOrder does not exist").setStatusCode(401).response();
					createResponse = CreateResponse.setError(errorResponse);
				} else {
					// You can further validate that the payload data contains the SalesOrderID same as that in the URL.
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

	@Read(entity = "SalesOrders", serviceName = "EPMSampleService")
	public ReadResponse getSalesOrder(ReadRequest readRequest) {
		ReadResponse readResponse = ReadResponse.setSuccess().setData(readEntity(readRequest)).response();
		return readResponse;
	}

	@Read(entity = "SalesOrderLineItems", serviceName = "EPMSampleService")
	public ReadResponse getSOItem(ReadRequest readRequest) {
		ReadResponse readResponse = ReadResponse.setSuccess().setData(readEntity(readRequest)).response();
		return readResponse;
	}

	@Update(entity = "SalesOrders", serviceName = "EPMSampleService")
	public UpdateResponse updateSalesOrder(UpdateRequest updateRequest) {
		updateEntity(updateRequest);
		UpdateResponse updateResponse = UpdateResponse.setSuccess().response();
		return updateResponse;
	}

	@Delete(entity = "SalesOrders", serviceName = "EPMSampleService")
	public DeleteResponse deleteSalesOrder(DeleteRequest deleteRequest) {
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

	private static Logger logger = LoggerFactory.getLogger(EPMSampleService.class);

	private List<EntityData> getEntitySet(QueryRequest queryRequest) {
		String fullQualifiedName = queryRequest.getEntityMetadata().getNamespace() + "."
				+ queryRequest.getEntityMetadata().getName();
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(),
				queryRequest.getEntityMetadata().getNamespace());
		try {
			CDSQuery cdsQuery = new CDSSelectQueryBuilder(fullQualifiedName).orderBy("SalesOrderID", false).build();
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

	private List<EntityData> getSOItemsForSO(Map<String, Object> SalesOrderID) {
		String fullQualifiedName = "EPMSample.SalesOrderLineItems";
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(),
				"EPMSample");
		try {
			CDSQuery cdsQuery = new CDSSelectQueryBuilder(fullQualifiedName)
					.where(new ConditionBuilder().columnName("SalesOrderID")
							.EQ(SalesOrderID.get("SalesOrderID").toString()).build())
					.orderBy("SOLineItemID", false).build();
			CDSSelectQueryResult cdsSelectQueryResult = dsHandler.executeQuery(cdsQuery);
			return cdsSelectQueryResult.getResult();
		} catch (CDSException e) {
			e.printStackTrace();
		}
		return null;
	}

	private EntityData readSalesOrder(Map<String, Object> SalesOrderID) {
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(),
				"EPMSample");
		List<String> properties = Arrays.asList("SalesOrderID");
		EntityData ed = null;
		try {
			ed = dsHandler.executeRead("SalesOrders", SalesOrderID, properties);
		} catch (CDSException e) {
			//Handle exception here
			e.printStackTrace();
		}
		return ed;
	}

}
