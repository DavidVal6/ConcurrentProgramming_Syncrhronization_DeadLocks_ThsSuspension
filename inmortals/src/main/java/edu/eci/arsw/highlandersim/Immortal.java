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
    private boolean alive = true;
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

        while (alive) {
            if(running) {
                Immortal im;

                int myIndex = immortalsPopulation.indexOf(this);

                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                if (immortalsPopulation.size() == 1){
                    imDaWinner();
                    break;
                }
                //avoid self-fight
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }

                im = immortalsPopulation.get(nextFighterIndex);

                this.fight(im);

                try {
                    Thread.sleep(1);
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

    public void imDaWinner(){
        updateCallback.processReport("Me " + this + " , I AM IMMORTAAAAL"+"\n");
    }

    public boolean isImAlive(){
        return this.alive;
    }

    public void killImmortal(){
        this.alive = false;
        immortalsPopulation.remove(this);
    }

    public void stopRunning() {
        this.running = false;
    }

    public void continueRunning() {
        this.running = true;
    }

    public void fight(Immortal i2) {
        synchronized(myIndex < i2.getMyIndex()?this.health:i2.health){
            synchronized(myIndex > i2.getMyIndex()?this.health:i2.health){
                if (i2.getHealth().get() > 0) {
                    this.health.addAndGet(defaultDamageValue);
                    i2.health.addAndGet((-defaultDamageValue));
                    updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
                }else {
                    i2.killImmortal();
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                }
            }
        }
    }

    public int getMyIndex(){
        return this.myIndex;
    }

    public void changeHealth(int v) {
        this.health.set(v);
    }

    public AtomicInteger getHealth() {
        return this.health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
