package edu.eci.arsw.threads;

import java.util.LinkedList;
import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

public class ThreadCheck extends Thread{

    private HostBlacklistsDataSourceFacade skds;
    private int rangeLimitA;
    private int rangeLimitB;
    private String ipAdress;
    private LinkedList<Integer> indexes;
    private int serversChecked;

    private boolean end=false;

    private Object lock;

    private boolean paused = false;

    private int ocurrences;

    public void pauseThread(){
        this.paused = true;
    }

    public void resumeThread(){
        this.paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean hasEnd() {
        return end;
    }
    public void endThread(){
        this.end = true;
    }

    public ThreadCheck(HostBlacklistsDataSourceFacade skds, int rangeLimitA, int rangeLimitB, String ipAdress, Object lock){
        setServerList(skds, rangeLimitA, rangeLimitB);
        setIpToLookFor(ipAdress);
        this.indexes = new LinkedList<>();
        this.serversChecked = 0;
        this.lock = lock;

    }

    public static void main(String[] args){
        Object lock = new Object();
        ThreadCheck t = new ThreadCheck(HostBlacklistsDataSourceFacade.getInstance(), 0000,9000, "202.24.34.55",lock);
        t.start();
        //System.out.println("No. de servers:");
        //System.out.println(HostBlacklistsDataSourceFacade.getInstance().getRegisteredServersCount());
    }
    
    @Override
    public void run(){
        for(int i = rangeLimitA; i < rangeLimitB; i++){
            if(end){
                break;
            }
            if(paused) {
                synchronized(lock){
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            this.serversChecked++;
            if(skds.isInBlackListServer(i, ipAdress)){
                this.ocurrences++;
                this.indexes.add(i);
            }
        }
    }

    public void setServerList(HostBlacklistsDataSourceFacade skds, int rangeLimitA, int rangeLimitB){
        this.skds = skds;
        this.rangeLimitA = rangeLimitA;
        this.rangeLimitB = rangeLimitB;
    }

    public void setIpToLookFor(String ipAdress){
        this.ipAdress = ipAdress;
    }

    public int getOccurrences(){
        return this.ocurrences;
    }

    public int getServersChecked() {
        return this.serversChecked;
    }

    public LinkedList<Integer> getIndexes(){
        return this.indexes;
    }

}
