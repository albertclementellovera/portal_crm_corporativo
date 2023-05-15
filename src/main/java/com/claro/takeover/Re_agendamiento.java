/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.claro.takeover;

import static com.claro.takeover.agendamiento_tecnico.sendeom;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Albert
 */
@Path("changeInstallationDate")
public class Re_agendamiento {
    
    @Context
    private UriInfo context;
    private Connection conn = null;  
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/json")
    @Path("agendatec")
    public Response takeover(@QueryParam("customer") String p_customer, @QueryParam("contract") String p_contract,
            @QueryParam("user") String p_user, @QueryParam("iderror") String p_iderror, @QueryParam("errordescription") String p_errordes,
            @QueryParam("winstar") String p_winstar,@QueryParam("winend") String p_winend,@QueryParam("timeslot") String p_timeslot) {
    
        JSONObject jsonCreateUserResponse = new JSONObject();
        JSONObject jsonCreateUserResponse2 = new JSONObject();
        try {
            Random  rnd = new Random();
            Date day = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(day);
            cal.add(Calendar.YEAR, 1);
            Date nuevaFecha = cal.getTime();
            DateFormat Formato = new SimpleDateFormat("yyyy-MM-dd");
            System.out.println("Fecha: "+Formato.format(day));
            System.out.println("Fecha: "+Formato.format(nuevaFecha));
            
            jsonCreateUserResponse.put("orderId",p_errordes);
            jsonCreateUserResponse.put("createdDate",Formato.format(day)+"T00:00:00.000Z");
            jsonCreateUserResponse.put("createdBy",p_user);
            jsonCreateUserResponse.put("description","Change Appointment Details");
            jsonCreateUserResponse.put("requestID","23782482019050612221"+rnd.nextInt(9999999));
            jsonCreateUserResponse.put("requester","Beesion");
            
            JSONObject notes = new JSONObject();
            notes.put("text", "Change Appointment for OrderId "+p_errordes);
            jsonCreateUserResponse.put("notes",notes);
            
            JSONObject appointment = new JSONObject();
            appointment.put("action", "Modify");
            appointment.put("reason", "requested by client");
            appointment.put("slaWindowStart",p_winstar);
            appointment.put("slaWindowEnd", p_winend);
            appointment.put("timeSlot", "All-Day");
            appointment.put("timeSlotDate", p_winstar);
            jsonCreateUserResponse.put("appointment",appointment);
            
            String ordereom = sendeom(jsonCreateUserResponse.toString());
            
            jsonCreateUserResponse2.put("order_id",ordereom);
            jsonCreateUserResponse2.put("error","0");
            jsonCreateUserResponse2.put("value",JSONObject.NULL);
            
        } catch (JSONException ex) {
            Logger.getLogger(Re_agendamiento.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Re_agendamiento.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Response.ok(jsonCreateUserResponse2.toString())
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }
    
    public static String sendeom(String orden) throws Exception {
        String elements= null;
         URL url = new URL("http://172.17.224.150:7060/cwf/wfm/changeInstallationDate");
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
            String id = obj.getString("orderId");
            System.out.println(id);
        return id;
    }
}
