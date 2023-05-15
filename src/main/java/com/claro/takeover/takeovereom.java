/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.claro.takeover;

import static com.claro.takeover.agendamiento_tecnico.getservicesregistry;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Albert
 */
@Path("eomtakeover")
public class takeovereom {
    @Context
    private UriInfo context;
    private Connection conn = null;
    Crm_logs archivo = new Crm_logs();

    /**
     * Creates a new instance of GenericResource
     */
    public takeovereom() {
        
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("take")
    public Response takeover(@QueryParam("customer") String p_customer, @QueryParam("contract") String p_contract) throws JSONException{
        
        //archivo.crearLog("TakeOver: "+"\n NewCUSTOMER: "+p_customer+"\n OldCONTRACT: "+p_contract);
        JSONObject jsonCreateUserResponse = new JSONObject();
        JSONObject jsonCreateUserResponse2 = new JSONObject();
        String orderType = null;
        String bscsReason = null;
        ResultSet res = null;
        String p_user = "eccventas";
        
        try { 
                                  
            URL url = new URL("http://172.17.224.150:7060/cwf/om/v1/order");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            String regis = getservicesregistry("http://172.17.224.150:7060/cwf/sr/v1/product/?fields=productOffering,comprisedOf,id,name,activeStartDates,productCharacteristics,services,productRelationships&relatedParties.role=Customer&relatedParties.reference="+p_customer+"&relatedEntities.role=Contract&relatedEntities.reference="+p_contract);
            String[] parts = regis.split("/");
            archivo.crearLog("2-SERVICE REGISTRY: "+regis+"\n CUSTOMER: "+p_customer+"\n CONTRACT: "+p_contract);
            System.out.println(regis);
            
            System.out.println("largo: "+parts.length);
            
            Date day = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(day);
            cal.add(Calendar.YEAR, 1);
            Date nuevaFecha = cal.getTime();
            DateFormat Formato = new SimpleDateFormat("yyyy-MM-dd");
            System.out.println("Fecha: "+Formato.format(day));
            System.out.println("Fecha: "+Formato.format(nuevaFecha));
            
            jsonCreateUserResponse.put("createdDate",Formato.format(day)+"T00:00:00.000Z");
            jsonCreateUserResponse.put("createdBy",p_user);
            jsonCreateUserResponse.put("version",1);
            jsonCreateUserResponse.put("description","Postsale - Contract - TakeOver");
            jsonCreateUserResponse.put("requestedCompletionDate",Formato.format(nuevaFecha)+"T00:00:00.000Z");
            
                JSONArray attrs;
                attrs = new JSONArray();
                JSONObject jsonKey1 = new JSONObject();

                jsonKey1 = new JSONObject();
                jsonKey1.accumulate("name","countryCode");
                jsonKey1.accumulate("value","CRI");
                attrs.put(jsonKey1);

                jsonKey1 = new JSONObject();
                jsonKey1.accumulate("name","bscsReason");
                jsonKey1.accumulate("value","8");
                attrs.put(jsonKey1);

                jsonKey1 = new JSONObject();
                jsonKey1.accumulate("name","orderType");
                jsonKey1.accumulate("value","ContractTakeOver");
                attrs.put(jsonKey1);
                
            jsonCreateUserResponse.put("attrs",attrs);
            
            JSONObject notes = new JSONObject();
            notes.put("text", "Take Over for contract Id "+p_contract+" and customerId "+p_customer);
            jsonCreateUserResponse.put("notes",notes);
            
            JSONArray relatedParties = new JSONArray();
                JSONObject related = new JSONObject();
                related.put("role","Customer");
                related.put("reference",p_customer);
                    JSONObject party = new JSONObject();
                        party.put("timeZone","-5");
                        res = querysdata(2,p_customer);
                        while(res.next()){
                            party.put("partyType",res.getString("CUSTOMER_TYPE_ID"));
                            party.put("billingCycle",res.getString("CODE"));
                            party.put("billingCycleEndDate",res.getString("BILLING_CYCLE_END"));
                        }
                party.put("customerMediums",JSONObject.NULL);

                    JSONArray directoryDetails = new JSONArray();
                    JSONObject directoryDe;
                        res = querysdata(4,p_customer);
                        while(res.next()){
                            directoryDe = new JSONObject();
                            directoryDe.put("firstName",res.getString("name"));
                            directoryDe.put("lastName",JSONObject.NULL);
                            directoryDe.put("middleName",JSONObject.NULL);
                            directoryDe.put("nationality",JSONObject.NULL);
                            directoryDe.put("fiscalId",res.getString("id_value"));
                            directoryDe.put("idNumber",res.getString("id_value"));
                            directoryDetails.put(directoryDe);
                        }
                        res=null;
                party.put("directoryDetails",directoryDetails);

                related.put("party",party);
                relatedParties.put(related);
                
            jsonCreateUserResponse.put("relatedParties",relatedParties);
            
            JSONArray relatedEnt = new JSONArray();
            JSONObject relatedEntities = new JSONObject();
                
                relatedEntities = new JSONObject();
                relatedEntities.put("type","Bundle");
                relatedEntities.put("name","BundleDetails");
                relatedEntities.put("reference",p_contract);
                relatedEntities.put("entity","null");
                relatedEnt.put(relatedEntities);
                
            
            jsonCreateUserResponse.put("relatedEntities",relatedEnt);
            jsonCreateUserResponse.put("isBundled",false);
            
            Random  rnd = new Random();
            JSONArray orderItems = new JSONArray();
            JSONObject oritems = new JSONObject();
            
            oritems = new JSONObject();
            oritems.put("id", "857"+rnd.nextInt(99999));
            JSONObject items = new JSONObject();
                items.put("createdBy", "eccventas");
                items.put("createdDate", Formato.format(day)+"T00:00:00.000Z");
                items.put("lastModifiedDate", Formato.format(day)+"T00:00:00.000Z");
                items.put("description", JSONObject.NULL);
                items.put("orderType", "ProductOfferingOrder");
                items.put("action", "Add");
                JSONObject product = new JSONObject();
                    product = new JSONObject();
                    product.put("id", "POCtrlOffContractTakeOver");
                items.put("productOffering", product);
                JSONArray productCharacteristics = new JSONArray();
                JSONObject productChara = new JSONObject();
                
                productChara = new JSONObject();
                productChara.put("name","takeoverBillingAccountAssigments");
                productChara.put("value","false");
                productCharacteristics.put(productChara);
                
                productChara = new JSONObject();
                productChara.put("name","chargeSubs");
                productChara.put("value","false");
                productCharacteristics.put(productChara);
                
                JSONObject productCharas = new JSONObject();
                productCharas.put("productCharacteristics", productCharacteristics);
                items.put("product", productCharas);
                items.put("parentOrderItemId", JSONObject.NULL);
            oritems.put("item", items);
            orderItems.put(oritems);
            jsonCreateUserResponse.put("orderItems",orderItems);
            jsonCreateUserResponse.put("mode","NON_INTERACTIVE");
            jsonCreateUserResponse.put("requestID","23782482019050612221"+rnd.nextInt(9999999));
            jsonCreateUserResponse.put("requester","Beesion");
            jsonCreateUserResponse.put("run",true);
            jsonCreateUserResponse.put("orderId",0);
            jsonCreateUserResponse.put("state",JSONObject.NULL);
            
        }catch(Exception e){
            jsonCreateUserResponse.put("Error", e);
        }
        return Response.ok(jsonCreateUserResponse.toString())
               .header("Access-Control-Allow-Origin", "*")
               .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD")
               .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
       
    }
     
     public void toDb( )
    {
       try{
           Class.forName("oracle.jdbc.driver.OracleDriver"); 
           String dbURL = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS = (PROTOCOL = TCP)(HOST = 10.218.41.17)(PORT = 3875))(CONNECT_DATA =(SERVICE_NAME = CRMUATS)))";
           String strUserID = "CRM_AMX_CENAM_CR_UAT_FU91";
           String strPassword = "Claro2017";
           conn = DriverManager.getConnection(dbURL,strUserID,strPassword);
           System.out.println("Exito Conexion DB.....");
           
       }catch(SQLException | ClassNotFoundException e){
           System.out.println("ErrorDB: "+e);
       }
    }
     
