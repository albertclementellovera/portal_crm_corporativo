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
import javax.ws.rs.PUT;
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
@Path("ordereom")
public class ordereom {
    
     @Context
    private UriInfo context;
    private Connection conn = null;
    private Connection conndb = null;
    Crm_logs archivo = new Crm_logs();

    
    public ordereom() {}
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/json")
    @Path("getinfo")
    public Response getJson(@QueryParam("search") String p_search, @QueryParam("value") String p_value) {
        
        JSONObject jsonCreateUserResponse = new JSONObject();
        try{
            ResultSet res = null;
            JSONArray arreom = new JSONArray();
            switch(Integer.parseInt(p_search)){
            case 1:
                JSONObject eomruc;
                res = querysdata(7,p_value);
                while(res.next()){
                    eomruc = new JSONObject();
                    eomruc.put("customerid",res.getString("CUSTOMERID"));
                    eomruc.put("contract",res.getString("CONTRACT"));
                    eomruc.put("orderEom",res.getString("ORDEREOM"));
                    eomruc.put("action",res.getString("ACTION"));
                    eomruc.put("date",res.getDate("INSERTDATE"));
                    eomruc.put("customerid",res.getString("CUSTOMERID"));
                    arreom.put(eomruc);
                }
                break;
            case 2:
                JSONObject eomcustomer;
                res = querysdata(8,p_value);
                while(res.next()){
                    eomcustomer = new JSONObject();
                    eomcustomer.put("customerid",res.getString("CUSTOMERID"));
                    eomcustomer.put("contract",res.getString("CONTRACT"));
                    eomcustomer.put("orderEom",res.getString("ORDEREOM"));
                    eomcustomer.put("action",res.getString("ACTION"));
                    eomcustomer.put("date",res.getDate("INSERTDATE"));
                    eomcustomer.put("customerid",res.getString("CUSTOMERID"));
                    arreom.put(eomcustomer);
                }
                break;
                case 3:
                JSONObject eomcontract;
                 res = querysdata(9,p_value);
                 while(res.next()){
                    eomcontract = new JSONObject();
                    eomcontract.put("customerid",res.getString("CUSTOMERID"));
                    eomcontract.put("contract",res.getString("CONTRACT"));
                    eomcontract.put("orderEom",res.getString("ORDEREOM"));
                    eomcontract.put("action",res.getString("ACTION"));
                    eomcontract.put("date",res.getDate("INSERTDATE"));
                    eomcontract.put("customerid",res.getString("CUSTOMERID"));
                    arreom.put(eomcontract);
                }
                break;
            default:
                 break;
             }
             conn.close();
            jsonCreateUserResponse.put("values",arreom);
            
        }catch(Exception e){
            System.out.println(e);
        }
        return Response.ok(jsonCreateUserResponse.toString())
               .header("Access-Control-Allow-Origin", "*")
               .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD")
               .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
        }
    
     @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void putJson(String content) {
        
    }
        
     public void toDbx03( )
    {
       try{
           Class.forName("oracle.jdbc.driver.OracleDriver"); 
           String dbURL = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=172.24.5.213)(PORT=3924))(CONNECT_DATA=(SERVER=dedicated)(SERVICE_NAME=BSDBX03)))";
           String strUserID = "INTERFAZ";
           String strPassword = "INTERFAZ";
           conn = DriverManager.getConnection(dbURL,strUserID,strPassword);
           System.out.println("Exito Conexion DB.....");
           
       }catch(SQLException | ClassNotFoundException e){
           System.out.println("ErrorDB: "+e);
       }
    }
     
     public ResultSet querysdata(int p_data1, String p_data2) throws SQLException{
         ResultSet res = null;
         PreparedStatement pstm = null;
         try{
             toDbx03();             
             switch(p_data1){
            case 7:
                 String eomruc = "SELECT * FROM TBL_CRM_ORDEREOM WHERE NODOCUMENT = '"+p_data2+"'";
                 pstm = conn.prepareStatement(eomruc);
                 res = pstm.executeQuery();
                 break;
            case 8:
                 String eomcustomer = "SELECT * FROM TBL_CRM_ORDEREOM WHERE CUSTOMERID = '"+p_data2+"'";
                 pstm = conn.prepareStatement(eomcustomer);
                 res = pstm.executeQuery();
                 break;
            case 9:
                 String eomcontract = "SELECT * FROM TBL_CRM_ORDEREOM WHERE CONTRACT = '"+p_data2+"'";
                 pstm = conn.prepareStatement(eomcontract);
                 res = pstm.executeQuery();
                 break;
                 
             default:
                 break;
             }
         }catch(Exception e){
             System.out.println(e);
         }
         return res;
     }
}

