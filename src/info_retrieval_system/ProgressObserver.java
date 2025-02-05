package info_retrieval_system;

public interface ProgressObserver {
	
	void initProgressbar(int endValue);
	
    void updateDataWriteProgressBar(int progress);
    
    void updateMergingProgressBar(int progress);
    
    void updateNormsCalculationProgressBar(int progress);
    
    void printDataWriteStatistics(long exeTime, int docsProcessed);
    
    void printMergingStatistics(long exeTime, int mergesDone);
    
    void printNormCalculationStatistics(long exeTime);
    
}