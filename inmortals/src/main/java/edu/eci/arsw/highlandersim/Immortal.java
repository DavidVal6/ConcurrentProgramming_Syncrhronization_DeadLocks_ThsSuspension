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
    private int myIndex;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb, Object lock) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health.set(health);
        this.defaultDamageValue=defaultDamageValue;
        this.lock = lock;
        this.myIndex = immortalsPopulation.indexOf(this);
    }

    public void run() {

        while (true) {
            synchronized(health){
                if (this.getHealth().get() <= 0){
                    this.killImmortal();
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
                    Thread.sleep(500);
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

    public AtomicBoolean isImAlive(){
        return alive;
    }

    public void killImmortal(){
        alive.set(false);
    }

    public void stopRunning() {
        running = false;
    }

    public void continueRunning() {
        running = true;
    }

    public void fight(Immortal i2) {
        AtomicInteger i2Health = i2.getHealth();
        if (myIndex < i2.getMyIndex()){
            synchronized(health){
                synchronized(i2Health){
                    realFight(this, i2, i2Health);
                }
            }
        }else if (myIndex > i2.getMyIndex()){
            synchronized(i2Health){
                synchronized(health){
                    realFight(this, i2, i2Health);
                }
            }
        }else{
            if (r.nextBoolean()){
                synchronized(health){
                    synchronized(i2Health){
                        realFight(this, i2, i2Health);
                    }
                }
            }else{
                synchronized(i2Health){
                    synchronized(health){
                        realFight(this, i2, i2Health);
                    }
                }
            }
        }
    }

    public synchronized void realFight(Immortal i1, Immortal i2, AtomicInteger i2Health){
        if (i2Health.get() > 0) {
            i2.changeHealth(i2.getHealth().get() - defaultDamageValue);
            this.health.addAndGet(defaultDamageValue);
            updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
        }else {
            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
        }
    }

    public int getMyIndex(){
        return this.myIndex;
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
