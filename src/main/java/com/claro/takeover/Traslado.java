/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.claro.takeover;

import static com.claro.takeover.agendamiento_tecnico.getservicesregistry;
import static com.claro.takeover.agendamiento_tecnico.sendeom;
import com.claro.ws.CustomSoapClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Albert
 */
@Path("traslado")
public class Traslado {
    
    @Context
    private UriInfo context;
    private Connection conn = null;
    Crm_logs archivo = new Crm_logs();
    
    public Traslado() {}
    
    public Response getJson(@QueryParam("customer") String p_customer, 
            @QueryParam("contract") String p_contract, 
            @QueryParam("sessionid") String session,
            @QueryParam("direccion") String direccion) 
            throws JSONException{
        
        archivo.crearLog("Traslado de Servicio: "+"\n CUSTOMER: "+p_customer+"\n CONTRACT: "+p_contract+"\n SESSIONID: "+session);
        JSONObject jsonCreateUserResponse = new JSONObject();
        JSONObject jsonCreateUserResponse2 = new JSONObject();
        String orderType = null;
        String bscsReason = null;
        ResultSet res = null;
        String p_user = "eccventas";
        String[] newaddress = direccion.split(",");
        
        String pro = newaddress[0];
        String dis = newaddress[1];
        String co = newaddress[2];
        String dir = newaddress[3];
        String cdir = newaddress[4];
        try { 
            String testi = sendreserva(session,p_customer,p_contract);
                   
            String regis = getservicesregistry("http://172.17.224.150:7060/cwf/sr/v1/product/?relatedParties.role=Customer&relatedParties.reference="+p_customer+"&relatedEntities.role=Bundle&relatedEntities.reference="+p_contract);
            String[] parts = regis.split("/");
            //archivo.crearLog("2-SERVICE REGISTRY: "+regis+"\n CUSTOMER: "+p_customer+"\n CONTRACT: "+p_contract);
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
            jsonCreateUserResponse.put("description","Postsale - change service address");
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
                jsonKey1.accumulate("value","5");
                attrs.put(jsonKey1);

                jsonKey1 = new JSONObject();
                jsonKey1.accumulate("name","orderType");
                jsonKey1.accumulate("value","InstAddressChangeExt");
                attrs.put(jsonKey1);

                jsonKey1 = new JSONObject();
                jsonKey1.accumulate("name","salesExecutive");
                jsonKey1.accumulate("value",p_user);
                attrs.put(jsonKey1);

                jsonKey1 = new JSONObject();
                jsonKey1.accumulate("name","salesChannel");
                jsonKey1.accumulate("value","CORP");
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
                        party.put("partyType","1");
                        party.put("timeZone","-5");
                        res = querysdata(2,p_customer);
                        while(res.next()){
                             party.put("billingCycle",res.getString("CODE"));
                             party.put("billingCycleEndDate",res.getString("BILLING_CYCLE_END"));
                        }

                res = null;
                    JSONArray customerMediums = new JSONArray();
                    JSONObject customerMed;
                    res = querysdata(3,p_customer);
                        while(res.next()){
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
                
                relatedEntities = new JSONObject();
                relatedEntities.put("type","OldPlace");
                relatedEntities.put("name","Address");
                JSONObject entity = new JSONObject();
                    entity = new JSONObject();
                    res = querysdata(5,p_customer);
                    
                    String party_id = null;
                    while(res.next()){
                        party_id = res.getString("PARTY_ID");
                    }
                   
                    ResultSet res2 = querysdata(7,party_id);
                    String District = null;
                    String canton = null;
                    String province = null;
                    String city = null;
                    int count = 0;
                    
                    while(res2.next()){
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
                    
                    
                    while(res.next()){
                        relatedEntities.put("reference",res.getString("ADDRESS_ID"));
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
                        entity.put("slaWindowStart",Formato.format(day)+"T00:00:00.000Z");
                        entity.put("slaWindowEnd", "2021-03-24T00:00:00.000Z");
                        entity.put("timeSlot", "All-Day");
                        entity.put("timeSlotDate", Formato.format(day)+"T00:00:00.000Z");
                        entity.put("coordx", JSONObject.NULL);
                        entity.put("coordy", JSONObject.NULL);
                        entity.put("mainAccount", false);
                        entity.put("contractExpirationDate", JSONObject.NULL);
                    }
                relatedEntities.put("entity",entity);
                relatedEnt.put(relatedEntities);
                
                relatedEntities = new JSONObject();
                relatedEntities.put("type","Place");
                relatedEntities.put("name","Address");
                Random  rnd1 = new Random();
                relatedEntities.put("reference","989"+rnd1.nextInt(9999));
                    entity = new JSONObject();
                    entity.put("bscsSequence","null");
                    entity.put("actionCode", "Add");
                    entity.put("defaultAddress",true);
                    entity.put("typeAddress", "INSTALL");
                    entity.put("addressLine1", dir);
                    entity.put("addressLine2", cdir);
                    entity.put("streetName",JSONObject.NULL);
                    entity.put("streetNumber", JSONObject.NULL);
                    entity.put("city", "PANAMA");
                    entity.put("country","panama");
                    entity.put("province",  pro);
                    entity.put("canton", co);
                    entity.put("district",dis);
                    entity.put("zipCode", "591");
                    entity.put("countryCode", "PAN");
                    entity.put("bucket", JSONObject.NULL);
                    entity.put("slaWindowStart",Formato.format(day)+"T00:00:00.000Z");
                    entity.put("slaWindowEnd", "2019-10-30T00:00:00.000Z");
                    entity.put("timeSlot", "All-Day");
                    entity.put("timeSlotDate", Formato.format(day)+"T00:00:00.000Z");
                    entity.put("coordx", JSONObject.NULL);
                    entity.put("coordy", JSONObject.NULL);
                    entity.put("mainAccount", false);
                    entity.put("contractExpirationDate", JSONObject.NULL);
                    
                relatedEntities.put("entity",entity);
                relatedEnt.put(relatedEntities);
            
            jsonCreateUserResponse.put("relatedEntities",relatedEnt);
            jsonCreateUserResponse.put("isBundled",false);
            
            JSONArray orderItems = new JSONArray();
            JSONObject oritems = new JSONObject();
            for(int pos = 0; pos < parts.length; pos++){
                String[] ch = parts[pos].split(",");
                    int flag = ch[1].indexOf("Internet Empresa");
                    if(flag != -1){
                        oritems = new JSONObject();
                        oritems.put("id", ch[0]);
                        JSONObject itemsc = new JSONObject();
                        itemsc.put("createdBy", "eccventas");
                        itemsc.put("createdDate", ch[3]);
                        itemsc.put("lastModifiedDate", ch[3]);
                        itemsc.put("description", ch[1]);
                        itemsc.put("orderType", "ProductOfferingOrder");
                        itemsc.put("action", "Modify");
                        JSONObject productc = new JSONObject();
                            productc = new JSONObject();
                            productc.put("id", ch[2]);
                        itemsc.put("productOffering", productc);
                        
                        JSONArray producta = new JSONArray();
                        JSONObject producto = new JSONObject();
                            producto = new JSONObject();
                            producto.put("name", "reservationId");
                            //producto.put("value","789945645");
                            producto.put("value",testi);
                            producta.put(producto);
                        itemsc.put("product", producta);
                        itemsc.put("parentOrderItemId", JSONObject.NULL);
                        oritems.put("item", itemsc);
                        orderItems.put(oritems);
                        ch = null;
                    }else{
                        
                        oritems = new JSONObject();
                        oritems.put("id", ch[0]);
                        JSONObject itemsc = new JSONObject();
                        itemsc.put("createdBy", "eccventas");
                        itemsc.put("createdDate", ch[3]);
                        itemsc.put("lastModifiedDate", ch[3]);
                        itemsc.put("description", ch[1]);
                        itemsc.put("orderType", "ProductOfferingOrder");
                        itemsc.put("action", "Modify");
                        JSONObject productc = new JSONObject();
                            productc = new JSONObject();
                            productc.put("id", ch[2]);
                        itemsc.put("productOffering", productc);
                        itemsc.put("product", JSONObject.NULL);
                        itemsc.put("parentOrderItemId", JSONObject.NULL);
                        oritems.put("item", itemsc);
                        orderItems.put(oritems);
                        ch=null;
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
            
            insertordereom(p_customer, p_contract, ordereom, "InstAddressChangeExt", res.getString("id_value"));
                    
            jsonCreateUserResponse2.put("order_id",ordereom);
            jsonCreateUserResponse2.put("error","0");
            jsonCreateUserResponse2.put("value",JSONObject.NULL);
            //archivo.crearLog("3-JsonEOM: "+jsonCreateUserResponse);
            //archivo.crearLog("4-JsonEOM: "+ordereom);
        } catch (Exception ex) {
            
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
      
         public String  sendreserva(String session_id, String p_customer, String p_contract)
    {
        String nuevo = null;
        String nuevokey = null;
        ResultSet res = null;
        String data = null;
        String key = null;
        String AccessPointName = null;
        String AccessPointType = null;
        try 
        {
           Date day = new Date();
           DateFormat Formato = new SimpleDateFormat("yyyy-MM-dd");
            res = querysdata(6,session_id);
            while(res.next()){
                data= res.getString("geo_data");
            }
            res = null;
            
            res = querysdata(7,"");
            while(res.next()){
                key= res.getString("value");
            }
            res = null;

            
            JSONObject obj = new JSONObject(data);
            AccessPointName = obj.getJSONArray("AccessTypeList").getJSONObject(0).getJSONArray("Equipments").getJSONObject(0).getString("AccessPointName");
            AccessPointType = obj.getJSONArray("AccessTypeList").getJSONObject(0).getJSONArray("Equipments").getJSONObject(0).getString("AccessPointType");
            
            String speed = getvelocidad(p_contract, p_customer);
           
           String claro =
            "<S:Envelope\n" +
            "    xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "    <SOAP-ENV:Header>\n" +
            "        <graniteSessionInfo\n" +
            "            xmlns=\"urn:com:ericsson:schema:xml:granite:sdk:common:data-types:1:0\"\n" +
            "            xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "            xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "            <traceSessionInfo\n" +
            "                xmlns=\"\">\n" +
            "                <traceId/>NewInstall\n" +
            "                <tracePrefix/>AMX_"+Formato.format(day)+"_Log97\n" +
            "                <traceLevel/>DEBUG\n" +
            "                <flowVizFormat/>SDEDIT_SDX\n" +
            "            </traceSessionInfo>\n" +
            "            <webAuthenticationToken\n" +
            "                xmlns=\"\">"+key+"\n" +
            "            </webAuthenticationToken>\n" +
            "            <originatingSystem\n" +
            "                xmlns=\"\">CRM\n" +
            "            </originatingSystem>\n" +
            "        </graniteSessionInfo>\n" +
            "    </SOAP-ENV:Header>\n" +
            "    <S:Body>\n" +
            "        <ns4:designAndAssign\n" +
            "            xmlns:ns2=\"http://www.telcordia.com/fullfillment/platform/dto/v2\"\n" +
            "            xmlns:ns3=\"urn:com:ericsson:schema:xml:granite:sdk:common:data-types:1:0\"\n" +
            "            xmlns:ns4=\"urn:com:ericsson:schema:wsdl:oss:core:nd:ws:ProvisioningControllerService:command\"\n" +
            "            xmlns:ns5=\"urn:com:ericsson:schema:xml:granite:sdk:command:data-types:1:0\"\n" +
            "            xmlns:ns6=\"urn:com:ericsson:schema:xml:oss:core:framework:data-types:1:0\"\n" +
            "            xmlns:ns7=\"urn:com:ericsson:schema:xml:oss:core:nd:data-types:1:0\"\n" +
            "            xmlns:ns8=\"urn:com:ericsson:schema:xml:oss:core:project-management:data-types:1:0\"\n" +
            "            xmlns:ns9=\"urn:com:ericsson:schema:xml:oss:core:uie:data-types:1:0\">\n" +
            "            <request>\n" +
            "                <provisioningOrder>\n" +
            "                    <name>Crm_NewInstall_"+session_id+"_45</name>\n" +
            "                    <clientId>CrmBeesionCR</clientId>\n" +
            "                    <clientCorrelationId>CRM-CRI-"+session_id+"-94</clientCorrelationId>\n" +
            "                    <expectedCompletionDate>"+Formato.format(day)+"T00:00:00Z</expectedCompletionDate>\n" +
            "                    <priority>1</priority>\n" +
            "                    <provisioningOrderItem>\n" +
            "                        <serviceType>fiber:GponLink</serviceType>\n" +
            "                        <serviceName>NewInstallService</serviceName>\n" +
            "                        <action>ADD</action>\n" +
            "                        <requestData>\n" +
            "                            <amx:amxService\n" +
            "                                xmlns:amx=\"urn:com:ericsson:schema:xml:oss:core:amx:obj:data-types:1:0\">\n" +
            "                                <customerType>RESIDENTIAL</customerType>\n" +
            "                                <endPoint>\n" +
            "                                    <type>"+AccessPointType+"</type>\n" +
            "                                    <name>"+AccessPointName+"</name>\n" +
            "                                </endPoint>\n" +
            "                                <serviceData>\n" +
            "                                    <type>INTERNET</type>\n" +
            "                                    <actionType>ADD</actionType>\n" +
            "                                    <bandwidth>"+speed+"</bandwidth>\n" +
            "                                    <bandwidthUnit>MB</bandwidthUnit>\n" +
            "                                </serviceData>\n" +
            "                                <countryCode>CR</countryCode>\n" +
            "                            </amx:amxService>\n" +
            "                        </requestData>\n" +
            "                        <dynamicAttributes>\n" +
            "                            <dynamicAttribute>\n" +
            "                                <groupName>OTHER_LINK_DATA</groupName>\n" +
            "                                <attributeName>COUNTRY_CODE</attributeName>\n" +
            "                                <stringAttributeValue>CR</stringAttributeValue>\n" +
            "                            </dynamicAttribute>\n" +
            "                        </dynamicAttributes>\n" +
            "                    </provisioningOrderItem>\n" +
            "                </provisioningOrder>\n" +
            "            </request>\n" +
            "        </ns4:designAndAssign>\n" +
            "    </S:Body>\n" +
            "</S:Envelope>";
            //Create a Cliente
            archivo.crearLog("1.1-Requestxml: "+claro);
            CustomSoapClient client = new CustomSoapClient(true);

            //Calling tho method.
            SOAPMessage response = client.callMethodSoap("http://172.16.204.176:7280/oss-core-ws/ond-adv/ProvisioningControllerService?wsdl", claro, "urn:com:ericsson:schema:wsdl:oss:core:nd:ws:ProvisioningControllerService/ProvisioningControllerServicePortType/designAndAssignRequest");

            String resposeString = client.getStringResponse(response);
            archivo.crearLog("1.1-Responsexml: "+resposeString);
            System.out.println("RESPONSE sendreserva"+resposeString);

            SOAPBody body = response.getSOAPBody();

            NodeList listaEmpleados = body.getElementsByTagName("return");
            Node test = null;
            if(listaEmpleados.getLength() == 0){
                nuevo = "error provisioningOrder";
                System.out.println("error takeover cms");
            }else{

                for(int temp1 = 0; temp1 < listaEmpleados.getLength(); temp1++){
                    test = listaEmpleados.item(temp1);
                    nuevo = test.getTextContent().replace(" ", "").replaceAll("[\n\r]","");
                }
                String projectKey = nuevo.substring(0,4);
                String provisioningRequestKey = nuevo.substring(4,8);
                System.out.println("projectKey: "+projectKey);
                System.out.println("provisioningRequestKey: "+provisioningRequestKey);
                System.out.println("resp: "+nuevo);
                String data12 = sendtoeai(provisioningRequestKey,projectKey,key);
                System.out.println("resp sendtoeai: "+data12);
                nuevokey = projectKey;
                
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return nuevokey;
    }
         
           
    public static String getvelocidad(String p_contract,String p_customer) throws Exception
    {
        String elements= null;
        String jsonf  = null;
        try{
            URL url = new URL("http://172.17.241.248:7012/cwf/sr/v1/product/?fields=productOffering,comprisedOf,id,name,activeStartDates,productCharacteristics,services,productRelationships&relatedParties.role=Customer&relatedParties.reference="+p_customer+"&relatedEntities.role=Contract&relatedEntities.reference="+p_contract);
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
            
            String childs  = null;
            String fath  = null;
                jsonf = obj.getJSONArray("data").getJSONObject(2).getJSONArray("productCharacteristics").getJSONObject(4).getString("value");
             
            conn.disconnect();
	  } catch (MalformedURLException e) {

		System.out.println(e);

	  } catch (IOException e) {

		System.out.println(e);
          }
	return jsonf;
    }
     
        
    public String  sendtoeai(String provisioningRequestKey, String projectKey, String key)
    {
        String nuevo = null;
        try 
        {
           String claro = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<S:Envelope\n" +
                "    xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <SOAP-ENV:Header>\n" +
                "        <graniteSessionInfo\n" +
                "            xmlns=\"urn:com:ericsson:schema:xml:granite:sdk:common:data-types:1:0\"\n" +
                "            xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "            xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "            <webAuthenticationToken\n" +
                "                xmlns=\"\">"+key+"\n" +
                "            </webAuthenticationToken>\n" +
                "            <originatingSystem\n" +
                "                xmlns=\"\">CRM\n" +
                "            </originatingSystem>\n" +
                "        </graniteSessionInfo>\n" +
                "    </SOAP-ENV:Header>\n" +
                "    <S:Body>\n" +
                "        <ns4:fetchByKey\n" +
                "            xmlns:ns2=\"http://www.telcordia.com/fullfillment/platform/dto/v2\"\n" +
                "            xmlns:ns3=\"urn:com:ericsson:schema:xml:granite:sdk:common:data-types:1:0\"\n" +
                "            xmlns:ns4=\"urn:com:ericsson:schema:wsdl:oss:core:nd:ws:ProvisioningRequestService:command\"\n" +
                "            xmlns:ns5=\"urn:com:ericsson:schema:xml:granite:sdk:command:data-types:1:0\"\n" +
                "            xmlns:ns6=\"urn:com:ericsson:schema:xml:oss:core:framework:data-types:1:0\"\n" +
                "            xmlns:ns7=\"urn:com:ericsson:schema:xml:oss:core:nd:data-types:1:0\"\n" +
                "            xmlns:ns8=\"urn:com:ericsson:schema:xml:oss:core:project-management:data-types:1:0\"\n" +
                "            xmlns:ns9=\"urn:com:ericsson:schema:xml:oss:core:uie:data-types:1:0\">\n" +
                "            <provisioningRequestKey>\n" +
                "                <keyValue>"+provisioningRequestKey+"</keyValue>\n" +
                "            </provisioningRequestKey>\n" +
                "            <provisioningRequestFetchSpec/>\n" +
                "        </ns4:fetchByKey>\n" +
                "    </S:Body>\n" +
                "</S:Envelope>";
            //Create a Cliente
            archivo.crearLog("1.2-Requestxml: "+claro);
            CustomSoapClient client = new CustomSoapClient(true);

            //Calling tho method.
            SOAPMessage response = client.callMethodSoap("http://172.16.204.176:7280/oss-core-ws/ond/ProvisioningRequestService?wsdl", claro, "urn:com:ericsson:schema:wsdl:oss:core:nd:ws:ProvisioningRequestService/ProvisioningRequestServicePortType/fetchByKeyRequest");

            String resposeString = client.getStringResponse(response);
            archivo.crearLog("1.2-Responsexml: "+resposeString);
            System.out.println("RESPONSE sendtoeai:"+resposeString);

            SOAPBody body = response.getSOAPBody();

            NodeList listaEmpleados = body.getElementsByTagName("return");
            Node test = null;
            if(listaEmpleados.getLength() == 0){
                nuevo = "error provisioningOrder";
                System.out.println("error takeover cms");
            }else{

               
                String data = sendtoeaiprojectKey(projectKey,key);
                nuevo = "exito";
                System.out.println("resp: "+nuevo);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return nuevo;
    }
      
          
    public String  sendtoeaiprojectKey(String projectKey,String key)
    {
        String nuevo = null;
        try 
        {
           String claro = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<S:Envelope\n" +
                "    xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <SOAP-ENV:Header>\n" +
                "        <graniteSessionInfo\n" +
                "            xmlns=\"urn:com:ericsson:schema:xml:granite:sdk:common:data-types:1:0\"\n" +
                "            xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "            xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "            <webAuthenticationToken\n" +
                "                xmlns=\"\">"+key+"\n" +
                "            </webAuthenticationToken>\n" +
                "            <originatingSystem\n" +
                "                xmlns=\"\">CRM\n" +
                "            </originatingSystem>\n" +
                "        </graniteSessionInfo>\n" +
                "    </SOAP-ENV:Header>\n" +
                "    <S:Body>\n" +
                "        <ns4:fetch\n" +
                "            xmlns:ns2=\"http://www.telcordia.com/fullfillment/platform/dto/v2\"\n" +
                "            xmlns:ns3=\"urn:com:ericsson:schema:xml:granite:sdk:common:data-types:1:0\"\n" +
                "            xmlns:ns4=\"urn:com:ericsson:schema:wsdl:oss:core:nd:ws:ProvisioningControllerService:command\"\n" +
                "            xmlns:ns5=\"urn:com:ericsson:schema:xml:granite:sdk:command:data-types:1:0\"\n" +
                "            xmlns:ns6=\"urn:com:ericsson:schema:xml:oss:core:framework:data-types:1:0\"\n" +
                "            xmlns:ns7=\"urn:com:ericsson:schema:xml:oss:core:nd:data-types:1:0\"\n" +
                "            xmlns:ns8=\"urn:com:ericsson:schema:xml:oss:core:project-management:data-types:1:0\"\n" +
                "            xmlns:ns9=\"urn:com:ericsson:schema:xml:oss:core:uie:data-types:1:0\">\n" +
                "            <request>\n" +
                "                <projectKey>\n" +
                "                    <keyValue>"+projectKey+"</keyValue>\n" +
                "                </projectKey>\n" +
                "                <serviceGraphFetchSpec>\n" +
                "                    <fetchTopologyElements>false</fetchTopologyElements>\n" +
                "                    <outputView>ACTIVATION</outputView>\n" +
                "                    <calculateServiceElementAction>true</calculateServiceElementAction>\n" +
                "                    <fetchServiceComponents>false</fetchServiceComponents>\n" +
                "                </serviceGraphFetchSpec>\n" +
                "            </request>\n" +
                "        </ns4:fetch>\n" +
                "    </S:Body>\n" +
                "</S:Envelope>";
            //Create a Cliente
            archivo.crearLog("1.3-Requestxml: "+claro);
            CustomSoapClient client = new CustomSoapClient(true);

            //Calling tho method.
            SOAPMessage response = client.callMethodSoap("http://172.16.204.176:7280/oss-core-ws/ond-adv/ProvisioningControllerService?wsdl", claro, "urn:com:ericsson:schema:wsdl:oss:core:nd:ws:ProvisioningControllerService/ProvisioningControllerServicePortType/fetchRequest");

            String resposeString = client.getStringResponse(response);
            archivo.crearLog("1.3-Responsexml: "+resposeString);
            System.out.println("RESPONSE sendtoeaiprojectKey"+resposeString);

            SOAPBody body = response.getSOAPBody();

            NodeList listaEmpleados = body.getElementsByTagName("return");
            Node test = null;
            if(listaEmpleados.getLength() == 0){
                nuevo = "error provisioningOrder";
                System.out.println("error takeover cms");
            }else{

                nuevo = "exito";
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return nuevo;
    }
    
    
     public ResultSet querysdata(int p_data1, String p_data2) throws SQLException
    {
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
                 String places = "SELECT AR.PARTY_ADDRESS_ROLE_ID, AR.ADDRESS_ROLE_TYPE_ID, AR.ADDRESS_ID, RT.NAME, LA.FULL_ADDRESS, LA.GEO_DATA, LA.ADDRESS_LINE_1\n" +
                         "FROM TBL_CM_PARTY_ADDRESS_ROLES AR\n" +
                         "INNER JOIN TBL_CM_ADDRESS_ROLE_TYPES RT ON RT.ADDRESS_ROLE_TYPE_ID = AR.ADDRESS_ROLE_TYPE_ID\n" +
                         "INNER JOIN TBL_LOC_ADDRESSES LA ON LA.PARTY_ID = AR.PARTY_ID\n" +
                         "WHERE AR.PARTY_ID = (SELECT PARTY_ID FROM TBL_CRM_CUSTOMERS WHERE external_id ='"+p_data2+"'" +") AND AR.ADDRESS_ROLE_TYPE_ID = 1";
                 pstm = conn.prepareStatement(places);
                 res = pstm.executeQuery();
                 break;
            case 6:
                
                String geodata = "select geo_data from TBL_CRM_GEO_DATA\n" +
                         "where interaction_id="+p_data2;
                 pstm = conn.prepareStatement(geodata);
                 res = pstm.executeQuery();
                 break;
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
         }catch(Exception e){
             System.out.println(e);
         }
         return res;
     }
    
     public void insertordereom(String customer, String contract, String ordereom, String action, String nodocument)
    {
         
         try{
             toDbx03();
             Statement st = conn.createStatement();
             st.executeUpdate("INSERT INTO TBL_CRM_ORDEREOME (ID, CUSTOMERID, CONTRACT, ORDEREOM, ACTION, NODOCUMENT) \n" +
                    "VALUES(SEQ_CRM_ORDEREOM.NEXTVAL,'"+customer+"','"+contract+"','"+ordereom+"','"+action+"','"+nodocument+"')");
             conn.commit();
             conn.close();         
                          
         }catch(SQLException e){
             System.out.println(e);
         }
     }
     
     public void toDbx03()
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
     
}
