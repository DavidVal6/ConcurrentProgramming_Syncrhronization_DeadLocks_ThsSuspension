/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import edu.eci.arsw.threads.ThreadCheck;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {
    private Object lock = new Object();
    private boolean running = true;

    private static final int BLACK_LIST_ALARM_COUNT=5;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public ConcurrentLinkedDeque<Integer> checkHost(String ipaddress, int N){
        
        ConcurrentLinkedDeque<Integer> blackListOcurrences=new ConcurrentLinkedDeque<Integer>();
        
        AtomicInteger ocurrencesCount = new AtomicInteger(0);
        
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        
        AtomicInteger checkedListsCount = new AtomicInteger(0);

        ArrayList<ThreadCheck> threads = new ArrayList<>();

        int numberOfServers = skds.getRegisteredServersCount();

        int size = numberOfServers/N;

        boolean trust=true;
        
        for (int i=0; i<N;i++){

            int a = i*size;
            int b;
            if(i==N-1){
                b = numberOfServers;
            } else {
                b = size*(i+1);
            }
            
            threads.add(new ThreadCheck(HostBlacklistsDataSourceFacade.getInstance(), a, b, ipaddress,this.lock));
            
        }

        for(ThreadCheck thread:threads){
            thread.start();
        }

        while (true) {
            pauseThreads(threads);
            for (ThreadCheck thread : threads) {
                //System.out.println(thread.getIndexes());
                for (Integer value: thread.getIndexes()){
                    if(!blackListOcurrences.contains(value)){
                        blackListOcurrences.addAll(thread.getIndexes());
                    }
                }
                //blackListOcurrences.addAll(thread.getIndexes());
                ocurrencesCount.addAndGet(thread.getOccurrences());
                checkedListsCount.addAndGet(thread.getServersChecked());
            }

            if (ocurrencesCount.get() >= BLACK_LIST_ALARM_COUNT) {
                endThreads(threads);
                trust=false;
                break;
            }

            resumeThreads(threads);
            checkedListsCount.set(0);
        }
        
        if (trust){
            skds.reportAsTrustworthy(ipaddress);
        }else{
            skds.reportAsNotTrustworthy(ipaddress);
        }
        
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
        
        return blackListOcurrences;
    }

    public void pauseThreads(ArrayList<ThreadCheck> threads){
        for (ThreadCheck thread : threads){
            thread.pauseThread();
        }
    }

    public void  resumeThreads(ArrayList<ThreadCheck> threads){
        for (ThreadCheck thread : threads){
            thread.resumeThread();
        }
        synchronized (lock){
            lock.notifyAll();
        };
    }
    public void endThreads(ArrayList<ThreadCheck> threads){
        for (ThreadCheck thread: threads){
            thread.endThread();
        }
    }
    
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    
    
    
}