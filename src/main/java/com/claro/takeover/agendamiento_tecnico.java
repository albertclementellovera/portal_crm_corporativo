/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.claro.takeover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
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
@Path("agendamiento")
public class agendamiento_tecnico {
    
    @Context
    private UriInfo context;
    private Connection conn = null;  

    public agendamiento_tecnico() {}
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/json")
    @Path("agendatec")
    public Response takeover(@QueryParam("customer") String p_customer, @QueryParam("contract") String p_contract,@QueryParam("user") String p_user, @QueryParam("iderror") String p_iderror, @QueryParam("errordescription") String p_errordes,@QueryParam("winstar") String p_winstar,@QueryParam("winend") String p_winend,@QueryParam("timeslot") String p_timeslot)
    {
        JSONObject jsonCreateUserResponse = new JSONObject();
        JSONObject jsonCreateUserResponse2 = new JSONObject();
        String orderType = null;
        String bscsReason = null;
        String Nodoc = null;
        orderType ="EquipmentRepair";
        bscsReason = "1";
        
        switch(p_iderror)
        {
            case "2":
                orderType ="InstAddressChangeInt";
                bscsReason = "5";
            break;
            case "6":
                orderType ="EquipmentRepair";
                bscsReason = "1";
            break;
            case "8":
                orderType ="EquipmentRepair";
                bscsReason = "1";
            break;
            case "11":
                orderType ="EquipmentRepair";
                bscsReason = "1";
            break;
            case "12":
                orderType ="EquipmentRepair";
                bscsReason = "1";
            break;
        }

        try
        {
            String regis = getservicesregistry("http://172.17.224.150:7060/cwf/sr/v1/product/?fields=productOffering,comprisedOf,id,name,activeStartDates,productCharacteristics,services&relatedParties.role=Customer&relatedParties.reference="+p_customer+"&relatedEntities.role=Bundle&relatedEntities.reference="+p_contract);
            String[] parts = regis.split("/");
            ResultSet res = null;        
            URL url = new URL("http://172.17.224.150:7060/cwf/om/v1/order");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            Date day = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(day);
            cal.add(Calendar.YEAR, 1);
            Date nuevaFecha = cal.getTime();
            DateFormat Formato = new SimpleDateFormat("yyyy-MM-dd");
            
            jsonCreateUserResponse.put("createdDate",Formato.format(day)+"T00:00:00.000Z");
            jsonCreateUserResponse.put("createdBy",p_user);
            jsonCreateUserResponse.put("version",1);
            jsonCreateUserResponse.put("description","Postsale - technical appointment");
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
            jsonKey1.accumulate("value",bscsReason);
            attrs.put(jsonKey1);
            
            jsonKey1 = new JSONObject();
            jsonKey1.accumulate("name","orderType");
            jsonKey1.accumulate("value",orderType);
            attrs.put(jsonKey1);
            
            jsonKey1 = new JSONObject();
            jsonKey1.accumulate("name","salesExecutive");
            jsonKey1.accumulate("value",p_user);
            attrs.put(jsonKey1);
            
            jsonKey1 = new JSONObject();
            jsonKey1.accumulate("name","salesChannel");
            jsonKey1.accumulate("value","CORP");
            attrs.put(jsonKey1);
            
            jsonKey1 = new JSONObject();
            jsonKey1.accumulate("name","ticketing");
            
            JSONObject jsonKey2 = new JSONObject();
            jsonKey2.put("installDate", Formato.format(day)+"T00:00:00.000Z");
            res = querysdata(1,p_iderror);
            while(res.next())
            {
                jsonKey2.put("errorDescription", res.getString("description"));
                jsonKey2.put("ttfalla", res.getString("code"));
                jsonKey2.put("errorType", Integer.parseInt(res.getString("error_type")));
                jsonKey2.put("error", "E00"+res.getString("code"));
            }
            
            jsonKey2.put("errorDate", Formato.format(day)+"T00:00:00.000Z");
            res = null;
            
            jsonKey1.accumulate("value",jsonKey2);
            
            attrs.put(jsonKey1);
            jsonCreateUserResponse.put("attrs",attrs);
            
            JSONObject notes = new JSONObject();
            notes.put("text", "Technical appointment for contract Id "+p_contract+" and customerId "+p_customer);
            jsonCreateUserResponse.put("notes",notes);
            
            JSONArray relatedParties = new JSONArray();
            JSONObject related = new JSONObject();
            related.put("role","Customer");
            related.put("reference",p_customer);
            JSONObject party = new JSONObject();
            party.put("partyType","43");
            party.put("timeZone","-5");
            res = querysdata(2,p_customer);
            while(res.next())
            {
                 party.put("billingCycle",res.getString("CODE"));
                 party.put("billingCycleEndDate",res.getString("BILLING_CYCLE_END"));
            }
                    
            res = null;
            JSONArray customerMediums = new JSONArray();
            JSONObject customerMed;
            res = querysdata(3,p_customer);
            while(res.next())
            {
                customerMed = new JSONObject();
                customerMed.put("customerContact",res.getString("name"));
                customerMed.put("customerContactType",res.getString("CONTACT_MEDIUM_VALUE"));
                customerMed.put("mainContact", res.getString("info"));
                customerMediums.put(customerMed);
            }
            res = null;
            
            party.put("customerMediums",customerMediums);
            
            JSONArray directoryDetails = new JSONArray();
            JSONObject directoryDe;
            res = querysdata(4,p_customer);
            while(res.next())
            {
                directoryDe = new JSONObject();
                directoryDe.put("firstName",res.getString("name"));
                directoryDe.put("lastName",JSONObject.NULL);
                directoryDe.put("middleName",JSONObject.NULL);
                directoryDe.put("nationality",JSONObject.NULL);
                directoryDe.put("fiscalId",res.getString("id_value"));
                directoryDe.put("idNumber",res.getString("id_value"));
                Nodoc =res.getString("id_value");
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

            relatedEntities = new JSONObject();
            relatedEntities.put("type","Place");
            relatedEntities.put("name","Address");
            relatedEntities.put("reference","2958436");
                            
            JSONObject entity = new JSONObject();
            entity = new JSONObject();
            res = querysdata(5,p_customer);
            String party_id = null;
            while(res.next())
            {
                party_id = res.getString("PARTY_ID");
            }
                   
            ResultSet res2 = querysdata(6,party_id);
            String District = null;
            String canton = null;
            String province = null;
            String city = null;
            int count = 0;

            while(res2.next())
            {
                count++;
                if (count ==1){
                    city = res2.getString("name");
                }
                if (count ==2){
                    District = res2.getString("name");
                }
                if (count ==3){
                    canton = res2.getString("name");
                }
                if (count ==4){
                    province = res2.getString("name");
                }
            }
            
            res =null;
            res = querysdata(5,p_customer);
            while(res.next())
            {
                String[] address = res.getString("FULL_ADDRESS").split("-");
                String Line2 = address[0];
                String streetName = address[1];
                entity.put("bscsSequence","null");
                entity.put("actionCode", "Add");
                entity.put("defaultAddress",true);
                entity.put("typeAddress", "INSTALL");
                entity.put("addressLine1", res.getString("ADDRESS_LINE_1"));
                entity.put("addressLine2", Line2);
                entity.put("streetName",streetName+","+Line2);
                entity.put("streetNumber", JSONObject.NULL);
                entity.put("city", city);
                entity.put("country", "Costa Rica");
                entity.put("province",  province);
                entity.put("canton", canton);
                entity.put("district",District);
                entity.put("zipCode", "188");
                entity.put("countryCodntoe", "CRI");
                entity.put("bucket", JSONObject.NULL);
                entity.put("slaWindowStart",p_winstar);
                entity.put("slaWindowEnd", p_winend);
                entity.put("timeSlot", "All-Day");
                entity.put("timeSlotDate", Formato.format(day)+"T00:00:00.000Z");
                entity.put("coordx", JSONObject.NULL);
                entity.put("coordy", JSONObject.NULL);
                entity.put("mainAccount", false);
                entity.put("contractExpirationDate", JSONObject.NULL);
            }
            
            relatedEntities.put("entity",entity);
            relatedEnt.put(relatedEntities);
            jsonCreateUserResponse.put("relatedEntities",relatedEnt);
            jsonCreateUserResponse.put("isBundled",false);
            JSONArray orderItems = new JSONArray();
            JSONObject oritems = new JSONObject();
            String[] raiz = parts[0].split(",");
            oritems = new JSONObject();
            oritems.put("id", raiz[0]);
            JSONObject items = new JSONObject();
            items.put("createdBy", "eccventas");
            items.put("createdDate", raiz[3]);
            items.put("lastModifiedDate", raiz[3]);
            items.put("description", raiz[1]);
            items.put("orderType", "ProductOfferingOrder");
            items.put("action", "No_Change");
            
            JSONObject product = new JSONObject();
            product = new JSONObject();
            product.put("id", raiz[2]);
            items.put("productOffering", product);
            items.put("product", JSONObject.NULL);
            items.put("parentOrderItemId", JSONObject.NULL);
            oritems.put("item", items);
            orderItems.put(oritems);
            String Modify = null;
            
            Modify = bscsReason.equals("1") ? "Modify" : "No_Change";
            
            for(int pos = 1; pos < parts.length; pos++)
            {
                String[] ch = parts[pos].split(",");
                if(ch[1].equals("ONT"))
                {
                    oritems = new JSONObject();
                    oritems.put("id", ch[0]);
                    JSONObject itemsc = new JSONObject();
                    itemsc.put("createdBy", "eccventas");
                    itemsc.put("createdDate", ch[3]);
                    itemsc.put("lastModifiedDate", ch[3]);
                    itemsc.put("description", ch[1]);
                    itemsc.put("orderType", "ProductOfferingOrder");
                    itemsc.put("action", Modify);
                    JSONObject productc = new JSONObject();
                        productc = new JSONObject();
                        productc.put("id", ch[2]);
                    itemsc.put("productOffering", productc);
                    itemsc.put("product", JSONObject.NULL);
                    itemsc.put("parentOrderItemId", JSONObject.NULL);
                    oritems.put("item", itemsc);
                    orderItems.put(oritems);
                }else
                {
                    oritems = new JSONObject();
                    oritems.put("id", ch[0]);
                    JSONObject itemsc = new JSONObject();
                    itemsc.put("createdBy", "eccventas");
                    itemsc.put("createdDate", ch[3]);
                    itemsc.put("lastModifiedDate", ch[3]);
                    itemsc.put("description", ch[1]);
                    itemsc.put("orderType", "ProductOfferingOrder");
                    itemsc.put("action", "No_Change");
                    JSONObject productc = new JSONObject();
                        productc = new JSONObject();
                        productc.put("id", ch[2]);
                    itemsc.put("productOffering", productc);
                    itemsc.put("product", JSONObject.NULL);
                    itemsc.put("parentOrderItemId", JSONObject.NULL);
                    oritems.put("item", itemsc);
                    orderItems.put(oritems);
                }
            }
              
            jsonCreateUserResponse.put("orderItems",orderItems);
            jsonCreateUserResponse.put("mode","NON_INTERACTIVE");
            Random  rnd = new Random();
            jsonCreateUserResponse.put("requestID","23782482019050612221"+rnd.nextInt(9999999));
            jsonCreateUserResponse.put("requester","Beesion");
            jsonCreateUserResponse.put("run",true);
            jsonCreateUserResponse.put("orderId",0);
            jsonCreateUserResponse.put("state",JSONObject.NULL);
            
            String ordereom = sendeom(jsonCreateUserResponse.toString());
            insertordereom(p_customer, p_contract, ordereom, p_iderror, Nodoc);
            
            conn.disconnect();
            jsonCreateUserResponse2.put("order_id",ordereom);
            jsonCreateUserResponse2.put("error","0");
            jsonCreateUserResponse2.put("value",JSONObject.NULL);
        }
        catch (Exception ex)
        {
            Logger.getLogger(agendamiento_tecnico.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Response.ok(jsonCreateUserResponse2.toString())
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
     
      
    public ResultSet querysdata(int p_data1, String p_data2) throws SQLException{
         ResultSet res = null;
         PreparedStatement pstm = null;
         try{
             toDb();             
             switch(p_data1){
             case 1:
                 String select = "SELECT description, error_type, code FROM TBL_CRM_TECHNICAL_ERRORS WHERE TECHNICAL_ERROR_ID ="+p_data2;
                 pstm = conn.prepareStatement(select);
                 res = pstm.executeQuery();
                 System.out.println("Obteniendo Registros.....");
                 break;
             case 2:
                 String party = "SELECT CS.BILLING_CYCLE_SPEC_ID, CS.CODE, CS.BILLING_CYCLE_END FROM TBL_CRM_CUSTOMERS CU\n" +
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
            case 5:
                 String places = "SELECT AR.PARTY_ID, AR.PARTY_ADDRESS_ROLE_ID, AR.ADDRESS_ROLE_TYPE_ID, AR.ADDRESS_ID, RT.NAME, LA.FULL_ADDRESS, LA.GEO_DATA, LA.ADDRESS_LINE_1\n" +
                         "FROM TBL_CM_PARTY_ADDRESS_ROLES AR\n" +
                         "INNER JOIN TBL_CM_ADDRESS_ROLE_TYPES RT ON RT.ADDRESS_ROLE_TYPE_ID = AR.ADDRESS_ROLE_TYPE_ID\n" +
                         "INNER JOIN TBL_LOC_ADDRESSES LA ON LA.PARTY_ID = AR.PARTY_ID\n" +
                         "WHERE AR.PARTY_ID = (SELECT PARTY_ID FROM TBL_CRM_CUSTOMERS WHERE external_id ='"+p_data2+"'" +") AND AR.ADDRESS_ROLE_TYPE_ID = 1 AND AR.ADDRESS_ID = LA.ADDRESS_ID";
                 
                 pstm = conn.prepareStatement(places);
                 res = pstm.executeQuery();
                 break;
            case 6:
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
         }catch(Exception e){
             System.out.println(e);
         }
         return res;
     }
     
     
    public static String getservicesregistry(String urlParaVisitar) throws Exception {
        String elements= null;
        String jsonf  = null;
        try{
            URL url = new URL(urlParaVisitar);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");


            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));


            System.out.println("Output from Server .... \n");
            String output;
            while ((output = br.readLine()) != null) {
                    System.out.println(output);
                    elements = output;
            }
            elements ="{\"data\":"+elements+"}";
            JSONObject obj = new JSONObject(elements);
            System.out.println(elements);
            String childs  = null;
            String fath  = null;
            for(int pos = 0; pos < obj.getJSONArray("data").length(); pos++){
                 String id = obj.getJSONArray("data").getJSONObject(pos).getString("id");
                 boolean child = obj.getJSONArray("data").getJSONObject(pos).isNull("comprisedOf");
                 String fech = obj.getJSONArray("data").getJSONObject(pos).getJSONArray("activeStartDates").getJSONObject(0).getString("value");
                 String description = obj.getJSONArray("data").getJSONObject(pos).getString("name");
                 String productoffer = obj.getJSONArray("data").getJSONObject(pos).getJSONObject("productOffering").getString("id");
                if(child == true ){
                    if(childs == null){
                        childs = "/"+id+","+description+","+productoffer+","+fech;
                    }else{
                        childs = childs+"/"+id+","+description+","+productoffer+","+fech;
                    }
                }else{
                    if(fath == null){
                        fath =id+","+description+","+productoffer+","+fech;
                    }else{
                        fath =fath+"/"+id+","+description+","+productoffer+","+fech;
                    }
                }
                
            }    
            jsonf = fath+childs;
            System.out.println("parse");
            System.out.println(jsonf);
            conn.disconnect();
	  } catch (MalformedURLException e) {

		System.out.println(e);

	  } catch (IOException e) {

		System.out.println(e);
          }
	return jsonf;
    }
    
    public static String sendeom(String orden) throws Exception {
        String elements= null;
         URL url = new URL("http://172.17.224.150:7060/cwf/om/v1/order");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStream os = conn.getOutputStream();
            os.write(orden.getBytes());
            os.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                            (conn.getInputStream())));
            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
                elements = output;
            }
            JSONObject obj = new JSONObject(elements);
            String id = obj.getString("id");
            System.out.println(id);
        return id;
    }
    
     
    public void toDbx03( )
    {
       try{
           Class.forName("oracle.jdbc.driver.OracleDriver"); 
           String dbURL = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS = (PROTOCOL = TCP)(HOST = 172.17.224.151)(PORT = 1521))(CONNECT_DATA =(SERVICE_NAME = BCRIDEV1)))";
           String strUserID = "CRM_AMX";
           String strPassword = "CRM_AMX";
           conn = DriverManager.getConnection(dbURL,strUserID,strPassword);
           System.out.println("Exito Conexion DB.....");
           
       }catch(SQLException | ClassNotFoundException e){
           System.out.println("ErrorDB: "+e);
       }
    }
     
    
    public void insertordereom(String customer, String contract, String ordereom, String action, String nodocument){
         
         try{
             toDbx03();
             Statement st = conn.createStatement();
             st.executeUpdate("INSERT INTO TBL_CRM_ORDEREOM (ID, CUSTOMERID, CONTRACT, ORDEREOM, ACTION, NODOCUMENT) \n" +
                    "VALUES(SEQ_CRM_ORDEREOM.NEXTVAL,'"+customer+"','"+contract+"','"+ordereom+"','"+action+"','"+nodocument+"')");
             conn.commit();
             conn.close();         
                          
         }catch(SQLException e){
             System.out.println(e);
         }
     }
}