// QUERY.S CRM
     public ResultSet querysdata(int p_data1, String p_data2) throws SQLException{
         ResultSet res = null;
         PreparedStatement pstm = null;
         try{
             toDb();             
             switch(p_data1){
                 
             // Motivos de Reagendar entendidos como error de agendado    
             case 1:
                 String select = "SELECT description, error_type, code FROM TBL_CRM_TECHNICAL_ERRORS WHERE TECHNICAL_ERROR_ID ="+p_data2;
                 pstm = conn.prepareStatement(select);
                 res = pstm.executeQuery();
                 System.out.println("Obteniendo Registros.....");
                 break;
                 
             //obtener ciclos de facturacion    
             case 2:
                 String party = "SELECT CS.BILLING_CYCLE_SPEC_ID, CS.CODE, CS.BILLING_CYCLE_END, CU.CUSTOMER_TYPE_ID FROM TBL_CRM_CUSTOMERS CU\n" +
                         "INNER JOIN TBL_CRM_ACCOUNTS CA ON CA.CUSTOMER_ID = CU.CUSTOMER_ID \n" +
                         "INNER JOIN TBL_CRM_ACCOUNT_BILLING_INFO BI \n" +
                         "on BI.ACCOUNT_BILLING_INFO_ID = CA.ACCOUNT_BILLING_INFO_ID \n" +
                         "INNER JOIN TBL_CRM_BILLING_CYCLE_SPECS CS ON CS.BILLING_CYCLE_SPEC_ID = BI.BILLING_CYCLE_SPEC_ID\n" +
                         "WHERE CU.external_id = '"+p_data2+"'";
                  pstm = conn.prepareStatement(party);
                  res = pstm.executeQuery();
                 break;
             case 3:
                 String contact = "SELECT MT.NAME,CM.CONTACT_MEDIUM_VALUE, DECODE(CM.IS_MAIN,'Y','true','N','false')info  FROM TBL_CRM_CUSTOMERS CU INNER JOIN TBL_CM_PARTY_CONTACT_MEDIUMS CM ON CM.PARTY_ID = CU.PARTY_ID\n" +
                        "INNER JOIN TBL_CM_CONTACT_MEDIUM_TYPES MT ON MT.CM_TYPE_ID = CM.CONTACT_MEDIUM_TYPE_ID\n" +
                        "WHERE external_id = '"+p_data2+"'";
                  pstm = conn.prepareStatement(contact);
                  res = pstm.executeQuery();
                 break;
             case 4:
                 String info_client = "SELECT PI.PARTY_IDENT_TYPE_ID,PI.ID_VALUE,PO.NAME FROM TBL_CRM_CUSTOMERS CU \n" +
                         "INNER JOIN TBL_CM_PARTY_IDENTIFICATIONS PI ON PI.PARTY_ID = CU.PARTY_ID\n" +
                         "INNER JOIN TBL_CM_PARTY_ORGANIZATIONS PO ON PO.PARTY_ID = CU.PARTY_ID\n" +
                         "WHERE CU.external_id ='"+p_data2+"'" +" AND PI.PARTY_IDENT_TYPE_ID = 19";
                 pstm = conn.prepareStatement(info_client);
                 res = pstm.executeQuery();
                 break;
                 
            //Obtener direccion del cliente     
            case 5:
                 String places = "SELECT AR.PARTY_ADDRESS_ROLE_ID, AR.ADDRESS_ROLE_TYPE_ID, AR.ADDRESS_ID, RT.NAME, LA.FULL_ADDRESS, LA.GEO_DATA, LA.ADDRESS_LINE_1\n" +
                         "FROM TBL_CM_PARTY_ADDRESS_ROLES AR\n" +
                         "INNER JOIN TBL_CM_ADDRESS_ROLE_TYPES RT ON RT.ADDRESS_ROLE_TYPE_ID = AR.ADDRESS_ROLE_TYPE_ID\n" +
                         "INNER JOIN TBL_LOC_ADDRESSES LA ON LA.PARTY_ID = AR.PARTY_ID\n" +
                         "WHERE AR.PARTY_ID = (SELECT PARTY_ID FROM TBL_CRM_CUSTOMERS WHERE external_id ='"+p_data2+"'" +") AND AR.ADDRESS_ROLE_TYPE_ID = 1";
                 pstm = conn.prepareStatement(places);
                 res = pstm.executeQuery();
                 break;
            case 6:
                 String seque = "SELECT SEQCODEMPRESA.NEXTVAL FROM DUAL";
                 pstm = conn.prepareStatement(seque);
                 res = pstm.executeQuery();
                 break;
                 
            //Obtener otros campos de la direccion     
            case 7:
                 String address = "select pa.address_id, la.area_id, AR.NAME,AR.AREA_TYPE_ID from TBL_CM_PARTY_ADDRESS_ROLES pa\n" +
                        "INNER JOIN TBL_LOC_ADDRESS_AREAS la on la.ADDRESS_ID = pa.ADDRESS_ID\n" +
                        "INNER JOIN TBL_LOC_AREAS AR on AR.AREA_ID = la.AREA_ID\n" +
                        "where pa.party_id = '"+p_data2+"'and pa.address_role_type_id = 1 order by 4 desc";
                 pstm = conn.prepareStatement(address);
                 res = pstm.executeQuery();
                 break;
             default:
                 break;
             }
         }catch(SQLException e){
             System.out.println(e);
         }
         return res;
     }
}
