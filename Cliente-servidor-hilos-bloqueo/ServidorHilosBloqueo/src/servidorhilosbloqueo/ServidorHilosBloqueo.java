/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorhilosbloqueo;

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

/**
 *
 * @author crise
 */
public class ServidorHilosBloqueo {

    /**
     * @param args the command line arguments
     */
     private static Set<String> names = new HashSet<>();
    private static Set<PrintWriter> writers = new HashSet<>();
   static HashMap <String, PrintWriter> map = new HashMap <String, PrintWriter> ();
   static HashMap <String, ArrayList <String>> bloqueados = new HashMap <String, ArrayList <String>> ();
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running... ");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));

            }
        }

    }

    private static class Handler implements Runnable {

        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        
        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                ArrayList <String> bloquea = new ArrayList <String>();

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;

                        }
                    }
                }
                out.println("NAMEACCEPTED " + name);
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " has joined");
                }
                
                map.put(name, out);
                bloqueados.put(name, bloquea);
                writers.add(out);

                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    
                    if(input.toLowerCase().startsWith("/bloquear")){
                        System.out.println("ENTRA A BLOQUEAR");
                        String [] bloqueado = input.toLowerCase().substring(8).trim().split(" ");
                        boolean yabloqueado = false;
                        if(map.get(bloqueado[1]) == null){
                             map.get(name).println("MESSAGE " + "Sistema: El usuario no existe o no está conectado");
                             yabloqueado=true;
                        }else{
                            for(int i=0; i < bloqueados.get(name).size();i++){
                             if(bloqueados.get(name).get(i).equals(bloqueado[1])){
                                 map.get(name).println("MESSAGE " + "Sistema: El usuario ya está bloqueado");
                                 yabloqueado = true;
                             }
                        }   
                        }
                        if(yabloqueado != true){
                         bloqueados.get(name).add(bloqueado[1]);
                        map.get(name).println("MESSAGE " + "Sistema: Bloqueaste a " + bloqueado[1] );    
                        }
                                          
                    }else if(input.toLowerCase().startsWith("/desbloquear")){ 
                    System.out.println("ENTRA A DESBLOQUEAR");
                    String [] desbloqueado = input.toLowerCase().substring(12).trim().split(" ");                 
                    if(bloqueados.get(name).isEmpty()){
                             map.get(name).println("MESSAGE " + "Sistema: No hay ningun usuario bloqueado");
                          
                        }else{
                        if(bloqueados.get(name).contains(desbloqueado[0])){
                            bloqueados.get(name).remove(desbloqueado[0]);
                            map.get(name).println("MESSAGE " + "Sistema: Usuario desbloqueado " + desbloqueado[0]);
                        }else{
                           map.get(name).println("MESSAGE " + "Sistema: No existe ningun bloqueado con ese nombre"); 
                        }
                    }    
                        }else{if (input.toLowerCase().startsWith("/privado")) {
                       System.out.println("ENTRA A PRIVADO");
                       String [] mensaje = input.toLowerCase().substring(8).trim().split(" ");
                       String mensajes = "";
                       
                           
                           if(mensaje[0].equals("") || map.get(mensaje[0]) == null ){
                               map.get(name).println("MESSAGE " + "Sistema: El nombre de destinatario es incorrecto o no existe");
                           }else if(mensaje.length < 2){
                               map.get(name).println("MESSAGE " + "Sistema: Escribe un mensaje para enviar al destinatario");                             
                       }else{
                         for(int i=1; i < mensaje.length; i++){
                             mensajes += mensaje[i] + " ";
                         }  
                         if(map.get(mensaje[0]) != null){
                             if(bloqueados.get(mensaje[0]).isEmpty()){
                                 map.get(mensaje[0]).println("MESSAGE " +"Mensaje privado de "+ name + ": " + mensajes); 
                             }else{
                             
                             if(!bloqueados.get(mensaje[0]).contains(name)){
                                 map.get(mensaje[0]).println("MESSAGE " +"Mensaje privado de "+ name + ": " + mensajes); 
                                 System.out.println(name);        
                             }  
                                      
                        }                   
                       }  
                       }                                         
                    }else{
                        for(String usuario : map.keySet()){
                            
                            if(bloqueados.get(usuario).isEmpty()){
                              map.get(usuario).println("MESSAGE " + name + ": " + input); 
                                 
                            }else{            
                             if(!bloqueados.get(usuario).contains(name)){                                  
                                 map.get(usuario).println("MESSAGE " + name + ": " + input);                                       
                             }  
                               
                            }                  
                        }
                       
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
