/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.claro.takeover;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;

/**
 *
 * @author Albert
 */
public class Crm_logs {
    String dir = "\\app\\weblogicMaxActPre\\Oracle\\Middleware\\Oracle_Home\\user_projects\\domains\\MAXACTPREPA\\servers\\AdminServer\\upload";
    FileWriter archivo;
    
    public void crearLog(String Operacion){
        try{
            this.dir = dir;
              //Pregunta el archivo existe, caso contrario crea uno con el nombre log.txt
              if (new File(dir+"//"+"log.txt").exists()==false){archivo=new FileWriter(new File(dir +"//"+"log.txt"),false);}
              archivo = new FileWriter(new File(dir +"//"+"log.txt"), true);
              Calendar fechaActual = Calendar.getInstance(); //Para poder utilizar el paquete calendar
              //Empieza a escribir en el archivo
              archivo.write("["+(String.valueOf(fechaActual.get(Calendar.DAY_OF_MONTH))
              +"/"+String.valueOf(fechaActual.get(Calendar.MONTH)+1)
              +"/"+String.valueOf(fechaActual.get(Calendar.YEAR))
              +" "+String.valueOf(fechaActual.get(Calendar.HOUR_OF_DAY))
              +":"+String.valueOf(fechaActual.get(Calendar.MINUTE))
              +":"+String.valueOf(fechaActual.get(Calendar.SECOND)))+"]"+"[INFO]"+ " " +Operacion+"\r\n");
              archivo.close(); //Se cierra el archivo
        }catch(Exception ex){
              System.out.println(ex);
        }
    }
}