/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.claro.takeover;

import com.claro.ws.CustomSoapClient;
import java.sql.Connection;
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;

/**
 *
 * @author Albert
 */
@Path("consumo")
public class LimiteConsumo {
    
    @Context
    private UriInfo context;
    private Connection conn = null;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("updateLDC")
    public Response takeover(@QueryParam("customer") String dn_id, @QueryParam("contract") String value){
        
        JSONObject jsonCreateUserResponse = new JSONObject();
        
        int p_value = Integer.parseInt(dn_id);  
        int p_value1 = p_value * 100000;
        String values = String.valueOf(p_value1);
        String casos = updateldc(value,values);
        System.out.println("Valor: "+values);
        
        try {
            jsonCreateUserResponse.put("mode","test");
        } catch (JSONException ex) {
            Logger.getLogger(LimiteConsumo.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Response.ok(jsonCreateUserResponse.toString())
               .header("Access-Control-Allow-Origin", "*")
               .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD")
               .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }
    
    public String  updateldc(String dn_id, String value){
        String nuevo = null;
        try 
        {
            String claro = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws_cs_pospaid_operations.webservice.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <ws:credit_limit_update_amount_business>\n" +
                "         <!--Optional:-->\n" +
                "         <originId>"+dn_id+"</originId>\n" +
                "         <!--Optional:-->\n" +
                "         <msisdn>"+dn_id+"</msisdn>\n" +
                "         <!--Optional:-->\n" +
                "         <type>CORP</type>\n" +
                "         <!--Optional:-->\n" +
                "         <trxId>"+dn_id+"</trxId>\n" +
                "         <!--Optional:-->\n" +
                "         <amount>"+value+"</amount>\n" +
                "      </ws:credit_limit_update_amount_business>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
            //Create a Cliente
            CustomSoapClient client = new CustomSoapClient(true);
            
            //Calling tho method.
            SOAPMessage response = client.callMethodSoap("http://172.16.168.143:7018/AirCharingWLogic/WS_CS_POSPAID_OPERATIONS?wsdl", claro, "http://ws_cs_pospaid_operations.webservice.com/WsCsPosPaidOperations/credit_limit_update_amount_businessRequest");
            System.out.println("Envio el string");
            String resposeString = client.getStringResponse(response);
            System.out.println(resposeString);

            SOAPBody body = response.getSOAPBody(); 
            
            nuevo = "0";
            
        } catch (Exception e) {
            System.out.println(e);
            nuevo = "1";
        }
        return nuevo;
    }
}
