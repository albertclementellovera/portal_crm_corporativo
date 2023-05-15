/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.claro.takeover;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Albert
 */
@Path("tkover")
public class takeover {
    @Context
    private UriInfo context;
    private Connection conn = null;  

    /**
     * Creates a new instance of GenericResource
     */
    public takeover() {
    }
    
    /**
     * Retrieves representation of an instance of com.claro.takeover.GenericResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/json")
    @Path("getJson")
    public Response getJson(@QueryParam("contract") String contract, @QueryParam("customer") String customerid) throws JSONException{
        //TODO return proper representation object
        JSONObject jsonCreateUserResponse = new JSONObject();
                  
        JSONArray jsonextensionInfo1 = new JSONArray();
        jsonCreateUserResponse = new JSONObject();
        jsonCreateUserResponse.put("NewContract","56698");
        jsonCreateUserResponse.put("OldContract",contract);
        jsonextensionInfo1.put(jsonCreateUserResponse);
       
            
           
       return Response.ok(jsonCreateUserResponse.toString())
               .header("Access-Control-Allow-Origin", "*")
               .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD")
               .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }
    /**
     * PUT method for updating or creating an instance of GenericResource
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void putJson(String content) {
        
    }
     public void toDb( )
    {
       try{
           Class.forName("oracle.jdbc.driver.OracleDriver"); 
           String dbURL = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=10.218.41.11)(PORT=3875))(CONNECT_DATA=(SERVICE_NAME=crmuats)))";
           String strUserID = "CRM_AMX_CENAM_FU_PA_UAT";
           String strPassword = "Claro2017";
           conn = DriverManager.getConnection(dbURL,strUserID,strPassword);
           System.out.println("Exito Conexion DB.....");
           
       }catch(SQLException | ClassNotFoundException e){
           System.out.println("ErrorDB: "+e);
       }
    }
}
