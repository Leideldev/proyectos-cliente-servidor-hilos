/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorhilosprivado;

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
import java.util.logging.Handler;
/**
 *
 * @author Fer
 */
public class ServidorHilosPrivado {

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
                    
                  if (input.toLowerCase().startsWith("/privado")) {
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
                        for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
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
