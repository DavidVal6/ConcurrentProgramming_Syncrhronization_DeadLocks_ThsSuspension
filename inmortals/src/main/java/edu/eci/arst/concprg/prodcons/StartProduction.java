/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StartProduction {

    public static void main(String[] args) {
        Queue<Integer> queue = new LinkedBlockingQueue<>();
        Object monitor = new Object(); // Objeto monitor para sincronización

        Producer producer = new Producer(queue, Long.MAX_VALUE, monitor);
        Consumer consumer = new Consumer(queue, monitor);

        producer.start();
        consumer.start();
    }
}