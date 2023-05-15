/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.claro.takeover;
import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author Albert
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application{
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.claro.takeover.GenericResource.class);
        resources.add(com.claro.takeover.LimiteConsumo.class);
        resources.add(com.claro.takeover.Re_agendamiento.class);
        resources.add(com.claro.takeover.Traslado.class);
        resources.add(com.claro.takeover.agendamiento_tecnico.class);
        resources.add(com.claro.takeover.ordereom.class);
        resources.add(com.claro.takeover.takeover.class);
        resources.add(com.claro.takeover.takeovereom.class);
        resources.add(com.claro.takeover.takeruc.class);
    }
        
    
}
