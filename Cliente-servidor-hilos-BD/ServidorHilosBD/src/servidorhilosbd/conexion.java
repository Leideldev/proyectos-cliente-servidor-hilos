/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorhilosbd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author crise
 */
public class conexion {
    String ruta = "C:/Users/"+ System.getProperty("user.name") + "/usuariosbd";
    Connection conexion;
    
    public void conectarse(){
        try{
          conexion = DriverManager.getConnection("jdbc:sqlite:" + ruta);
            System.out.println("Conectado a la base de datos");
            
        }catch(SQLException e){
            System.out.println("No fue posible conectarse a la base de datos");
        }  
    }
    
    public void cerrarConexion(){
        try{
           conexion.close(); 
        }catch(SQLException e){
            System.out.println("Error al tratar de cerrar la conexion");
        }  
        
    }
    
     public void guardarLogin(String nombre, String contrasena){
        try {
            PreparedStatement st = conexion.prepareStatement("insert into usuario (nombre, contrasena) values (?,?)");
            st.setString(1, nombre);
            st.setString(2, contrasena);
            st.execute();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }

    }
     
     public boolean existeLogin(String nombre){
        ResultSet result = null;
        try {
            PreparedStatement st = conexion.prepareStatement("select nombre,contrasena from usuario where nombre=?");
            st.setString(1, nombre);
           
            result = st.executeQuery();
           if(result.getString("nombre").equals(nombre)){
               System.out.print("Nombre: ");
               System.out.println(result.getString("nombre"));          
                return true;
           }        
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return false;
    } 
     
     public boolean datosLogin(String nombre, String contrasena){
        ResultSet result = null;
        try {
            PreparedStatement st = conexion.prepareStatement("select nombre,contrasena from usuario where nombre=? AND contrasena=?");
            st.setString(1, nombre);
            st.setString(2, contrasena);
            result = st.executeQuery();
           if(result.getString("nombre").equals(nombre) && result.getString("contrasena").equals(contrasena)){
               System.out.print("Nombre: ");
               System.out.println(result.getString("nombre"));
                System.out.print("Contrasena: ");
                System.out.println(result.getString("contrasena"));
                return true;
           }        
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return false;
    }
     
     public void bloquear(String nombreusuario, String nombrebloqueado){
         try {
            PreparedStatement st = conexion.prepareStatement("insert into bloqueados (nombreusuario, nombrebloqueado) values (?,?)");
            st.setString(1, nombreusuario);
            st.setString(2, nombrebloqueado);
            st.execute();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
     }
     
     public boolean verificarBloqueado( String nombreusuario, String nombrebloqueado){
         
        ResultSet result = null;
        try {
            PreparedStatement st = conexion.prepareStatement("select nombreusuario,nombrebloqueado from bloqueados where nombreusuario=? AND nombrebloqueado=?");
            st.setString(1, nombreusuario);
            st.setString(2, nombrebloqueado);
            result = st.executeQuery();       
                if(result.getString("nombrebloqueado").equals(nombrebloqueado) && result.getString("nombreusuario").equals(nombreusuario)){
                   System.out.print("Nombre usuario: ");
                System.out.println(result.getString("nombreusuario"));

                System.out.print("Nombre bloqueado: ");
                System.out.println(result.getString("nombrebloqueado"));                             
                return true;         
                }            
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return false;
     }
     
     public HashMap verificarBloqueados( String nombreusuario){
         HashMap <String, ArrayList <String>> bloqueados = new HashMap <String, ArrayList <String>> ();
         ArrayList <String> bloquea = new ArrayList <String>();
        ResultSet result = null;
        try {
            PreparedStatement st = conexion.prepareStatement("select nombreusuario,nombrebloqueado from bloqueados where nombreusuario=?");
            st.setString(1, nombreusuario);     
            result = st.executeQuery(); 
            
             while (result.next()) {
                
                bloqueados.put(result.getString("nombreusuario"), bloquea);
                bloqueados.get(result.getString("nombreusuario")).add(result.getString("nombrebloqueado"));
               
            }
                return bloqueados;   
            
                
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return bloqueados;
     }
     
      public void eliminarBloqueo( String nombreusuario, String nombrebloqueado){
         
        
        try {
            PreparedStatement st = conexion.prepareStatement("DELETE from bloqueados where nombreusuario=? AND nombrebloqueado=?");
            st.setString(1, nombreusuario);
            st.setString(2, nombrebloqueado);
            st.executeUpdate();                                     
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
     
     }
}
