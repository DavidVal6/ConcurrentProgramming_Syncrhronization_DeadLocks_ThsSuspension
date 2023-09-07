package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private AtomicInteger health = new AtomicInteger();
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;
    private Object lock;
    private boolean running = true;
    private AtomicBoolean alive = new AtomicBoolean(true);

    private final Random r = new Random(System.currentTimeMillis());


    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb, Object lock) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health.set(health);
        this.defaultDamageValue=defaultDamageValue;
        this.lock = lock;
    }

    public void run() {

        while (true) {
            synchronized(health){
                if (this.health.get() <= 0){
                    this.alive.set(false);
                    break;
                }
            };
            if(running) {
                Immortal im;

                int myIndex = immortalsPopulation.indexOf(this);

                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                //avoid self-fight
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }

                im = immortalsPopulation.get(nextFighterIndex);

                this.fight(im);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                synchronized(lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            

        }

    }

    public boolean isImAlive(){
        return alive.get();
    }

    public void stopRunning() {
        running = false;
    }

    public void continueRunning() {
        running = true;
    }

    public void fight(Immortal i2) {
        AtomicInteger i2Health = i2.getHealth();
        
        synchronized(health){
            synchronized(i2Health){
                if (i2Health.get() > 0) {
                    i2.changeHealth(i2.getHealth().get() - defaultDamageValue);
                    this.health.addAndGet(defaultDamageValue);
                    updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
                }else {
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                }
            }
        }
    } 


    public void changeHealth(int v) {
        health.set(v);
    }

    public AtomicInteger getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
