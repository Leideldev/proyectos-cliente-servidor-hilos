/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorhilosbd;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JOptionPane;

/**
 *
 * @author crise
 */
public class ServidorHilosBD {

    private static Set<String> names = new HashSet<>();
    private static Set<PrintWriter> writers = new HashSet<>();
   static HashMap <String, PrintWriter> map = new HashMap <String, PrintWriter> ();
   static HashMap <String, ArrayList <String>> bloqueados = new HashMap <String, ArrayList <String>> ();
    public static void main(String[] args)  {
        System.out.println("The chat server is running... ");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));

            }
        }catch(IOException e){
            System.out.println("Error al crear el servidor ");
        }

    }

    private static class Handler implements Runnable {

        private String name;
        private String password;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        conexion con = new conexion();
        
        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                ArrayList <String> bloquea = new ArrayList <String>();
                boolean breakeado = false;
                while (true) {
                    if(breakeado){
                           break;
                    }
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    con.conectarse();
                    if(con.existeLogin(name)){
                     while (true) {
                    out.println("SUBMITPASSWORD");
                    password = in.nextLine();
                    if(con.datosLogin(name, password)){
                        System.out.println("Conexion exitosa de: " + name);
                        con.cerrarConexion();
                        breakeado = true;
                        break;
                    }
                         out.println("INCORRECTPASS");
                }   
                    }else{
                    out.println("SUBMITPASSWORD");
                    password = in.nextLine();
                    con.guardarLogin(name, password);
                    con.cerrarConexion();
                    breakeado = true;
                    break;
                    }
                    
                }
                if(password==null){
                   while (true) {
                    out.println("SUBMITPASSWORD");
                    password = in.nextLine(); 
                     if (password == null) {
                        return;
                    }else{
                         break;
                     }    
                } 
                }
                 
                out.println("NAMEACCEPTED " + name);
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " has joined");
                }        
                writers.add(out);
                map.put(name, out);
                bloqueados.put(name, bloquea);
                con.cerrarConexion();

                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    
                    if(input.toLowerCase().startsWith("/bloquear")){
                        System.out.println("ENTRA A BLOQUEAR");
                        String [] bloqueado = input.toLowerCase().substring(8).trim().split(" ");
                        boolean yabloqueado = false;
                       
                        con.conectarse();
                        System.out.println(bloqueado.length);
                        if(bloqueado.length<2){      
                            map.get(name).println("MESSAGE " + "Sistema: Escribe el nombre del usuario a bloquear");
                        } else{
                        if(!con.existeLogin(bloqueado[1])){
                             map.get(name).println("MESSAGE " + "Sistema: El usuario no existe o no está conectado");
                             yabloqueado=true;
                             con.cerrarConexion();
                        }else if(con.verificarBloqueado(name, bloqueado[1])){
                                 map.get(name).println("MESSAGE " + "Sistema: El usuario ya está bloqueado");
                                 yabloqueado = true;
                                 con.cerrarConexion();
                        }                         
                        if(yabloqueado != true){
                         con.bloquear(name, bloqueado[1]);                        
                         con.cerrarConexion();
                        map.get(name).println("MESSAGE " + "Sistema: Bloqueaste a " + bloqueado[1] );    
                        }
                        }              
                    }else if(input.toLowerCase().startsWith("/desbloquear")){ 
                    System.out.println("ENTRA A DESBLOQUEAR");
                    String [] desbloqueado = input.toLowerCase().substring(12).trim().split(" ");
                    con.conectarse();
                    if(!con.verificarBloqueado(name, desbloqueado[0])){
                             map.get(name).println("MESSAGE " + "Sistema: El usuario no se encuentra bloqueado o no existe");
                             con.cerrarConexion();
                        }else{
                        if(con.verificarBloqueado(name, desbloqueado[0])){
                            con.eliminarBloqueo(name, desbloqueado[0]);
                            map.get(name).println("MESSAGE " + "Sistema: Usuario desbloqueado " + desbloqueado[0]);
                            con.cerrarConexion();
                        }else{
                           map.get(name).println("MESSAGE " + "Sistema: No existe ningun bloqueado con ese nombre"); 
                           con.cerrarConexion();
                        }
                    }    
                        }else{if (input.toLowerCase().startsWith("/privado")) {
                       System.out.println("ENTRA A PRIVADO");
                       String [] mensaje = input.toLowerCase().substring(8).trim().split(" ");
                       String mensajes = "";
                        
                        con.conectarse();
                           if(mensaje[0].equals("") || map.get(mensaje[0]) == null ){
                               map.get(name).println("MESSAGE " + "Sistema: El nombre de destinatario es incorrecto o no existe");
                           }else if(mensaje.length < 2){
                               map.get(name).println("MESSAGE " + "Sistema: Escribe un mensaje para enviar al destinatario");                             
                       }else{
                         for(int i=1; i < mensaje.length; i++){
                             mensajes += mensaje[i] + " ";
                         }  
                         if(map.get(mensaje[0]) != null){
                             if(!con.verificarBloqueado(mensaje[0], name )){
                                map.get(mensaje[0]).println("MESSAGE " +"Mensaje privado de "+ name + ": " + mensajes);
                                con.cerrarConexion();
                             }                
                       }  
                       }
                            con.cerrarConexion();
                    }else{
                            con.conectarse();
                        for(String usuario : map.keySet()){
                         bloqueados = con.verificarBloqueados(usuario);
                            System.out.println(bloqueados.get(usuario));
                            if(bloqueados.isEmpty()){
                              map.get(usuario).println("MESSAGE " + name + ": " + input);                      
                            }else{            
                             if(!bloqueados.get(usuario).contains(name)){                                  
                                 map.get(usuario).println("MESSAGE " + name + ": " + input);                                       
                             }  
                               
                            }                  
                        }
                       con.cerrarConexion();
                    }  
                }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    writers.remove(out);
                }
                if (name != null) {
                    System.out.println(" is leaving");
                    names.remove(name);
                    for(PrintWriter writer:writers){
                        writer.println("MESSAGE "+name+" has left");
                    }
                }
                try{
                    socket.close();
                } catch(IOException e){
                }
                }
            }

        }
    
}
