/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.library.utils;




/**
 * Some functions for handling memory - control etc
 * @author Fiete
 * @version $326$
 */
public class MemoryManagment {
	
	/**
	 * minimal amount of free memory after calling gc and sleeping for 3 seconds
	 */
	static long minMemory = 20000;
	
	/**
	 * after calling gc in case of low memory we wait this amount of millisecs
	 */
	static long waitingMillis=3000;
	
	static Runtime r = java.lang.Runtime.getRuntime();
	
	public MemoryManagment() {
		
	}

	 /** 
     * Ändert die Priotität des Finalizer-Threads. 
     * @param newPriority Die Priorität, die der Finalizer-Thread bekommen soll. 
     * from Helge Stieghahn
     * (Fiete)
     */ 
    public static final void setFinalizerPriority(int newPriority) 
    { 
        new FinalizerChanger(newPriority); 
        java.lang.System.gc(); 
        java.lang.System.runFinalization(); 
    } 

    /** 
     * A class to make the finalizer thread max priority.
     * from Helge Stieghahn
     * (Fiete) 
     */ 
    private final static class FinalizerChanger 
    { 
        final int m_priority; 
        private FinalizerChanger(int priority) 
        { 
            this.m_priority = priority; 
        } 
        public void finalize() 
        { 
            Thread.currentThread().setPriority(this.m_priority); 
        } 
    }    

    
    /**
     * checks, if there es enough free memory fpr the JVM
     * if not, invokes the garbage collector
     * if not succesfull returns false, otherwise true
     * @return true, if enoug memory available
     * @author Fiete
     */
    public static boolean isFreeMemory(){
    	// Runtime r = java.lang.Runtime.getRuntime();
    	if (checkFreeMemory()){
    		return true;
    	}
    	r.gc();
    	try {
    		Thread.sleep(waitingMillis);
    	} catch (InterruptedException e){
    		// do nothing...
    	}
    	if (checkFreeMemory()){
    		return true;
    	}
    	return false;
    }
    
    private static boolean checkFreeMemory(){
    	
    	if (r.freeMemory()>minMemory){
    		return true;
    	}
    	
    	// as long as we have totalMemory < maxMemory the gc will increase totalMemory
    	// and may be we have not to worry...
    	if (r.totalMemory()<r.maxMemory()){
    		return true;
    	}
    	return false;
    }
	
}
