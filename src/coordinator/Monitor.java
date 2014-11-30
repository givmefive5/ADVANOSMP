package coordinator;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Monitor {

	Lock lock = new ReentrantLock();

	Condition cond = lock.newCondition();

	int count = 1;

	public void waiting() {
		lock.lock();

		try {
			while (count == 0) {
				cond.await();
			}
			count--;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public void signaling() {
		lock.lock();
		cond.signalAll();
		count++;
		lock.unlock();
	}

}
