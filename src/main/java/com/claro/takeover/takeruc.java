/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.claro.takeover;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Albert
 */
@Path("tkruc")
public class takeruc {
    @Context
    private UriInfo context;
    private Connection conn = null;  

    public takeruc() {}
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/json")
    @Path("getJson")
    public Response getJson(@QueryParam("searchid") String customerid, @QueryParam("searchopcion") String cust)
    {
        JSONObject jsonCreateUserResponse = new JSONObject();

        String consulta2 = "select count (customer_id) reg from VIEW_SEARCH_CRM vw\n" +
        "INNER JOIN TBL_CRM_SUBSCRIPTIONS SU ON SU.SUBSCRIPTION_ID = vw.SUBSCRIPTION_ID\n" +
        "where party_id in (select party_id from TBL_CM_PARTY_IDENTIFICATIONS where id_value ='"+customerid+"') and SU.STATUS = 2 ";

        String consulta3 = "select VW.subscription_name plan, SU.external_id contract, CU.external_id customer, VW.party_name full_name, VW.customer_number from VIEW_SEARCH_CRM vw\n" +
        "INNER JOIN TBL_CRM_SUBSCRIPTIONS SU ON SU.SUBSCRIPTION_ID = vw.SUBSCRIPTION_ID\n" +
        "INNER JOIN TBL_CRM_CUSTOMERS CU ON CU.PARTY_ID = vw.PARTY_ID\n" +
        "where VW.party_id in (select party_id from TBL_CM_PARTY_IDENTIFICATIONS where id_value ='"+customerid+"') and SU.STATUS = 2  AND VW.CUSTOMER_NUMBER = CU.CUSTOMER_NUMBER order by 5";

        try
        {
            toDb();

            PreparedStatement pstm = conn.prepareStatement(consulta3);
            ResultSet res = pstm.executeQuery();

            PreparedStatement pstm1 = conn.prepareStatement(consulta2);
            ResultSet res1 = pstm1.executeQuery();

            JSONArray jsonextensionInfo = new JSONArray();
            JSONArray jsonextensionInfo1 = new JSONArray();
            JSONObject jsonKey = new JSONObject();
            String custnu = null;
            String custn = null;
            String full_name = null;
            res1.next();
            int fl = res1.getInt("reg");
            int flag = 0;
            fl=fl+1;
            while(res.next())
            {   
                if(flag > 0)
                {
                    if(!custnu.equals(res.getString("customer_number")) || flag > fl)
                    {
                          jsonKey = new JSONObject();
                          jsonKey.put("customer",custn);
                          jsonKey.put("name",full_name);
                          jsonKey.put("suscriber",custnu);
                          jsonKey.put("services",jsonextensionInfo1);
                          jsonextensionInfo.put(jsonKey);
                        jsonextensionInfo1 = new JSONArray();
                    }
                }
                flag++;
                JSONObject jsonKey1 = new JSONObject();
                jsonKey1 = new JSONObject();
                jsonKey1.put("value",res.getString("PLAN"));
                jsonKey1.put("contract",res.getString("CONTRACT"));
                jsonextensionInfo1.put(jsonKey1);
                custnu = res.getString("customer_number");
                custn = res.getString("CUSTOMER");
                full_name = res.getString("FULL_NAME");
            }
            jsonKey = new JSONObject();
            jsonKey.put("customer",custn);
            jsonKey.put("name",full_name);
            jsonKey.put("suscriber",custnu);
            jsonKey.put("services",jsonextensionInfo1);
            jsonextensionInfo.put(jsonKey);      
            jsonCreateUserResponse.accumulate("ruc",customerid);
            jsonCreateUserResponse.accumulate("suscribers",jsonextensionInfo);
           
            res.close();
            conn.commit();
            conn.close(); 
            
        }catch(Exception e)
        {
            System.out.println("Error - 1 " +e);
        }
       
       return Response.ok(jsonCreateUserResponse.toString())
               .header("Access-Control-Allow-Origin", "*")
               .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD")
               .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }
    
    public void toDb( )
    {
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver"); 
            String dbURL = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS = (PROTOCOL = TCP)(HOST = 10.218.41.17)(PORT = 3875))(CONNECT_DATA =(SERVICE_NAME = CRMUATS)))";
            String strUserID = "CRM_AMX_CENAM_CR_UAT_FU91";
            String strPassword = "Claro2017";
            conn = DriverManager.getConnection(dbURL,strUserID,strPassword);
        }catch(SQLException | ClassNotFoundException e)
        {
            System.out.println("ErrorDB: "+e);
        }
    } 
}
