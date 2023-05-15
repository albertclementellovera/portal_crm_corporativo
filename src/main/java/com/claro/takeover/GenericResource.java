/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.claro.takeover;

import static com.claro.takeover.agendamiento_tecnico.getservicesregistry;
import static com.claro.takeover.agendamiento_tecnico.sendeom;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Clob;
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
@Path("generic")
public class GenericResource {
    
    @Context
    private UriInfo context;
    private Connection conn = null;  
    
    public GenericResource() {}
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("take")
    public Response takeover(@QueryParam("customer") String p_customer, 
            @QueryParam("contract") String p_contract) throws JSONException, Exception
    { 
        
        JSONObject jsonCreateUserResponse = new JSONObject();
        System.out.println("Customer: "+p_customer+ " contract: "+ p_contract);
        
        try{
            
            String orderType = null;
            String bscsReason = null;
            String cont1 = null;
            String cont11 = null;
            ResultSet res = null;
            ResultSet res2 = null;
            String p_1 = null;
            String p_2 = null;
            ResultSet qry = null;
            Clob test = null;
            
            res2=querysdata(9,p_contract);
            res2.next();
            String p_contrac2 =res2.getString("external_id");
            String p_contrac3 =res2.getString("contract_details");
            log_bscs("obtuvo contratos: "+p_contrac2+", "+p_contrac3,p_customer+", "+p_contract);
            System.out.println("obtuvo contratos: "+p_contrac2+", "+p_contrac3);
            
            res=querysdata(10,p_contrac3);
            res.next();
            
            String old_customer =res.getString("customer_id");
            
            String tal1 = null;
            String[] parts = null;
            Boolean pas = false;
            Boolean pas2 = false;
            int retry = 0;
            
            if(pas2 == true){
                p_2 = jeom2(p_contract, p_customer,cont1);
                log_bscs("Segundo Cambio de Titular."+p_2,p_customer+", "+p_contract);
                System.out.println("Segundo Cambio de Titular."+p_2);
                Thread.sleep(3*60*1000);

                String tal11 = null;
                String[] parts1 = null;
                Boolean pas1 = false;
                Boolean pas21 = false;
                int retry1 = 0;
                
                while(pas1 == false){
                    retry1++;
                    qry=null;
                    qry = querysdata(8,p_2);
                    qry.next();
                    test = qry.getClob("notification_data");
                    String tel = getClobString(test);
                    
                    tal11 = getstatus(tel);
                    parts1 = tal11.split(",");
                    String sta = parts1[0];
                    cont11 = parts1[1];
                    
                    if(sta.equals("CLOSED.COMPLETED") && !cont11.equals("1")){
                        pas1 = true;
                        log_bscs("Vandera True",p_customer+", "+p_contract);
                        System.out.println("Vandera True");
                        pas21 = true;
                    } 
                    if(retry1 >=3 ){
                        pas1 = true;
                        log_bscs("Error EOM",p_customer+", "+p_contract);
                        System.out.println("Error EOM");
                    }
                    Thread.sleep(30000);
                }
            }if(pas2 ==false){
                log_bscs("Error en primer TAKEOVER"+p_1,p_customer+", "+p_contract);
                System.out.println("Error en primer TAKEOVER"+p_1);
                p_2 ="Error primer takeover";
            } 
            log_bscs("Salio de Ciclo",p_customer+", "+p_contract);
            System.out.println("Salio de Ciclo");
            
            JSONObject noticrm = new JSONObject();
                    noticrm.put("OriginCustomer",old_customer);
                    noticrm.put("OriginContract",p_contract);
                    noticrm.put("TargetCustomer",p_customer);
                    noticrm.put("TargetContract",cont11);
                    noticrm.put("ExternalSystem","BSCS");
                    noticrm.put("RequesterId",3);
                    noticrm.put("Operation","TakeOver");
                    log_bscs(noticrm.toString(),p_customer+", "+p_contract);
                    System.out.println(noticrm.toString());
                    
                    String crmorder = sendcrm(noticrm.toString());
                   log_bscs(crmorder,p_customer+", "+p_contract);
                   System.out.println(crmorder);
                   
                   Thread.sleep(30000);
                   ResultSet data1 = querysdata(11,cont11);   
                   Thread.sleep(30000);
                   
            insertordereom(p_contract,cont11);
              
            jsonCreateUserResponse.put("mode","test");
        
    }catch(Exception e){
        System.out.println(e);
    }

        
    return Response.ok(jsonCreateUserResponse.toString())
               .header("Access-Control-Allow-Origin", "*")
               .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD")
               .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("maps/getSessionId")
    public Response getSessionId()
    {
        String response = null;
        String idSession = null;
        PreparedStatement pstmt;
        ResultSet rs;
        try
        {
            toDb();
            String query = "SELECT FN_GET_SESSION_ID() SESSION_ID FROM DUAL";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            idSession = String.valueOf(rs.getInt("SESSION_ID"));
            
            conn.close();
            
        }catch(Exception ex)
        {
            System.out.println(ex.toString());
            idSession = "0";
        }
        
        response = "{ \n \"sessionId\":\""+idSession+"\" \n }";
        return Response.ok(response)
               .header("Access-Control-Allow-Origin", "*")
               .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD")
               .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }
    
    public static String getstatus(String p_json) throws Exception
    {
        String elements= null;
        String jsonf  = null;
        JSONObject obj = new JSONObject(p_json);
        String childs  = null;
        String state  = null;
        String contract = "1";
        
        state = obj.getJSONObject("Event").getString("state");
        
        if(state.equals("CLOSED.COMPLETED")){
            
            contract = obj.getJSONObject("Event").getJSONArray("attrs").getJSONObject(2).getString("value");
            System.out.println(contract);
        }
        jsonf = state+","+contract;       
        System.out.println("parse");
        System.out.println(jsonf);
	return jsonf;
    }
    
    public static String getClobString(Clob clob) throws SQLException,IOException
    {
        BufferedReader stringReader = new BufferedReader(
                clob.getCharacterStream());
        String singleLine = null;
        StringBuffer strBuff = new StringBuffer();
        while ((singleLine = stringReader.readLine()) != null) {
            strBuff.append(singleLine);
        }
        return strBuff.toString();
    }
    
    public String jeom2(String p_contract, String p_customer, String p_contract1) 
    {
        JSONObject jsonCreateUserResponse = new JSONObject();
        JSONObject jsonCreateUserResponse2 = new JSONObject();
        String orderType = null;
        String bscsReason = null;
        ResultSet res = null;
        String p_user = "jesus.mendez";
        String resp= null;
        try { 
            
            URL url = new URL("http://172.17.224.150:7060/cwf/om/v1/order");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            String regis = getservicesregistry("http://172.17.224.150:7060/cwf/sr/v1/product/?relatedParties.role=Customer&relatedParties.reference="+p_customer+"&relatedEntities.role=Bundle&relatedEntities.reference="+p_contract);
            String[] parts = regis.split("/");
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
                relatedEntities.put("type","Contract");
                relatedEntities.put("name","ContractDetails");
                relatedEntities.put("reference",p_contract);
                relatedEntities.put("entity",JSONObject.NULL);
                relatedEnt.put(relatedEntities);
        
                relatedEntities = new JSONObject();
                relatedEntities.put("type","Bundle");
                relatedEntities.put("name","BundleDetails");
                relatedEntities.put("reference",p_contract);
                relatedEntities.put("entity",JSONObject.NULL);
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
            
            resp = sendeom(jsonCreateUserResponse.toString());
            
        }catch(Exception e){
            System.out.println(e);
        }
        return resp;
    }
    
    public void insertordereom(String old_contr, String new_contr)
    {
         
         try{
             toDb();
             Statement st = conn.createStatement();
             st.executeQuery("update TBL_CRM_SUBSCRIPTIONS set external_id = '"+old_contr+"', deactivation_date = null"
                     + " where contract_details = '"+new_contr+"'");
             st.executeQuery("commit");
             conn.close();  
             log_bscs("Realizo Commit Update",old_contr+", "+new_contr);
             System.out.println("Actualizo Ultimo contrato3");  
         }catch(SQLException e){
             System.out.println(e);
         }
     }
    
    public void toDb()
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
                case 7:
                    String order = "select MAX(subscription_id) from TBL_CRM_SUBSCRIPTIONS where external_id = '"+p_data2+"'";
                    pstm = conn.prepareStatement(order);
                    res = pstm.executeQuery();
                    break;
                case 8:
                    String rorder = "select notification_data from TBL_CRM_PEND_ORD_NOTIFICATIONS where public_code = '"+p_data2+"' \n" +
                       "and pending_order_notification_id = (select MAX(pending_order_notification_id) from TBL_CRM_PEND_ORD_NOTIFICATIONS where public_code = '"+p_data2+"')";
                    pstm = conn.prepareStatement(rorder);
                    res = pstm.executeQuery();
                    break;
                case 9:
                    String con = "select external_id, contract_details from TBL_CRM_SUBSCRIPTIONS where external_id = '"+p_data2+"' and \n" +
                        "subscription_id = (select MAX(subscription_id) from TBL_CRM_SUBSCRIPTIONS where external_id = '"+p_data2+"' and status = 2)";
                    pstm = conn.prepareStatement(con);
                    res = pstm.executeQuery();
                    break;
                case 10:
                    String contp = "select customer_id from contract_all@CRM_TO_BSCS2 where co_id = '"+p_data2+"'";
                    pstm = conn.prepareStatement(contp);
                    res = pstm.executeQuery();
                    break;
                case 11:
                    String updata = "select * from TBL_CRM_SUBSCRIPTIONS where contract_details = '"+p_data2+"'";
                    pstm = conn.prepareStatement(updata);
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
    
    public void toDbx03()
    {
        try{
           Class.forName("oracle.jdbc.driver.OracleDriver"); 
           String dbURL = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=172.24.5.213)(PORT=3924))(CONNECT_DATA=(SERVICE_NAME=BSDBX03)))";
           String strUserID = "INTERFAZ";
           String strPassword = "INTERFAZ";
           conn = DriverManager.getConnection(dbURL,strUserID,strPassword);
           System.out.println("Exito Conexion DB.....");
           
       }catch(SQLException | ClassNotFoundException e){
           System.out.println("ErrorDB: "+e);
       }
    }
    
    public void log_bscs(String descrip, String cu_co)
    {       
        try{
             toDbx03();
             Statement st = conn.createStatement();
             st.executeUpdate("INSERT INTO TBL_TAKEOVER_CRM (ID, DESCRIPCION, CU_CO) \n" +
                    "VALUES(SQ_TAKEOVER.NEXTVAL,'"+descrip+"','"+cu_co+"')");
             st.executeQuery("commit");
             
             conn.close();         
                          
         }catch(SQLException e){
             System.out.println(e);
         }
    }
    
    public static String sendcrm(String orden) throws Exception
    {
        String elements= null;
         URL url = new URL("http://10.218.41.39:8003/CrmAmxCenamFuCrUat91/rest/crmorder/order");
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
            int id = obj.getInt("OrderId");
        return Integer.toString(id);
    }    
}
