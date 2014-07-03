package pl.edu.agh.informatyka.so.threadsdemo;

import java.util.ArrayList;
import java.util.List;

public class BookshopState {
	private int readerCnt = 0;
	private int writerCnt = 0;
	private int[] isReading = null;
	private int currentWriter = -1;	
	
	public BookshopState() {
	}

	public BookshopState(int readerCnt, int writerCnt, int[] isReading,
			int currentWriter) {
		this.readerCnt = readerCnt;
		this.writerCnt = writerCnt;
		this.isReading = isReading;
		this.currentWriter = currentWriter;
	}

	public boolean anybodyWriting() {
		return currentWriter >= 0;
	}
	
	public boolean isReading(int i) {
		return isReading[i] != 0;
	}
	
	public int getCurrentWriter() {
		return currentWriter;
	}
	
	public List<Integer> getActiveReaders() {
		List<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < isReading.length; i++)
			if (isReading(i))
				res.add(i);
		return res;
	}
	
	public int getReaderCount() {
		return readerCnt;
	}	
	
	public int getWriterCount() {
		return writerCnt;
	}
}
