/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.Queue;

/**
 *
<<<<<<< HEAD
 * @author davidValencia
 */
public class Consumer extends Thread {

    private Queue<Integer> queue;
    private final Object monitor;

    public Consumer(Queue<Integer> queue, Object monitor) {
        this.queue = queue;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (monitor) {
                // Verificar si la cola está vacía
                while (queue.isEmpty()) {
                    try {
                        monitor.wait(); // Esperar a que haya elementos en la cola
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

                int elem = queue.poll();
                System.out.println("Consumer consumes " + elem);

                monitor.notify(); // Notificar al productor que hay espacio en la cola
            }
        }
    }
}


=======
 * @author hcadavid
 */
public class Consumer extends Thread{
    
    private Queue<Integer> queue;
    
    
    public Consumer(Queue<Integer> queue){
        this.queue=queue;        
    }
    
    @Override
    public void run() {
        while (true) {

            if (queue.size() > 0) {
                int elem=queue.poll();
                System.out.println("Consumer consumes "+elem);                                
            }
            
        }
    }
}
>>>>>>> origin/develop
