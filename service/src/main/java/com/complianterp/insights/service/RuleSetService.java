package com.complianterp.insights.service;
/**
 *
 * @author S0016910852
 */
import java.sql.Connection;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sdk.hana.connectivity.cds.CDSQuery;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSSelectQueryBuilder;
import com.sap.cloud.sdk.hana.connectivity.cds.CDSSelectQueryResult;

import com.sap.cloud.sdk.hana.connectivity.handler.CDSDataSourceHandler;
import com.sap.cloud.sdk.hana.connectivity.handler.DataSourceHandlerFactory;

import com.sap.cloud.sdk.service.prov.api.EntityData;
import com.sap.cloud.sdk.service.prov.api.operations.Query;
import com.sap.cloud.sdk.service.prov.api.operations.Read;
import com.sap.cloud.sdk.service.prov.api.request.QueryRequest;
import com.sap.cloud.sdk.service.prov.api.request.ReadRequest;
import com.sap.cloud.sdk.service.prov.api.response.QueryResponse;
import com.sap.cloud.sdk.service.prov.api.response.ReadResponse;

public class RuleSetService {

	private static Logger logger = LoggerFactory.getLogger(RuleSetService.class);

	@Query(entity = "module", serviceName = "ruleset")
	public QueryResponse findModule(QueryRequest request) {
		try {
			QueryResponse res = QueryResponse.setSuccess().setEntityData(getEntitySet(request)).response();
			return res;
		} catch (Exception e) {
			return null;
		}
	}

	@Read(entity = "module", serviceName = "ruleset")
	public ReadResponse getProposedBooks(ReadRequest readRequest) {
		try {
			ReadResponse readResponse = ReadResponse.setSuccess().setData(readEntity(readRequest)).response();
			return readResponse;
		} catch (Exception e) {
			return null;
		}
	}

	@Query(entity = "subModule", serviceName = "ruleset")
	public QueryResponse findSubModule(QueryRequest request) {
		try {
			QueryResponse res = QueryResponse.setSuccess().setEntityData(getEntitySet(request)).response();
			return res;
		} catch (Exception e) {
			return null;
		}
	}

	@Read(entity = "subModule", serviceName = "ruleset")
	public ReadResponse getSubModule(ReadRequest readRequest) {
		try {
			ReadResponse readResponse = ReadResponse.setSuccess().setData(readEntity(readRequest)).response();
			return readResponse;
		} catch (Exception e) {
			return null;
		}
	}
	private List<EntityData> getEntitySet(QueryRequest queryRequest) {
		String fullQualifiedName = queryRequest.getEntityMetadata().getNamespace() + "." + queryRequest.getEntityMetadata().getName();
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(), queryRequest.getEntityMetadata().getNamespace());
		try {
			CDSQuery cdsQuery = new CDSSelectQueryBuilder(fullQualifiedName).build();
			CDSSelectQueryResult cdsSelectQueryResult = dsHandler.executeQuery(cdsQuery);
			return cdsSelectQueryResult.getResult();
		} catch (Exception e) {
			logger.error("==> Exception while fetching query data from CDS: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private EntityData readEntity(ReadRequest readRequest) throws Exception {
		CDSDataSourceHandler dsHandler = DataSourceHandlerFactory.getInstance().getCDSHandler(getConnection(), readRequest.getEntityMetadata().getNamespace());
		EntityData ed = dsHandler.executeRead(readRequest.getEntityMetadata().getName(), readRequest.getKeys(), readRequest.getEntityMetadata().getElementNames());
		return ed;
	}

	private static Connection getConnection() {
		Connection conn = null;
		Context ctx;
		try {
			ctx = new InitialContext();
			conn = ((DataSource) ctx.lookup("java:comp/env/jdbc/java-hdi-container")).getConnection();
			System.out.println("conn = " + conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

}
